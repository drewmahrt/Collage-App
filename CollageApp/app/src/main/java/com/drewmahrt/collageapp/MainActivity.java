package com.drewmahrt.collageapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, MenuItem.OnMenuItemClickListener, View.OnFocusChangeListener {
    private static final String TAG = "CollageActivity";
    private static final int PICTURE_GALLERY = 1;
    private static final int STORAGE_PERMISSION = 0;

    private float mScaleFactor = 1.f;

    private static final String ADD_PREFERENCES_KEY = "add_tutorial";
    private static final String BACKGROUND_PREFERENCES_KEY = "background_tutorial";
    private static final String LAYER_PREFERENCES_KEY = "layer_tutorial";

    private SharedPreferences mPreferences;

    private FloatingActionButton mAddImageButton;
    private MenuItem mDeleteButton, mSaveButton, mSortUpButton, mChooseColorButton, mRotateLeftButton, mRotateRightButton;
    private FrameLayout mCollageContainer;

    private ScaleGestureDetector mScaleDetector;
    private boolean isScaling;
    private int touchedId;
    private int touchedIndex;

    private List<CollageImage> mCollageImages;

    private int mStartX, mStartY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collage_creator);

        mPreferences = getPreferences(MODE_PRIVATE);

        ActionMenuView bottomBar = (ActionMenuView)findViewById(R.id.amvMenu);
        Menu bottomMenu = bottomBar.getMenu();
        getMenuInflater().inflate(R.menu.collage_menu,bottomMenu);

        mCollageImages = new ArrayList<>();

        mAddImageButton = (FloatingActionButton)findViewById(R.id.addFab);
        mDeleteButton = bottomMenu.findItem(R.id.delete_image_action);
        mSaveButton = bottomMenu.findItem(R.id.save_action);
        mSortUpButton = bottomMenu.findItem(R.id.sort_layer_up_action);
        mChooseColorButton = bottomMenu.findItem(R.id.pick_color_action);
        mRotateLeftButton = bottomMenu.findItem(R.id.rotate_left_action);
        mRotateRightButton = bottomMenu.findItem(R.id.rotate_right_action);
        mCollageContainer = (FrameLayout)findViewById(R.id.collage_container);

        mCollageContainer.setOnTouchListener(this);

        isScaling = false;
        touchedId = -1;
        touchedIndex = -1;

        //Fix to bring FAB on top for 4.x devices
        mAddImageButton.bringToFront();
        restoreFullScreen();

        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoPicker();
            }
        });

        mSaveButton.setOnMenuItemClickListener(this);
        mChooseColorButton.setOnMenuItemClickListener(this);
        mSortUpButton.setOnMenuItemClickListener(this);
        mDeleteButton.setOnMenuItemClickListener(this);
        mRotateRightButton.setOnMenuItemClickListener(this);
        mRotateLeftButton.setOnMenuItemClickListener(this);

        setupScaleDetector();

        //If first time launching, show add picture tutorial
        if(!mPreferences.getBoolean(ADD_PREFERENCES_KEY,false))
            triggerIntroShowcase(R.id.addFab,"Tap here to add you first photo!",ADD_PREFERENCES_KEY);
    }


    private void startPhotoPicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICTURE_GALLERY);
    }

    private void triggerIntroShowcase(final int id, String message, final String preferencesKey) {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(id),message)
                .transparentTarget(true)
                .cancelable(false),
                new TapTargetView.Listener(){
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        mPreferences.edit().putBoolean(preferencesKey,true).apply();
                        view.dismiss(true);

                        switch (id){
                            case R.id.addFab:
                                startPhotoPicker();
                                break;
                            case R.id.pick_color_action:
                                colorButtonPressed();
                                break;
                            case R.id.sort_layer_up_action:
                                bringToFront();
                                break;
                        }
                    }
                }
        );
    }



    @Override
    protected void onResume() {
        super.onResume();
        restoreFullScreen();
    }

    private void setupScaleDetector() {
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                
            }
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isScaling = true;
                return true;
            }
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                View image = mCollageContainer.getFocusedChild();

                float scale = Math.max(0.95f,Math.min(detector.getScaleFactor(),1.1f));
                CollageImage currentImage = mCollageImages.get(touchedIndex);
                currentImage.setScale(currentImage.getScale()*scale);

                image.setScaleX(currentImage.getScale());
                image.setScaleY(currentImage.getScale());

                return false;

            }
        });

        ScaleGestureDetector.SimpleOnScaleGestureListener listener = new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                return super.onScale(detector);
            }
        };
    }

    public Uri addImageToGallery(Context context, String title, String description) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void bringToFront(){
        if(mCollageContainer.getFocusedChild() != null) {
            mCollageContainer.getFocusedChild().bringToFront();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collage_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        //Check for touching background of canvas which causes ClassCastException for RelativeLayout to FrameLayout
        if(view.getId() == R.id.collage_container)
            return false;


        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) view.getLayoutParams();

        if(motionEvent.getPointerCount() > 1 && touchedId != -1){
            mScaleDetector.onTouchEvent(motionEvent);
        } else if(touchedId == view.getId() || touchedId == -1) {
            view.requestFocus();
            view.requestFocusFromTouch();
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    touchedId = view.getId();
                    touchedIndex = findCollageIndex(touchedId);
                    mStartX = (int) motionEvent.getX();
                    mStartY = (int) motionEvent.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    //only allow moving if all fingers have been removed from a previous scaling action
                    if(!isScaling) {
                        int delta_x = (int) motionEvent.getX() - mStartX;
                        int delta_y = (int) motionEvent.getY() - mStartY;
                        layoutParams.leftMargin = layoutParams.leftMargin + delta_x;
                        layoutParams.rightMargin = layoutParams.rightMargin - delta_x;
                        layoutParams.topMargin = layoutParams.topMargin + delta_y;
                        layoutParams.bottomMargin = layoutParams.bottomMargin - delta_y;
                        view.setLayoutParams(layoutParams);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    //set scaling flag to false since all fingers have been removed.
                    isScaling = false;
                    touchedId = -1;
                    touchedIndex = -1;
                    break;
            }
        }

        mCollageContainer.invalidate();

        return true;
    }

    private int findCollageIndex(int touchedId) {
        for (int i = 0; i < mCollageImages.size(); i++) {
            if(touchedId == mCollageImages.get(i).getId())
                return i;
        }
        return -1;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICTURE_GALLERY:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri imageUri = data.getData();
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        selectedImage = ImageUtility.getResizedBitmap(selectedImage, 600);
                        ImageView newImage = new ImageView(this);
                        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                        newImage.setLayoutParams(layoutParams);
                        newImage.setImageBitmap(selectedImage);
                        newImage.setFocusableInTouchMode(true);
                        newImage.setFocusable(true);
                        newImage.setOnTouchListener(this);
                        newImage.setOnFocusChangeListener(this);
                        newImage.setId(View.generateViewId());
                        newImage.requestFocus();
                        newImage.requestFocusFromTouch();
                        newImage.setOnTouchListener(this);
                        mCollageContainer.addView(newImage);
                        mCollageImages.add(new CollageImage(selectedImage.getWidth(),selectedImage.getHeight(),imageUri,newImage.getId()));


                        //reset image scale factor for new image
                        mScaleFactor = 1f;

                        //If first time adding a picture, show background color tutorial
                        if(!mPreferences.getBoolean(BACKGROUND_PREFERENCES_KEY,false))
                            triggerIntroShowcase(R.id.pick_color_action,"Tap here to choose your background color.",BACKGROUND_PREFERENCES_KEY);

                        //If first time with two pictures, show layer button tutorial
                        if(mCollageImages.size() > 1 && !mPreferences.getBoolean(LAYER_PREFERENCES_KEY,false))
                            triggerIntroShowcase(R.id.sort_layer_up_action,"Use this button to move the last selected image to the top layer.",LAYER_PREFERENCES_KEY);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }

        restoreFullScreen();
    }



    private void colorButtonPressed() {
        ColorPickerDialogBuilder
                .with(MainActivity.this)
                .setTitle("Choose background color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .noSliders()
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {

                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        mCollageContainer.setBackgroundColor(selectedColor);
                        dialog.dismiss();
                        mCollageContainer.invalidate();
                        restoreFullScreen();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        restoreFullScreen();
                    }
                })
                .build()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    ImageUtility.saveCollage(this,mCollageContainer);
                } else {

                    Toast.makeText(this, "Images cannot be saved without storage access", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
        restoreFullScreen();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort_layer_up_action:
                bringToFront();
                break;
            case R.id.delete_image_action:
                deleteSelectedImage();
                break;
            case R.id.pick_color_action:
                colorButtonPressed();
                break;
            case R.id.save_action:
                ImageUtility.saveCollage(this,mCollageContainer);
                break;
            case R.id.rotate_left_action:
                rotateImage(-30);
                break;
            case R.id.rotate_right_action:
                rotateImage(30);
                break;
        }
        return true;
    }

    private void rotateImage(float i) {
        View image = mCollageContainer.getFocusedChild();
        if(image != null) {
            Log.d(TAG, "rotateImage: current: " + image.getRotation() + " delta: " + i);
            image.setRotation(image.getRotation() + i);
        }
    }

    private void deleteSelectedImage(){
        if(mCollageContainer.getFocusedChild() != null){
            mCollageContainer.removeView(mCollageContainer.getFocusedChild());
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundResource(R.drawable.selected_border);
        } else {
            v.setBackgroundResource(R.drawable.unselected_border);
        }
    }

    private void restoreFullScreen(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
