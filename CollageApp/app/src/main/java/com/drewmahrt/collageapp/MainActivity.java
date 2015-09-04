package com.drewmahrt.collageapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CollageActivity";
    private static final int PICTURE_GALLERY = 1;
    private FloatingActionButton addFab;
    private MenuItem closeButton, saveButton, sortUpButton, sortDownButton, colorButton, zoomOutButton, zoomInButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collage_creator);
        addFab = (FloatingActionButton)findViewById(R.id.addFab);
        ActionMenuView bottomBar = (ActionMenuView)findViewById(R.id.amvMenu);
        Menu bottomMenu = bottomBar.getMenu();
        getMenuInflater().inflate(R.menu.collage_menu,bottomMenu);
        closeButton = bottomMenu.findItem(R.id.close_image_action);
        Log.d("BUTTONTEST","Close button: "+closeButton.getTitle());
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
        final DrawingView dv= new DrawingView(this);
        dv.setDrawingCacheEnabled(true);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, R.id.toolbar_bottom);
        rl.addView(dv, params);

        //Fix to bring FAB on top for 4.x devices
        addFab.bringToFront();

        saveButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Bitmap bitmap = dv.getDrawingCache();

                //  Get path to new gallery image
                Uri path = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                try {
                    OutputStream stream = getApplicationContext().getContentResolver().openOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    Toast.makeText(getApplicationContext(), "Image saved!", Toast.LENGTH_LONG).show();
                    finish();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "E: " + e.getMessage());
                }
                return true;
            }
        });
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

    class DrawingView extends View {
        private static final String TAG = "CollageActivity.DV";
        private ArrayList<ImageObject> imageList;
        private ScaleGestureDetector mScaleDetector;
        private float mScaleFactor = 1.f;
        private int screenWidth,screenHeight;
        private float currX,currY;
        private int lastSelected;
        private Paint paint;
        private ColorPicker cp;
        private boolean isPressedDown;

        public class ImageObject{
            private static final String TAG = "CollageActivity.IO";
            public float x, y, xEdge, yEdge;
            public Bitmap originalBitmap;
            public Bitmap resizedBitmap;

            public ImageObject(Bitmap orig,Bitmap resized){
                Log.d(TAG,"lastSelected: "+imageList.size());
                x = screenWidth*0.4f;
                y = screenHeight*0.4f;
                xEdge = x+resized.getWidth();
                yEdge = y+resized.getHeight();
                this.originalBitmap = orig;
                this.resizedBitmap = resized;
            }
        }

        public DrawingView(Context context) {
            super(context);
            mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
            if(imageList == null) {
                imageList = new ArrayList<ImageObject>();
            }

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
            currX = 0;
            currY = 0;
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            lastSelected = -1;
            isPressedDown = false;
            cp = new ColorPicker(MainActivity.this, 255, 255, 255);

            addFab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                   selectImage();
                }
            });

            colorButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Log.d(TAG, "clicked picker");
                    cp.show();

                    Button okColor = (Button)cp.findViewById(R.id.okColorButton);
                    okColor.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Or the android RGB Color (see the android Color class reference)
                            int red = cp.getRed();
                            int blue = cp.getBlue();
                            int green = cp.getGreen();
                            Log.d(TAG, "Current paint color(before): " + paint.getColor());
                            Log.d(TAG, "Color chosen: " + red + " " + blue + " " + green);
                            paint.setARGB(255, red, green, blue);
                            cp.dismiss();
                            invalidate();
                        }
                    });
                    return true;
                }
            });

            zoomOutButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    zoom("out");
                    return true;
                }
            });

            zoomInButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    zoom("in");
                    return true;
                }
            });

            sortDownButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Log.d(TAG,"Sort down with "+lastSelected);
                    if(lastSelected > 0){
                        ImageObject temp = imageList.remove(lastSelected);
                        imageList.add(lastSelected-1,temp);
                        Log.d(TAG, "Down: Moving from " + lastSelected + " to " + (lastSelected - 1));
                        lastSelected--;
                        invalidate();
                    }
                    return true;
                }
            });

            sortUpButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Log.d(TAG,"Sort up with "+lastSelected);
                    if(lastSelected >= 0 && lastSelected < imageList.size()-1){
                        ImageObject temp = imageList.remove(lastSelected);
                        imageList.add(lastSelected+1,temp);
                        Log.d(TAG, "Up: Moving from " + lastSelected + " to " + (lastSelected + 1));
                        lastSelected++;
                        invalidate();
                    }
                    return true;
                }
            });
        }



        public void selectImage(){
            final FragmentManager fm = ((FragmentActivity) getContext()).getSupportFragmentManager();
            Fragment auxiliary = new Fragment() {
                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent data) {
                    //DO WHATEVER YOU NEED
                    super.onActivityResult(requestCode, resultCode, data);
                    fm.beginTransaction().remove(this).commit();
                    if(requestCode == PICTURE_GALLERY && resultCode == Activity.RESULT_OK && data != null){
                        Log.d(TAG,"data: "+data.getData());
                        Uri selectedImageUri = handleImageUri(data.getData());
                        Log.d(TAG, "URI1: " + selectedImageUri);
                        selectedImageUri = Uri.parse(Uri.decode(selectedImageUri.toString()));

                        Log.d(TAG,"URI2: "+selectedImageUri);

                        if(selectedImageUri.toString().contains("com.google.android.apps.photos")) {
                            Toast.makeText(getApplicationContext(), "Only local media works right now, sorry!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String selectedImagePath = getPath(selectedImageUri);

                        Log.d(TAG, "path: " + selectedImagePath);

                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(selectedImagePath,options);


                        options.inSampleSize = calculateInSampleSize(options,screenWidth,screenHeight);
                        options.inJustDecodeBounds = false;


                        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
                        imageList.add(new ImageObject(bitmap, resizeImageForImageView(BitmapFactory.decodeFile(selectedImagePath, options), (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4))));
                        lastSelected = imageList.size()-1;

                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    }
                    invalidate();
                }
            };
            fm.beginTransaction().add(auxiliary, "FRAGMENT_TAG").commit();
            fm.executePendingTransactions();

            auxiliary.startActivityForResult(new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI),PICTURE_GALLERY);
        }

        public Uri handleImageUri(Uri uri) {
            Pattern pattern = Pattern.compile("(content://media/.*\\d)(/ACTUAL.*)");
            if (uri.getPath().contains("content")) {
                Matcher matcher = pattern.matcher(uri.getPath());
                if (matcher.find())
                    return Uri.parse(matcher.group(1));
                else
                    throw new IllegalArgumentException("Cannot handle this URI");
            } else
                return uri;
        }

        public Bitmap resizeImageForImageView(Bitmap bitmap, int newWidth, int newHeight){
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

        public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
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

        public String getPath(Uri uri){
            if(uri == null){
                return null;
            }

            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getApplicationContext().getContentResolver().query(uri,projection,null,null,null);
            if(cursor != null){
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
            return uri.getPath();
        }

        public int getImageIndex(){
            if(isPressedDown && lastSelected != -1)
                return lastSelected;
            int currIndex = -1;
            for (ImageObject img:imageList) {
                if(currX >= img.x && currX <= img.xEdge && currY >= img.y && currY <= img.yEdge)
                    currIndex = imageList.indexOf(img);
            }
            lastSelected = currIndex;
            return currIndex;
        }

        public boolean onTouchEvent(MotionEvent event) {
            currX = event.getX();
            currY = event.getY();
            int imageIndex = getImageIndex();
            //mScaleDetector.onTouchEvent(event);
            float newX,newY;
            if(imageIndex >= 0) {
                gestureDetector.onTouchEvent(event);
                if(lastSelected == -1)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                    }
                    break;

                    case MotionEvent.ACTION_MOVE:
                        if(!isPressedDown)
                            isPressedDown = true;
                        newX = (float) (currX - (0.5 * imageList.get(imageIndex).resizedBitmap.getWidth()));
                        newY = (float) (currY - (0.5 * imageList.get(imageIndex).resizedBitmap.getHeight()));
                        if (newX < 0) newX = 0;
                        if (newX + imageList.get(imageIndex).resizedBitmap.getWidth() > screenWidth)
                            newX = screenWidth - imageList.get(imageIndex).resizedBitmap.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + imageList.get(imageIndex).resizedBitmap.getHeight() > screenHeight)
                            newY = screenHeight - imageList.get(imageIndex).resizedBitmap.getHeight();
                        imageList.get(imageIndex).x = newX;
                        imageList.get(imageIndex).y = newY;
                        imageList.get(imageIndex).xEdge = imageList.get(imageIndex).x + imageList.get(imageIndex).resizedBitmap.getWidth();
                        imageList.get(imageIndex).yEdge = imageList.get(imageIndex).y + imageList.get(imageIndex).resizedBitmap.getHeight();
                        invalidate();

                        break;
                    case MotionEvent.ACTION_UP:
                        newX = (float) (currX - (0.5 * imageList.get(imageIndex).resizedBitmap.getWidth()));
                        newY = (float) (currY - (0.5 * imageList.get(imageIndex).resizedBitmap.getHeight()));
                        if (newX < 0) newX = 0;
                        if (newX + imageList.get(imageIndex).resizedBitmap.getWidth() > screenWidth)
                            newX = screenWidth - imageList.get(imageIndex).resizedBitmap.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY + imageList.get(imageIndex).resizedBitmap.getHeight() > screenHeight)
                            newY = screenHeight - imageList.get(imageIndex).resizedBitmap.getHeight();
                        imageList.get(imageIndex).x = newX;
                        imageList.get(imageIndex).y = newY;
                        imageList.get(imageIndex).xEdge = imageList.get(imageIndex).x + imageList.get(imageIndex).resizedBitmap.getWidth();
                        imageList.get(imageIndex).yEdge = imageList.get(imageIndex).y + imageList.get(imageIndex).resizedBitmap.getHeight();
                        isPressedDown = false;
                        invalidate();
                        break;
                }
            }
            return true;
        }

        final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(),new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                imageList.remove(lastSelected);
                lastSelected = -1;
                invalidate();
            }
        });

        private class ScaleListener
                extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {

                mScaleFactor *= detector.getScaleFactor();
                mScaleFactor = Math.max(0.9f, Math.min(mScaleFactor, 1.2f));
                int imageIndex = getImageIndex();
                if (imageIndex >= 0) {
                    Log.d(TAG,"mScaleFactor: "+mScaleFactor);
                    Bitmap originalBitmap = imageList.get(imageIndex).originalBitmap;
                    Bitmap resizedBitmap = imageList.get(imageIndex).resizedBitmap;
                    int newHeight = (int) (resizedBitmap.getHeight() * mScaleFactor);
                    int newWidth = (int) (resizedBitmap.getWidth() * mScaleFactor);
                    if (newHeight > 100 && newWidth > 100 && newHeight < screenHeight && newWidth < screenWidth) {
                        imageList.get(imageIndex).resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
                        imageList.get(imageIndex).xEdge = imageList.get(imageIndex).x + imageList.get(imageIndex).resizedBitmap.getWidth();
                        imageList.get(imageIndex).yEdge = imageList.get(imageIndex).y + imageList.get(imageIndex).resizedBitmap.getHeight();
                        invalidate();
                    }
                }
                return true;
            }
        }

        private void zoom(String zoomChoice) {

                if(zoomChoice.equals("in"))
                    mScaleFactor = 1.05f;
                else
                    mScaleFactor = 0.95f;

                int imageIndex = lastSelected;
                if (imageIndex >= 0) {
                    Log.d(TAG,"mScaleFactor: "+mScaleFactor);
                    Bitmap originalBitmap = imageList.get(imageIndex).originalBitmap;
                    Bitmap resizedBitmap = imageList.get(imageIndex).resizedBitmap;
                    int newHeight = (int) (resizedBitmap.getHeight() * mScaleFactor);
                    int newWidth = (int) (resizedBitmap.getWidth() * mScaleFactor);
                    if (newHeight > 100 && newWidth > 100 && newHeight < screenHeight && newWidth < screenWidth) {
                        imageList.get(imageIndex).resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
                        imageList.get(imageIndex).xEdge = imageList.get(imageIndex).x + imageList.get(imageIndex).resizedBitmap.getWidth();
                        imageList.get(imageIndex).yEdge = imageList.get(imageIndex).y + imageList.get(imageIndex).resizedBitmap.getHeight();
                        invalidate();
                    }
                }
        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawPaint(paint);
            for(ImageObject imgObj:imageList) {
                canvas.drawBitmap(imgObj.resizedBitmap, imgObj.x, imgObj.y, paint);
            }
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
