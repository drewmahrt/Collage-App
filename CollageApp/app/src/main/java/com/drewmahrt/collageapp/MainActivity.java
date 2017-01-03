package com.drewmahrt.collageapp;

import android.app.Activity;
import android.content.ContentValues;
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
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CollageActivity";
    private static final int PICTURE_GALLERY = 1;
    private FloatingActionButton addFab;
    private MenuItem closeButton, saveButton, sortUpButton, sortDownButton, colorButton, zoomOutButton, zoomInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collage_creator);

        ActionMenuView bottomBar = (ActionMenuView)findViewById(R.id.amvMenu);
        Menu bottomMenu = bottomBar.getMenu();
        getMenuInflater().inflate(R.menu.collage_menu,bottomMenu);

        addFab = (FloatingActionButton)findViewById(R.id.addFab);
        closeButton = bottomMenu.findItem(R.id.close_image_action);
        saveButton = bottomMenu.findItem(R.id.save_action);
        sortUpButton = bottomMenu.findItem(R.id.sort_layer_up_action);
        sortDownButton = bottomMenu.findItem(R.id.sort_layer_down_action);
        colorButton = bottomMenu.findItem(R.id.pick_color_action);
        zoomOutButton = bottomMenu.findItem(R.id.zoom_out_action);
        zoomInButton = bottomMenu.findItem(R.id.zoom_in_action);

        final Context alertContext = this;
        closeButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
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
        final CollageContainer dv= new CollageContainer(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, R.id.toolbar_bottom);
        rl.addView(dv, params);

        //Fix to bring FAB on top for 4.x devices
        addFab.bringToFront();

        saveButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                dv.setDrawingCacheEnabled(true);
                Bitmap bitmap = dv.getDrawingCache();
                Uri uri = addImageToGallery(getApplicationContext(), "Collage", "Collage");
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                    Toast.makeText(getApplicationContext(), "Image saved!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                }
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

        addFab.setOnClickListener(dv);
        colorButton.setOnMenuItemClickListener(dv);
        zoomInButton.setOnMenuItemClickListener(dv);
        zoomOutButton.setOnMenuItemClickListener(dv);
        sortDownButton.setOnMenuItemClickListener(dv);
        sortUpButton.setOnMenuItemClickListener(dv);

        //Trigger tutorial
        new MaterialShowcaseView.Builder(this)
                .setTarget(addFab)
                .setDismissText("GOT IT")
                .setDismissOnTouch(true)
                .setContentText("Click here to add new photos to your collage. \n\nPlace your finger on a photo and drag to move it. \n\nHold your finger on a picture without moving to delete it.")
                .singleUse("PICTURE DETAILS") // provide a unique ID used to ensure it is only shown once
                .show();
    }

    public void addFabClicked(CollageContainer container){

    }

    public Uri addImageToGallery(Context context, String title, String description) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
