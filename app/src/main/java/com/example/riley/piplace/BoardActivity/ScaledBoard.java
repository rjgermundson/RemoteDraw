package com.example.riley.piplace.BoardActivity;

import android.content.Context;
import android.util.AttributeSet;

public class ScaledBoard extends android.support.v7.widget.AppCompatImageView {
    private double ratio = 1.0;
    private double scale = 1.0;
    private int width;
    private int height;

    public ScaledBoard(Context c) {
        super(c);
    }

    public ScaledBoard(Context c, AttributeSet attributeSet) {
        super(c, attributeSet);
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setWidth(int width) {
        this.width = width;
        this.height = (int) Math.round(width * ratio);
        System.out.println("WIDTH " + width);
        setMeasuredDimension((int) Math.round(width * scale), (int) Math.round(height * scale));
        System.out.println("MEASURED WIDTH " + getMeasuredWidth());
    }

    public int getDefinedWidth() {
        return (int) Math.round(width * scale);
    }

    public int getDefinedHeight() {
        return (int) Math.round(height * scale);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
