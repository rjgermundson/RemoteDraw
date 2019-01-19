package com.example.riley.piplace.BoardActivity.PlayBoard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * This class represents a ViewGroup consisting of a view
 * set on top of a lower view
 * This covering view allows for zooming and dragging of the
 * lower view
 */
public class BoardHolder extends LinearLayout {
    private DrawBoard drawBoard;
    private float prevPinchDistance;  // -1 if no pinch occurring

    public BoardHolder(Context context) {
        super(context);
        this.drawBoard = new DrawBoard(context);
    }

    public BoardHolder(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.drawBoard = new DrawBoard(context, attrs);
    }

    public BoardHolder(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.drawBoard = new DrawBoard(context, attrs);
    }

    public BoardHolder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.drawBoard = new DrawBoard(context, attrs);
    }

    /**
     * Defines the bitmap that will be used for this board
     * @param image The image to be used for this board
     */
    public void setImage(Bitmap image) {
        drawBoard.setImageBitmap(image);
        this.invalidate();
        drawBoard.invalidate();
    }

    /**
     * Sets the touch listener for the image in the board
     * @param onTouchListener Touch listener for image in the board
     */
    @SuppressLint("ClickableViewAccessibility")
    public void setImageListener(OnTouchListener onTouchListener) {
        drawBoard.setOnTouchListener(onTouchListener);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        validate();
    }

 @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        System.out.println(event.getPointerCount());
        if (event.getPointerCount() == 1) {
            prevPinchDistance = -1;
        }
        if (event.getPointerCount() >= 2) {
            // Multiple fingers on screen
            System.out.println("TWO POINTERS");
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            // Dragging to move around screen
            System.out.println("DRAG");
            return true;
        }
        return true;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            // Zooming
            if (onScreen(event, 0) && onScreen(event, 1)) {
                float distance = distance(event.getX(0),
                                        event.getY(0),
                                        event.getX(1),
                                        event.getY(1));
                if (prevPinchDistance >= 0) {
                    // Previous distance is from current state
                    double pinchChange = (double) distance / (double) prevPinchDistance;
                    drawBoard.setScale(pinchChange);
                }
                prevPinchDistance = distance;
            }
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE){
            // Dragging
            System.out.println("MOVING");
            return true;
        }
        return true;
    }

    /**
     * Determines the absolute distance between two points
     * @param xOne X coordinate of first point
     * @param yOne Y coordinate of first point
     * @param xTwo X coordinate of second point
     * @param yTwo Y coordinate of second point
     * @return Distance between two points
     */
    private float distance(float xOne, float yOne, float xTwo, float yTwo) {
        float xDiff = xTwo - xOne;
        float yDiff = yTwo - yOne;
        return Math.round(Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2)));
    }

    /**
     * Determines whether the current event is on screen
     * @param event The event to check
     * @param ptrID The id for the pointer to check
     * @return True if the event is in the screen
     *         False otherwise
     */
    private boolean onScreen(MotionEvent event, int ptrID) {
        float x = event.getX(ptrID);
        float y = event.getY(ptrID);
        boolean inWidth = (x > 0) && (x < getMeasuredWidth());
        boolean inHeight = (y > 0) && (y < getMeasuredHeight());
        return inWidth && inHeight;
    }

    /**
     * Sets up the DrawingBoard as a child of this view
     */
    private void validate() {
        if (drawBoard.getParent() == null) {
            drawBoard.setBackgroundColor(Color.WHITE);
            LayoutParams layoutParams = new LayoutParams(getMeasuredWidth(), getMeasuredWidth());
            layoutParams.gravity = Gravity.CENTER;
            this.addView(drawBoard, layoutParams);
        }
    }

    /**
     * This object represents the board that is held in the board holder
     * It is the image that is shown on the board
     */
    private class DrawBoard extends android.support.v7.widget.AppCompatImageView {
        private float scale;
        private int width;
        private int height;

        private DrawBoard(Context c) {
            super(c);
            this.scale = 1.0f;
        }

        private DrawBoard(Context c, AttributeSet attributeSet) {
            super(c, attributeSet);
            this.scale = 1.0f;
        }

        /**
         * Changes the scalar of each axis of the image
         * @param pinchChange The amount of relative change the axes will have
         */
        private void setScale(double pinchChange) {
            this.scale *= pinchChange;
            this.setScaleX(scale);
            this.setScaleY(scale);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
