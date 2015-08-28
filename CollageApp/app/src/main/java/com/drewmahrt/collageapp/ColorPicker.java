package com.drewmahrt.collageapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * This is the only class of the project. Consist in a costum dialog that show
 * the GUI for choose the color.
 *
 * @author Simone Pessotto
 *
 */
public class ColorPicker extends Dialog implements SeekBar.OnSeekBarChangeListener {

    public Activity c;
    public Dialog d;

    View colorView;
    SeekBar redSeekBar, greenSeekBar, blueSeekBar;
    TextView redToolTip, greenToolTip, blueToolTip;
    private int red, green, blue;
    int seekBarLeft;
    Rect thumbRect;


    /**
     * Creator of the class. It will initialize the class with black color as default
     * @param a The reference to the activity where the color picker is called
     */
    public ColorPicker(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.red = 0;
        this.green = 0;
        this.blue = 0;
    }


    /**
     * Creator of the class. It will initialize the class with the rgb color passed as default
     *
     * @param a The reference to the activity where the color picker is called
     * @param r Red color for RGB values (0 - 255)
     * @param g Green color for RGB values (0 - 255)
     * @param b Blue color for RGB values (0 - 255)
     *
     * If the value of the colors it's not in the right range (0 - 255) it will be place at 0.
     */
    public ColorPicker(Activity a, int r, int g, int b) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;

        if(0 <= r && r <=255)
            this.red = r;
        else
            this.red = 0;

        if(0 <= r && r <=255)
            this.green = g;
        else
            this.green = 0;

        if(0 <= r && r <=255)
            this.blue = b;
        else
            this.green = 0;

    }

    /**
     * Simple onCreate function. Here there is the init of the GUI.
     *
     * @param savedInstanceState As usual ...
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setContentView(com.drewmahrt.collageapp.R.layout.materialcolorpicker__layout_color_picker);
        } else {
            setContentView(com.drewmahrt.collageapp.R.layout.materialcolorpicker__layout_color_picker_old_android);
        }

        colorView = findViewById(com.drewmahrt.collageapp.R.id.colorView);

        redSeekBar = (SeekBar)findViewById(com.drewmahrt.collageapp.R.id.redSeekBar);
        greenSeekBar = (SeekBar)findViewById(com.drewmahrt.collageapp.R.id.greenSeekBar);
        blueSeekBar = (SeekBar)findViewById(com.drewmahrt.collageapp.R.id.blueSeekBar);

        seekBarLeft = redSeekBar.getPaddingLeft();

        redToolTip = (TextView)findViewById(com.drewmahrt.collageapp.R.id.redToolTip);
        greenToolTip = (TextView)findViewById(com.drewmahrt.collageapp.R.id.greenToolTip);
        blueToolTip = (TextView)findViewById(com.drewmahrt.collageapp.R.id.blueToolTip);

        redSeekBar.setOnSeekBarChangeListener(this);
        greenSeekBar.setOnSeekBarChangeListener(this);
        blueSeekBar.setOnSeekBarChangeListener(this);

        redSeekBar.setProgress(red);
        greenSeekBar.setProgress(green);
        blueSeekBar.setProgress(blue);

        colorView.setBackgroundColor(Color.rgb(red, green, blue));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){

        thumbRect = redSeekBar.getThumb().getBounds();

        redToolTip.setX(seekBarLeft + thumbRect.left);
        if (red<10)
            redToolTip.setText("  "+red);
        else if (red<100)
            redToolTip.setText(" "+red);
        else
            redToolTip.setText(red+"");

        thumbRect = greenSeekBar.getThumb().getBounds();

        greenToolTip.setX(seekBarLeft + thumbRect.left);
        if (green<10)
            greenToolTip.setText("  "+green);
        else if (red<100)
            greenToolTip.setText(" "+green);
        else
            greenToolTip.setText(green+"");

        thumbRect = blueSeekBar.getThumb().getBounds();

        blueToolTip.setX(seekBarLeft + thumbRect.left);
        if (blue<10)
            blueToolTip.setText("  "+blue);
        else if (blue<100)
            blueToolTip.setText(" "+blue);
        else
            blueToolTip.setText(blue+"");

    }

    /**
     * Method called when the user change the value of the bars. This sync the colors.
     *
     * @param seekBar SeekBar that has changed
     * @param progress The new progress value
     * @param fromUser If it coem from User
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (seekBar.getId() == com.drewmahrt.collageapp.R.id.redSeekBar) {

            red = progress;
            thumbRect = seekBar.getThumb().getBounds();

            redToolTip.setX(seekBarLeft + thumbRect.left);

            if (progress<10)
                redToolTip.setText("  " + red);
            else if (progress<100)
                redToolTip.setText(" "+red);
            else
                redToolTip.setText(red+"");

        }
        else if (seekBar.getId() == com.drewmahrt.collageapp.R.id.greenSeekBar) {

            green = progress;
            thumbRect = seekBar.getThumb().getBounds();

            greenToolTip.setX(seekBar.getPaddingLeft()+thumbRect.left);
            if (progress<10)
                greenToolTip.setText("  "+green);
            else if (progress<100)
                greenToolTip.setText(" "+green);
            else
                greenToolTip.setText(green+"");

        }
        else if (seekBar.getId() == com.drewmahrt.collageapp.R.id.blueSeekBar) {

            blue = progress;
            thumbRect = seekBar.getThumb().getBounds();

            blueToolTip.setX(seekBarLeft + thumbRect.left);
            if (progress<10)
                blueToolTip.setText("  "+blue);
            else if (progress<100)
                blueToolTip.setText(" "+blue);
            else
                blueToolTip.setText(blue+"");

        }

        colorView.setBackgroundColor(Color.rgb(red, green, blue));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }


    /**
     * Getter for the RED value of the RGB selected color
     * @return RED Value Integer (0 - 255)
     */
    public int getRed() {
        return red;
    }

    /**
     * Getter for the GREEN value of the RGB selected color
     * @return GREEN Value Integer (0 - 255)
     */
    public int getGreen() {
        return green;
    }


    /**
     * Getter for the BLUE value of the RGB selected color
     * @return BLUE Value Integer (0 - 255)
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Getter for the color as Android Color class value.
     *
     * From Android Reference: The Color class defines methods for creating and converting color ints.
     * Colors are represented as packed ints, made up of 4 bytes: alpha, red, green, blue.
     * The values are unpremultiplied, meaning any transparency is stored solely in the alpha
     * component, and not in the color components.
     *
     * @return Selected color as Android Color class value.
     */
    public int getColor(){
        return Color.rgb(red, green, blue);
    }
}