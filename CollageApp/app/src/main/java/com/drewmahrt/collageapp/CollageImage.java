package com.drewmahrt.collageapp;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

/**
 * Created by drewmahrt on 1/2/17.
 */

public class CollageImage {
    private static final String TAG = "CollageImage";
//    public float x, y, xEdge, yEdge;
//    public Bitmap originalBitmap;
//    public Bitmap resizedBitmap;
//
//    public CollageImage(Bitmap orig, Bitmap resized, int width, int height){
//        x = width*0.4f;
//        y = height*0.4f;
//        xEdge = x+resized.getWidth();
//        yEdge = y+resized.getHeight();
//        this.originalBitmap = orig;
//        this.resizedBitmap = resized;
//    }

    private float mWidth, mHeight;
    private int mId;
    private Uri mImageUri;

    public CollageImage(float width, float height, Uri imageUri, int id) {
        mWidth = width;
        mHeight = height;
        mImageUri = imageUri;
        mId = id;
    }

    public float getWidth() {
        return mWidth;
    }

    public void setWidth(float width) {
        mWidth = width;
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float height) {
        mHeight = height;
    }

    public Uri getImageUri() {
        return mImageUri;
    }

    public void setImageUri(Uri imageUri) {
        mImageUri = imageUri;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }
}
