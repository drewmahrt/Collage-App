package com.drewmahrt.collageapp;

import android.net.Uri;

public class CollageImage {
    private float mWidth, mHeight;
    private float mScale;
    private int mId;
    private Uri mImageUri;

    public CollageImage(float width, float height, Uri imageUri, int id) {
        mWidth = width;
        mHeight = height;
        mImageUri = imageUri;
        mId = id;
        mScale = 1f;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        mScale = scale;
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
