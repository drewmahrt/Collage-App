package com.drewmahrt.collageapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class ImageUtility {
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
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
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static void saveCollage(AppCompatActivity activity, FrameLayout collageContainer){


        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        }else {

            //remove selected image border
            if(collageContainer.getFocusedChild() != null) {
                collageContainer.getFocusedChild().setBackgroundResource(R.drawable.unselected_border);
            }

            //Find the view we are after
            View view = (View) activity.findViewById(R.id.collage_container);
            //Create a Bitmap with the same dimensions
            Bitmap image = Bitmap.createBitmap(collageContainer.getWidth(),
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

                MediaStore.Images.Media.insertImage(activity.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                Toast.makeText(activity, "Collage saved!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
