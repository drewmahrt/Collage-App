package com.drewmahrt.collageapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, MenuItem.OnMenuItemClickListener, View.OnFocusChangeListener {
    private static final String TAG = "CollageActivity";
    private static final int PICTURE_GALLERY = 1;
    private static final int STORAGE_PERMISSION = 0;
    private FloatingActionButton addFab;
    private MenuItem deleteButton, saveButton, sortUpButton, colorButton, zoomOutButton, zoomInButton;
    private FrameLayout mCollageContainer;
    private ScaleGestureDetector mScaleDetector;
    private boolean isScaling;
    private int touchedId;


    private List<CollageImage> mCollageImages;

    private int _xDelta;
    private int _yDelta;
    private int mStartX, mStartY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collage_creator);



        ActionMenuView bottomBar = (ActionMenuView)findViewById(R.id.amvMenu);
        Menu bottomMenu = bottomBar.getMenu();
        getMenuInflater().inflate(R.menu.collage_menu,bottomMenu);

        mCollageImages = new ArrayList<>();

        addFab = (FloatingActionButton)findViewById(R.id.addFab);
        deleteButton = bottomMenu.findItem(R.id.delete_image_action);
        saveButton = bottomMenu.findItem(R.id.save_action);
        sortUpButton = bottomMenu.findItem(R.id.sort_layer_up_action);
//        sortDownButton = bottomMenu.findItem(R.id.sort_layer_down_action);
        colorButton = bottomMenu.findItem(R.id.pick_color_action);
//        zoomOutButton = bottomMenu.findItem(R.id.zoom_out_action);
//        zoomInButton = bottomMenu.findItem(R.id.zoom_in_action);
        mCollageContainer = (FrameLayout)findViewById(R.id.collage_container);
        mCollageContainer.setOnTouchListener(this);

        isScaling = false;
        touchedId = -1;

        final Context alertContext = this;
        deleteButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new AlertDialog.Builder(alertContext)
                        .setTitle("Abandon Collage")
                        .setMessage("Are you sure you want to delete this collage?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getWindow().getDecorView().setSystemUiVisibility(
                                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return false;
            }
        });

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.frame);
        //final CollageContainer dv= new CollageContainer(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, R.id.toolbar_bottom);
        //rl.addView(dv, params);

        //Fix to bring FAB on top for 4.x devices
        addFab.bringToFront();

        saveButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                saveCollage();
                return true;
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICTURE_GALLERY);
            }
        });

        colorButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                colorButtonPressed();
                return false;
            }
        });
//        zoomInButton.setOnMenuItemClickListener(dv);
//        zoomOutButton.setOnMenuItemClickListener(dv);
//        sortDownButton.setOnMenuItemClickListener(dv);
        sortUpButton.setOnMenuItemClickListener(this);
        deleteButton.setOnMenuItemClickListener(this);

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
                Log.d(TAG, "onScale: height orig: "+image.getHeight()+" new: "+height);
                Log.d(TAG, "onScale: width orig: "+image.getWidth()+" new: "+width);

                image.setMinimumHeight((int)height);
                image.setMinimumWidth((int)width);
                return false;
            }
        });

        //Trigger tutorial
        new MaterialShowcaseView.Builder(this)
                .setTarget(addFab)
                .setDismissText("GOT IT")
                .setDismissOnTouch(true)
                .setContentText("Click here to add new photos to your collage. \n\nPlace your finger on a photo and drag to move it. \n\nHold your finger on a picture without moving to delete it.")
                .singleUse("PICTURE DETAILS") // provide a unique ID used to ensure it is only shown once
                .show();
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
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // View holds position while being chosen to be delete or edited
//        if (mEditingOrDeleting) {
//            return false;
//        }


        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) view.getLayoutParams();

        Log.d(TAG, "onTouch: touchedId: "+touchedId+"   current: "+view.getId());
        if(motionEvent.getPointerCount() > 1 && touchedId != -1){
            Log.d(TAG, "onTouch: scaling");
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


    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        Log.d(TAG, "getResizedBitmap: width: "+width+"  height: "+height);
        return Bitmap.createScaledBitmap(image, width, height, true);
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
                        selectedImage = getResizedBitmap(selectedImage, 500);// 400 is for example, replace with desired size
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
//                        mCollageImages.add(new CollageImage(selectedImage.getWidth(),selectedImage.getHeight(),imageUri,newImage.getId()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void saveCollage(){

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);
        }else {

            //remove selected image border
            if(mCollageContainer.getFocusedChild() != null) {
                mCollageContainer.getFocusedChild().setBackgroundResource(R.drawable.unselected_border);
            }

            //Find the view we are after
            View view = (View) findViewById(R.id.collage_container);
            //Create a Bitmap with the same dimensions
            Bitmap image = Bitmap.createBitmap(mCollageContainer.getWidth(),
                    view.getHeight(),
                    Bitmap.Config.RGB_565);
            //Draw the view inside the Bitmap
            Canvas testcanvas = new Canvas();
            view.draw(new Canvas(image));

            //TODO: Make the pictures save in the correct folder
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fOut = null;
            File file = new File(path, "collage" + System.currentTimeMillis() + ".jpg"); // the File to save to
            try {
                fOut = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.JPEG, 90, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                fOut.flush();
                fOut.close(); // do not forget to close the stream

                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                Toast.makeText(this, "Collage saved!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
        if (!storageDir.exists())
            storageDir.mkdirs();
        File image = File.createTempFile(
                timeStamp,                   /* prefix */
                ".jpeg",                     /* suffix */
                storageDir                   /* directory */
        );
        return image;
    }

    public static void addPicToGallery(Context context, String photoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
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
                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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

                    saveCollage();

                } else {

                    Toast.makeText(this, "Images cannot be saved without storage access", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
        }
        return true;
    }

    private void deleteSelectedImage(){
        if(mCollageContainer.getFocusedChild() != null){
            Log.d(TAG, "deleteSelectedImage: ");
            mCollageContainer.removeView(mCollageContainer.getFocusedChild());
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            Log.d(TAG, "onFocusChange: ");
            v.setBackgroundResource(R.drawable.selected_border);
        } else {
            v.setBackgroundResource(R.drawable.unselected_border);
        }
    }
}
