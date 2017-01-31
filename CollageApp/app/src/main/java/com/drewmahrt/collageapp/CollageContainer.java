package com.drewmahrt.collageapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.InputStream;
import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by drewmahrt on 1/2/17.
 */

public class CollageContainer{
//public class CollageContainer extends View implements View.OnClickListener, MenuItem.OnMenuItemClickListener{
//    private static final int PICTURE_GALLERY = 1;
//    private static final String TAG = "CollageActivity.DV";
//    private ArrayList<CollageImage> imageList;
//    private float mScaleFactor = 1.f;
//    private int screenWidth,screenHeight;
//    private float currX,currY;
//    private int lastSelected;
//    private Paint paint;
//    private boolean isPressedDown;
//
//    public CollageContainer(Context context) {
//        super(context);
//        if(imageList == null) {
//            imageList = new ArrayList<CollageImage>();
//        }
//
//        Display display = ((AppCompatActivity)getContext()).getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        screenWidth = size.x;
//        screenHeight = size.y;
//        currX = 0;
//        currY = 0;
//        paint = new Paint();
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.WHITE);
//        lastSelected = -1;
//        isPressedDown = false;
//    }
//
//    public void addSelectedImage(CollageImage collageImage){
//        imageList.add(collageImage);
//        lastSelected = imageList.size()-1;
//    }
//
//    public void selectImage(){
//        final FragmentManager fm = ((FragmentActivity) getContext()).getSupportFragmentManager();
//        Fragment auxiliary = new Fragment() {
//            @Override
//            public void onActivityResult(int requestCode, int resultCode, Intent data) {
//                super.onActivityResult(requestCode, resultCode, data);
//                fm.beginTransaction().remove(this).commit();
//                if(requestCode == PICTURE_GALLERY && resultCode == Activity.RESULT_OK && data != null){
//                    try {
//                        Uri selectedImageUri = data.getData();
//                        Log.d(TAG, "URI1: " + selectedImageUri);
//                        InputStream inputStream = getContext().getContentResolver().openInputStream(selectedImageUri);
//                        final BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inJustDecodeBounds = true;
//                        // BitmapFactory.decodeFile(selectedImagePath,options);
//                        BitmapFactory.decodeStream(inputStream,null,options);
//                        inputStream.close();
//
//                        options.inSampleSize = ImageUtility.calculateInSampleSize(options,screenWidth,screenHeight);
//                        options.inJustDecodeBounds = false;
//
//
//                        //Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
//                        Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(selectedImageUri),null,options);
//                        Log.d(TAG,"BITMAP: "+bitmap);
//                        //imageList.add(new CollageImage(bitmap, resizeImageForImageView(BitmapFactory.decodeFile(selectedImagePath, options), (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4))));
//                        imageList.add(new CollageImage(bitmap, ImageUtility.resizeImageForImageView(BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(selectedImageUri),null,options), (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4)),screenWidth,screenHeight));
//                        lastSelected = imageList.size()-1;
//
//                    }catch (Exception e){
//
//                    }
//
//                    ((AppCompatActivity)getContext()).getWindow().getDecorView().setSystemUiVisibility(
//                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//                }
//                invalidate();
//            }
//        };
//        fm.beginTransaction().add(auxiliary, "FRAGMENT_TAG").commit();
//        fm.executePendingTransactions();
//
//        auxiliary.startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), PICTURE_GALLERY);
//
//        ShowcaseConfig config = new ShowcaseConfig();
//        config.setDelay(250); // quarter second between each showcase view
//
//        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence((Activity)getContext(),"CONTROLS");
//
//        sequence.setConfig(config);
//
//        sequence.addSequenceItem(((Activity) getContext()).findViewById(R.id.zoom_in_action),
//                "Click here to make the last selected image larger","GOT IT");
//
//        sequence.addSequenceItem(((Activity) getContext()).findViewById(R.id.zoom_out_action),
//                "Click here to make the last selected image smaller","GOT IT");
//
//        sequence.addSequenceItem(((Activity) getContext()).findViewById(R.id.sort_layer_up_action),
//                "Click here to move the last selected image up a layer.", "GOT IT");
//
//        sequence.addSequenceItem(((Activity) getContext()).findViewById(R.id.sort_layer_down_action),
//                "Click here to move the last selected image down a layer.", "GOT IT");
//
//        sequence.addSequenceItem(((Activity) getContext()).findViewById(R.id.pick_color_action),
//                "Click here to pick a new background color.", "GOT IT");
//
//        sequence.addSequenceItem(((Activity) getContext()).findViewById(R.id.save_action),
//                "Click here to save your collage.", "GOT IT");
//
//        sequence.addSequenceItem(((Activity) getContext()).findViewById(R.id.close_image_action),
//                "Click here to exit.", "GOT IT");
//
//        sequence.start();
//    }
//
//    public int getImageIndex(){
//        if(isPressedDown && lastSelected != -1)
//            return lastSelected;
//        int currIndex = -1;
//        for (CollageImage img:imageList) {
//            if(currX >= img.x && currX <= img.xEdge && currY >= img.y && currY <= img.yEdge)
//                currIndex = imageList.indexOf(img);
//        }
//        lastSelected = currIndex;
//        return currIndex;
//    }
//
//    public boolean onTouchEvent(MotionEvent event) {
//        currX = event.getX();
//        currY = event.getY();
//        int imageIndex = getImageIndex();
//        //mScaleDetector.onTouchEvent(event);
//        float newX,newY;
//        if(imageIndex >= 0) {
//            gestureDetector.onTouchEvent(event);
//            if(lastSelected == -1)
//                return true;
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN: {
//                }
//                break;
//
//                case MotionEvent.ACTION_MOVE:
//                    if(!isPressedDown)
//                        isPressedDown = true;
//                    newX = (float) (currX - (0.5 * imageList.get(imageIndex).resizedBitmap.getWidth()));
//                    newY = (float) (currY - (0.5 * imageList.get(imageIndex).resizedBitmap.getHeight()));
//                    if (newX < 0) newX = 0;
//                    if (newX + imageList.get(imageIndex).resizedBitmap.getWidth() > screenWidth)
//                        newX = screenWidth - imageList.get(imageIndex).resizedBitmap.getWidth();
//                    if (newY < 0) newY = 0;
//                    if (newY + imageList.get(imageIndex).resizedBitmap.getHeight() > screenHeight)
//                        newY = screenHeight - imageList.get(imageIndex).resizedBitmap.getHeight();
//                    imageList.get(imageIndex).x = newX;
//                    imageList.get(imageIndex).y = newY;
//                    imageList.get(imageIndex).xEdge = imageList.get(imageIndex).x + imageList.get(imageIndex).resizedBitmap.getWidth();
//                    imageList.get(imageIndex).yEdge = imageList.get(imageIndex).y + imageList.get(imageIndex).resizedBitmap.getHeight();
//                    invalidate();
//
//                    break;
//                case MotionEvent.ACTION_UP:
//                    newX = (float) (currX - (0.5 * imageList.get(imageIndex).resizedBitmap.getWidth()));
//                    newY = (float) (currY - (0.5 * imageList.get(imageIndex).resizedBitmap.getHeight()));
//                    if (newX < 0) newX = 0;
//                    if (newX + imageList.get(imageIndex).resizedBitmap.getWidth() > screenWidth)
//                        newX = screenWidth - imageList.get(imageIndex).resizedBitmap.getWidth();
//                    if (newY < 0) newY = 0;
//                    if (newY + imageList.get(imageIndex).resizedBitmap.getHeight() > screenHeight)
//                        newY = screenHeight - imageList.get(imageIndex).resizedBitmap.getHeight();
//                    imageList.get(imageIndex).x = newX;
//                    imageList.get(imageIndex).y = newY;
//                    imageList.get(imageIndex).xEdge = imageList.get(imageIndex).x + imageList.get(imageIndex).resizedBitmap.getWidth();
//                    imageList.get(imageIndex).yEdge = imageList.get(imageIndex).y + imageList.get(imageIndex).resizedBitmap.getHeight();
//                    isPressedDown = false;
//                    invalidate();
//                    break;
//            }
//        }
//        return true;
//    }
//
//    final GestureDetector gestureDetector = new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener() {
//        public void onLongPress(MotionEvent e) {
//            imageList.remove(lastSelected);
//            lastSelected = -1;
//            invalidate();
//        }
//    });
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.addFab:
//                addFabClicked();
//                break;
//        }
//    }
//
//    private void layerDownPressed() {
//        if (lastSelected > 0) {
//            CollageImage temp = imageList.remove(lastSelected);
//            imageList.add(lastSelected - 1, temp);
//            Log.d(TAG, "Down: Moving from " + lastSelected + " to " + (lastSelected - 1));
//            lastSelected--;
//            invalidate();
//        }
//    }
//
//    private void layerUpPressed() {
//        if (lastSelected >= 0 && lastSelected < imageList.size() - 1) {
//            CollageImage temp = imageList.remove(lastSelected);
//            imageList.add(lastSelected + 1, temp);
//            Log.d(TAG, "Up: Moving from " + lastSelected + " to " + (lastSelected + 1));
//            lastSelected++;
//            invalidate();
//        }
//    }
//
//    private void zoomOutPresssed() {
//        zoom("out");
//    }
//
//    private void zoomInPressed() {
//        zoom("in");
//    }
//
//    private void colorButtonPressed() {
//        ColorPickerDialogBuilder
//                .with(getContext())
//                .setTitle("Choose color")
//                .initialColor(Color.WHITE)
//                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
//                .density(12)
//                .setOnColorSelectedListener(new OnColorSelectedListener() {
//                    @Override
//                    public void onColorSelected(int selectedColor) {
//                        //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
//                    }
//                })
//                .setPositiveButton("ok", new ColorPickerClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
//                        paint.setColor(selectedColor);
//                        dialog.dismiss();
//                        invalidate();
//                    }
//                })
//                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        ((AppCompatActivity)getContext()).getWindow().getDecorView().setSystemUiVisibility(
//                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//                    }
//                })
//                .build()
//                .show();
//    }
//
//    private void addFabClicked() {
//        selectImage();
//    }
//
//    @Override
//    public boolean onMenuItemClick(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.pick_color_action:
//                colorButtonPressed();
//                break;
//            case R.id.zoom_in_action:
//                zoomInPressed();
//                break;
//            case R.id.zoom_out_action:
//                zoomOutPresssed();
//                break;
//            case R.id.sort_layer_up_action:
//                layerUpPressed();
//                break;
//            case R.id.sort_layer_down_action:
//                layerDownPressed();
//                break;
//        }
//        return true;
//    }
//
//
//    private class ScaleListener
//            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        @Override
//        public boolean onScale(ScaleGestureDetector detector) {
//
//            mScaleFactor *= detector.getScaleFactor();
//            mScaleFactor = Math.max(0.9f, Math.min(mScaleFactor, 1.2f));
//            int imageIndex = getImageIndex();
//            if (imageIndex >= 0) {
//                Log.d(TAG,"mScaleFactor: "+mScaleFactor);
//                Bitmap originalBitmap = imageList.get(imageIndex).originalBitmap;
//                Bitmap resizedBitmap = imageList.get(imageIndex).resizedBitmap;
//                int newHeight = (int) (resizedBitmap.getHeight() * mScaleFactor);
//                int newWidth = (int) (resizedBitmap.getWidth() * mScaleFactor);
//                if (newHeight > 100 && newWidth > 100 && newHeight < screenHeight && newWidth < screenWidth) {
//                    imageList.get(imageIndex).resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
//                    imageList.get(imageIndex).xEdge = imageList.get(imageIndex).x + imageList.get(imageIndex).resizedBitmap.getWidth();
//                    imageList.get(imageIndex).yEdge = imageList.get(imageIndex).y + imageList.get(imageIndex).resizedBitmap.getHeight();
//                    invalidate();
//                }
//            }
//            return true;
//        }
//    }
//
//    private void zoom(String zoomChoice) {
//
//        if(zoomChoice.equals("in"))
//            mScaleFactor = 1.05f;
//        else
//            mScaleFactor = 0.95f;
//
//        int imageIndex = lastSelected;
//        if (imageIndex >= 0) {
//            Log.d(TAG,"mScaleFactor: "+mScaleFactor);
//            Bitmap originalBitmap = imageList.get(imageIndex).originalBitmap;
//            Bitmap resizedBitmap = imageList.get(imageIndex).resizedBitmap;
//            int newHeight = (int) (resizedBitmap.getHeight() * mScaleFactor);
//            int newWidth = (int) (resizedBitmap.getWidth() * mScaleFactor);
//            //Log.d(TAG,"orig height: "+resizedBitmap.getHeight()+" new Height: "+newHeight);
//            //Log.d(TAG,"orig width: "+resizedBitmap.getWidth()+" new width: "+newWidth);
//            if (newHeight > 100 && newWidth > 100) {
//                        /*if(newHeight > screenHeight)
//                            newHeight = screenHeight;
//                        if(newWidth > screenWidth)
//                            newWidth = screenWidth;*/
//                Log.d(TAG,"orig height: "+resizedBitmap.getHeight()+" final new Height: "+newHeight);
//                imageList.get(imageIndex).resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
//                imageList.get(imageIndex).xEdge = imageList.get(imageIndex).x + imageList.get(imageIndex).resizedBitmap.getWidth();
//                imageList.get(imageIndex).yEdge = imageList.get(imageIndex).y + imageList.get(imageIndex).resizedBitmap.getHeight();
//                invalidate();
//            }
//        }
//    }
//
//    @Override
//    public void onDraw(Canvas canvas) {
//        canvas.drawPaint(paint);
//        for(CollageImage imgObj:imageList) {
//            canvas.drawBitmap(imgObj.resizedBitmap, imgObj.x, imgObj.y, paint);
//        }
//        ((AppCompatActivity)getContext()).getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//    }
}
