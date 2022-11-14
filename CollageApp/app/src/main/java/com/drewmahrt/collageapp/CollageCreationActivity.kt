package com.drewmahrt.collageapp

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ActionMenuView
import com.drewmahrt.collageapp.SaveImageUtility.getResizedBitmap
import com.drewmahrt.collageapp.SaveImageUtility.saveMediaToStorage
import com.drewmahrt.collageapp.databinding.ActivityCollageCreatorBinding
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import java.io.FileNotFoundException

class CollageCreationActivity : AppCompatActivity(), OnTouchListener, ActionMenuView.OnMenuItemClickListener,
    OnFocusChangeListener {
    private var mScaleDetector: ScaleGestureDetector? = null

    private var isScaling = false
    private var touchedId = 0
    private var touchedIndex = 0
    private var mCollageImages = mutableListOf<CollageImage>()
    private var mStartX = 0
    private var mStartY = 0

    private lateinit var binding: ActivityCollageCreatorBinding

    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            imagePicked(uri)
        } else {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollageCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomMenu = binding.actionMenuView.menu
        menuInflater.inflate(R.menu.collage_menu, bottomMenu)
        mCollageImages = ArrayList()

        binding.collageContainer.setOnTouchListener(this)
        isScaling = false
        touchedId = -1
        touchedIndex = -1

        //Fix to bring FAB on top for 4.x devices
        binding.addFab.bringToFront()
        restoreFullScreen()
        binding.addFab.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.actionMenuView.setOnMenuItemClickListener(this)
        setupScaleDetector()

        val preferences = getPreferences(MODE_PRIVATE)
        //If first time launching, show add picture tutorial
        if (!preferences.getBoolean(ADD_PREFERENCES_KEY, false)) {
            triggerIntroShowcase(
                R.id.add_fab,
                "Tap here to add you first photo!",
                ADD_PREFERENCES_KEY
            )
        }
    }

    private fun triggerIntroShowcase(id: Int, message: String, preferencesKey: String) {
        val preferences = getPreferences(MODE_PRIVATE)
        TapTargetView.showFor(this,
            TapTarget.forView(findViewById(id), message)
                .transparentTarget(true)
                .cancelable(false),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    preferences.edit().putBoolean(preferencesKey, true).apply()
                    view.dismiss(true)
                    when (id) {
                        R.id.add_fab -> pickImageLauncher.launch("image/*")
                        R.id.pick_color_action -> colorButtonPressed()
                        R.id.sort_layer_up_action -> bringToFront()
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        restoreFullScreen()
    }

    private fun setupScaleDetector() {
        mScaleDetector = ScaleGestureDetector(this, object : OnScaleGestureListener {
            override fun onScaleEnd(detector: ScaleGestureDetector) {}
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isScaling = true
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val image = binding.collageContainer.focusedChild
                val scale = Math.max(0.95f, Math.min(detector.scaleFactor, 1.1f))
                val currentImage = mCollageImages[touchedIndex]
                currentImage.scale = currentImage.scale * scale
                image.scaleX = currentImage.scale
                image.scaleY = currentImage.scale
                return false
            }
        })
        val listener: SimpleOnScaleGestureListener = object : SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                return super.onScale(detector)
            }
        }
    }

    private fun bringToFront() {
        if (binding.collageContainer.focusedChild != null) {
            binding.collageContainer.focusedChild.bringToFront()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.collage_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

        //Check for touching background of canvas which causes ClassCastException for RelativeLayout to FrameLayout
        if (view.id == R.id.collage_container) return false
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        if (motionEvent.pointerCount > 1 && touchedId != -1) {
            mScaleDetector!!.onTouchEvent(motionEvent)
        } else if (touchedId == view.id || touchedId == -1) {
            view.requestFocus()
            view.requestFocusFromTouch()
            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    touchedId = view.id
                    touchedIndex = findCollageIndex(touchedId)
                    mStartX = motionEvent.x.toInt()
                    mStartY = motionEvent.y.toInt()
                }
                MotionEvent.ACTION_MOVE ->                     //only allow moving if all fingers have been removed from a previous scaling action
                    if (!isScaling) {
                        val delta_x = motionEvent.x.toInt() - mStartX
                        val delta_y = motionEvent.y.toInt() - mStartY
                        layoutParams.leftMargin = layoutParams.leftMargin + delta_x
                        layoutParams.rightMargin = layoutParams.rightMargin - delta_x
                        layoutParams.topMargin = layoutParams.topMargin + delta_y
                        layoutParams.bottomMargin = layoutParams.bottomMargin - delta_y
                        view.layoutParams = layoutParams
                    }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    //set scaling flag to false since all fingers have been removed.
                    isScaling = false
                    touchedId = -1
                    touchedIndex = -1
                }
            }
        }
        binding.collageContainer.invalidate()
        return true
    }

    private fun findCollageIndex(touchedId: Int): Int {
        for (i in mCollageImages.indices) {
            if (touchedId == mCollageImages[i].id) return i
        }
        return -1
    }

    private fun imagePicked(uri: Uri) {
        try {
            val imageUri = uri
            val imageStream = contentResolver.openInputStream(imageUri)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            val resizedImage = getResizedBitmap(selectedImage, 600)
            val newImage = ImageView(this)
            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            newImage.layoutParams = layoutParams
            newImage.setImageBitmap(resizedImage)
            newImage.isFocusableInTouchMode = true
            newImage.isFocusable = true
            newImage.setOnTouchListener(this)
            newImage.onFocusChangeListener = this
            newImage.id = View.generateViewId()
            newImage.requestFocus()
            newImage.requestFocusFromTouch()
            newImage.setOnTouchListener(this)
            binding.collageContainer.addView(newImage)

            mCollageImages.add(
                CollageImage(
                    resizedImage.width.toFloat(),
                    resizedImage.height.toFloat(), imageUri, newImage.id
                )
            )

            //If first time adding a picture, show background color tutorial
            val preferences = getPreferences(MODE_PRIVATE)
            if (!preferences.getBoolean(BACKGROUND_PREFERENCES_KEY, false))
                triggerIntroShowcase(
                    R.id.pick_color_action,
                    "Tap here to choose your background color.",
                    BACKGROUND_PREFERENCES_KEY
                )

            //If first time with two pictures, show layer button tutorial
            if (mCollageImages.size > 1 && !preferences.getBoolean(LAYER_PREFERENCES_KEY, false))
                triggerIntroShowcase(
                    R.id.sort_layer_up_action,
                    "Use this button to move the last selected image to the top layer.",
                    LAYER_PREFERENCES_KEY
                )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun colorButtonPressed() {
        ColorPickerDialogBuilder
            .with(this@CollageCreationActivity)
            .setTitle("Choose background color")
            .initialColor(Color.WHITE)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .noSliders()
            .density(12)
            .setOnColorSelectedListener { }
            .setPositiveButton(
                "ok"
            ) { dialog, selectedColor, allColors ->
                binding.collageContainer.setBackgroundColor(selectedColor)
                dialog.dismiss()
                binding.collageContainer.invalidate()
                restoreFullScreen()
            }
            .setNegativeButton(
                "cancel"
            ) { dialog, which ->
                dialog.dismiss()
                restoreFullScreen()
            }
            .build()
            .show()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_layer_up_action -> bringToFront()
            R.id.delete_image_action -> deleteSelectedImage()
            R.id.pick_color_action -> colorButtonPressed()
            R.id.save_action -> saveMediaToStorage(getBitmapForSaving())
        }
        return true
    }

    private fun deleteSelectedImage() {
        if (binding.collageContainer.focusedChild != null) {
            binding.collageContainer.removeView(binding.collageContainer.focusedChild)
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            v.setBackgroundResource(R.drawable.selected_border)
        } else {
            v.setBackgroundResource(R.drawable.unselected_border)
        }
    }

    private fun restoreFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun getBitmapForSaving(): Bitmap {
        //remove selected image border
        if (binding.collageContainer.getFocusedChild() != null) {
            binding.collageContainer.getFocusedChild().setBackgroundResource(R.drawable.unselected_border)
        }

        binding.collageContainer
        //Find the view we are after
        //Create a Bitmap with the same dimensions
        val bitmap = Bitmap.createBitmap(
            binding.collageContainer.width,
            binding.collageContainer.height,
            Bitmap.Config.RGB_565
        )

        binding.collageContainer.draw(Canvas(bitmap))

        return bitmap
    }

    companion object {
        private const val TAG = "CollageActivity"
        private const val PICTURE_GALLERY = 1
        private const val STORAGE_PERMISSION = 0
        private const val ADD_PREFERENCES_KEY = "add_tutorial"
        private const val BACKGROUND_PREFERENCES_KEY = "background_tutorial"
        private const val LAYER_PREFERENCES_KEY = "layer_tutorial"
    }
}