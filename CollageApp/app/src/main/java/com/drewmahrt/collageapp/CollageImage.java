package com.drewmahrt.collageapp;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by drewmahrt on 1/2/17.
 */

public class CollageImage {
    private static final String TAG = "CollageImage";
    public float x, y, xEdge, yEdge;
    public Bitmap originalBitmap;
    public Bitmap resizedBitmap;

    public CollageImage(Bitmap orig, Bitmap resized, int width, int height){
        x = width*0.4f;
        y = height*0.4f;
        xEdge = x+resized.getWidth();
        yEdge = y+resized.getHeight();
        this.originalBitmap = orig;
        this.resizedBitmap = resized;
    }
}
