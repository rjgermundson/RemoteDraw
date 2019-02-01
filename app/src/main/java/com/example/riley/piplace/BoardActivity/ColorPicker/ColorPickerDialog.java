package com.example.riley.piplace.BoardActivity.ColorPicker;

import android.app.DialogFragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;

import com.example.riley.piplace.BoardActivity.BoardActivity;
import com.example.riley.piplace.R;

public class ColorPickerDialog extends DialogFragment {
    private static ColorPickerDialog instance;
    private static ImageButton colorWheel;
    private static final int CIRCLE_RADIUS = 50;

    private static int[] colors;

    public ColorPickerDialog() {}

    public static ColorPickerDialog newInstance(Context context, ImageButton wheel) {
        if (context == null) {
            if (instance == null) {
                return null;
            }
        }
        try {
            TypedArray typedArray = context.getResources().obtainTypedArray(R.array.colors);
            colors = new int[typedArray.length()];
            for (int i = 0; i < typedArray.length(); i++) {
                colors[i] = typedArray.getColor(i, 0);
            }
        } catch (NullPointerException e) {
            return null;
        }
        instance = new ColorPickerDialog();
        colorWheel = wheel;
        return instance;
    }

    public static ColorPickerDialog getInstance() {
        return instance;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstance) {
        setWindowProperties();
        View view = inflater.inflate(R.layout.color_picker, parent);
        GridLayout gridLayout = view.findViewById(R.id.color_picker_list);
        gridLayout.setRowCount(4);
        gridLayout.setColumnCount(4);
        for (int i = 0; i < colors.length; i++) {
            ImageButton button = getColorButton(inflater, colors[i]);
            gridLayout.addView(button, i);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private ImageButton getColorButton(LayoutInflater inflater, final int color) {
        ImageButton button = (ImageButton) inflater.inflate(R.layout.color_button, null);
        Bitmap buttonImage = Bitmap.createBitmap(CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(buttonImage);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawCircle(CIRCLE_RADIUS, CIRCLE_RADIUS, CIRCLE_RADIUS, paint);
        button.setImageBitmap(buttonImage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int width = colorWheel.getMeasuredWidth();
                Bitmap wheelBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
                Canvas wheelCanvas = new Canvas(wheelBitmap);
                Paint wheelPaint = new Paint();
                wheelPaint.setColor(color);
                wheelCanvas.drawCircle(width / 2, width / 2, width / 2, wheelPaint);
                colorWheel.setImageBitmap(wheelBitmap);
                BoardActivity.setColor(color);
                getDialog().dismiss();
            }
        });
        return button;
    }

    private void setWindowProperties() {
        Window window = instance.getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(params);
    }
}
