package com.drewmahrt.collageapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by drewmahrt on 1/2/17.
 */

public class ImageUtility {
    public static Bitmap resizeImageForImageView(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap resizedBitmap = null;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        float multFactor = -1.0F;
        if(newWidth > newHeight){
            multFactor = (float) originalWidth / (float) originalHeight;
            newWidth = (int) (newHeight * multFactor);
        } else if(newHeight > newWidth){
            multFactor = (float) originalHeight / (float) originalWidth;
            newHeight = (int) (newWidth * multFactor);
        }

        resizedBitmap = Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,false);
        return resizedBitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
