package com.drewmahrt.collageapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, MenuItem.OnMenuItemClickListener, View.OnFocusChangeListener {
    private static final String TAG = "CollageActivity";
    private static final int PICTURE_GALLERY = 1;
    private static final int STORAGE_PERMISSION = 0;

    private FloatingActionButton mAddImageButton;
    private MenuItem mDeleteButton, mSaveButton, mSortUpButton, mChooseColorButton;
    private FrameLayout mCollageContainer;

    private ScaleGestureDetector mScaleDetector;
    private boolean isScaling;
    private int touchedId;

    private List<CollageImage> mCollageImages;

    private int mStartX, mStartY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collage_creator);

        ActionMenuView bottomBar = (ActionMenuView)findViewById(R.id.amvMenu);
        Menu bottomMenu = bottomBar.getMenu();
        getMenuInflater().inflate(R.menu.collage_menu,bottomMenu);

        mCollageImages = new ArrayList<>();

        mAddImageButton = (FloatingActionButton)findViewById(R.id.addFab);
        mDeleteButton = bottomMenu.findItem(R.id.delete_image_action);
        mSaveButton = bottomMenu.findItem(R.id.save_action);
        mSortUpButton = bottomMenu.findItem(R.id.sort_layer_up_action);
        mChooseColorButton = bottomMenu.findItem(R.id.pick_color_action);
        mCollageContainer = (FrameLayout)findViewById(R.id.collage_container);

        mCollageContainer.setOnTouchListener(this);

        isScaling = false;
        touchedId = -1;

        //Fix to bring FAB on top for 4.x devices
        mAddImageButton.bringToFront();
        restoreFullScreen();

        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICTURE_GALLERY);
            }
        });

        mSaveButton.setOnMenuItemClickListener(this);
        mChooseColorButton.setOnMenuItemClickListener(this);
        mSortUpButton.setOnMenuItemClickListener(this);
        mDeleteButton.setOnMenuItemClickListener(this);

        setupScaleDetector();
    }

    private void setupScaleDetector() {
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
            }
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isScaling = true;
                return true;
            }
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                Log.d(TAG, "scale: " + detector.getScaleFactor());
                View image = mCollageContainer.getFocusedChild();

                float scale = detector.getScaleFactor();
                if(scale < 1)
                    scale = 0.95f;
                else
                    scale = 1.05f;

                float height = image.getHeight() * scale;
                float width = image.getWidth() * scale;

                if(Math.abs(image.getHeight()-height) > 10){
                    if(image.getHeight() - height < 0){
                        //image scaled up, but too fast
                        height = image.getHeight() + 10;
                    }else {
                        height = image.getHeight() - 10;

                    }
                }

                if(Math.abs(image.getWidth()-width) > 10){
                    if(image.getWidth() - width < 0){
                        //image scaled up, but too fast
                        width = image.getWidth() + 10;
                    }else {
                        width = image.getWidth() - 10;

                    }
                }

                image.setMinimumHeight((int)height);
                image.setMinimumWidth((int)width);
                return false;
            }
        });
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

        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) view.getLayoutParams();

        if(motionEvent.getPointerCount() > 1 && touchedId != -1){
            mScaleDetector.onTouchEvent(motionEvent);
        } else if(!isScaling && (touchedId == view.getId() || touchedId == -1)) {
            view.requestFocus();
            view.requestFocusFromTouch();
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    touchedId = view.getId();
                    mStartX = (int) motionEvent.getX();
                    mStartY = (int) motionEvent.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    int delta_x = (int) motionEvent.getX() - mStartX;
                    int delta_y = (int) motionEvent.getY() - mStartY;
                    layoutParams.leftMargin = layoutParams.leftMargin + delta_x;
                    layoutParams.rightMargin = layoutParams.rightMargin - delta_x;
                    layoutParams.topMargin = layoutParams.topMargin + delta_y;
                    layoutParams.bottomMargin = layoutParams.bottomMargin - delta_y;
                    view.setLayoutParams(layoutParams);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    touchedId = -1;
                    break;
            }
        }

        mCollageContainer.invalidate();
        return true;
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
                        selectedImage = ImageUtility.getResizedBitmap(selectedImage, 500);// 400 is for example, replace with desired size
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
                        mCollageContainer.addView(newImage);
                        mCollageImages.add(new CollageImage(selectedImage.getWidth(),selectedImage.getHeight(),imageUri,newImage.getId()));
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
                .setTitle("Choose color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
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
        }
        return true;
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
