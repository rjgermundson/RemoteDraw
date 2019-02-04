package com.example.riley.piplace.BoardActivity.PlayBoard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import com.example.riley.piplace.BoardActivity.BoardActivity;

/**
 * This class represents a ViewGroup consisting of a view
 * set on top of a lower view
 * This covering view allows for zooming and dragging of the
 * lower view
 */
public class BoardHolder extends LinearLayout {
    private static final long CLICK_TIMEOUT =
            Double.valueOf(ViewConfiguration.getDoubleTapTimeout() / 1.3).longValue();
    private static final float ACCELERATION = 1.3f;

    private DrawBoard drawBoard;
    private boolean zooming = false;
    private float prevPinchDistance = -1;  // -1 if no pinch occurring
    private long lastClickTime = -1;  // -1 Most recent was double click

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

    /**
     * Defines the bitmap that will be used for this board
     * Sets the dimensions of the board based on the bitmap dimensions
     * @param image The image to be used for this board
     */
    public void setImage(Bitmap image) {
        drawBoard.setImageBitmap(image);
        int margin = (this.getMeasuredWidth() - image.getWidth()) / 2;
        LayoutParams layoutParams = new LayoutParams(image.getWidth(), image.getHeight());
        layoutParams.setMargins(margin, 0, margin, 0);
        layoutParams.gravity = Gravity.CENTER;
        drawBoard.setLayoutParams(layoutParams);
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
        long currClickTime = System.currentTimeMillis();
        long timeDiff = currClickTime - lastClickTime;
        this.lastClickTime = currClickTime;
        if (event.getPointerCount() <= 1) {
            zooming = false;
            prevPinchDistance = -1;
        }
        if (timeDiff < CLICK_TIMEOUT) {
            // Double click should be handled by DrawBoard
            lastClickTime = Integer.MIN_VALUE;
            return false;
        }
        if (!BoardActivity.isDrag) {
            return false;
        }
        if (event.getPointerCount() >= 2) {
            // Multiple fingers on screen
            zooming = true;
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                   && !zooming) {
            // Dragging to move around screen
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
                    float prevScale = drawBoard.getScaleX();
                    drawBoard.setScale(pinchChange);
                    float diff = prevScale * drawBoard.getMeasuredWidth() - drawBoard.getScaleX() * drawBoard.getMeasuredWidth();
                    translate(drawBoard, Math.round(diff / 4), Math.round(diff / 4));
                }
                prevPinchDistance = distance;
            }
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                   && !zooming){
            // Dragging
            float deltaX = 0;
            float deltaY = 0;
            if (event.getHistorySize() > 0) {
                deltaX = event.getX() - event.getHistoricalX(event.getActionIndex());
                deltaY = event.getY() - event.getHistoricalY(event.getActionIndex());
            }
            translate(drawBoard, Math.round(deltaX * ACCELERATION), Math.round(deltaY * ACCELERATION));
            return true;
        }
        return true;
    }

    /**
     * Translates the given view
     * @param view View to translate
     * @param deltaX Change in x direction (positive right)
     * @param deltaY Change in y direction (positive down)
     */
    private void translate(View view, int deltaX, int deltaY) {
        int widthExcess = Math.round(drawBoard.getScaleX() * drawBoard.getMeasuredWidth() - drawBoard.getMeasuredWidth());
        int heightExcess = Math.round(drawBoard.getScaleY() * drawBoard.getMeasuredHeight() - drawBoard.getMeasuredHeight());
        int widthMargin = (this.getMeasuredWidth() - drawBoard.getMeasuredWidth()) / 2;
        int heightMargin = (this.getMeasuredHeight() - drawBoard.getMeasuredHeight()) / 2;
        view.setLeft(median(-widthExcess + widthMargin,
                            drawBoard.getLeft() + deltaX ,
                            widthExcess + widthMargin));
        view.setTop(median(-heightExcess + heightMargin,
                           drawBoard.getTop() + deltaY,
                           heightExcess + heightMargin));
        view.setRight(drawBoard.getLeft() + drawBoard.getMeasuredWidth());
        view.setBottom(drawBoard.getTop() + drawBoard.getMeasuredHeight());
    }

    /**
     * Returns the median value of a, b and c
     * @return Median of given values
     */
    private int median(int a, int b, int c) {
        if ((a <= b && b <= c)
           || (a >= b && b >= c)) {
            return b;
        } else if ((a <= c && c <= b)
                   || (a >= c && c >= b)) {
            return c;
        }
        return a;
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
            int minDimension = Math.min(this.getMeasuredWidth(), this.getMeasuredHeight());
            LayoutParams layoutParams = new LayoutParams(minDimension, minDimension);
            layoutParams.gravity = Gravity.CENTER;
            this.addView(drawBoard, layoutParams);
        }
    }

    /**
     * This object represents the board that is held in the board holder
     * It is the image that is shown on the board
     */
    private class DrawBoard extends android.support.v7.widget.AppCompatImageView {
        private static final float MAX_SCALE = 10.0f;
        private static final float MIN_SCALE = 1.0f;
        private float scale;

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
            this.scale = Math.max(this.scale, MIN_SCALE);
            this.scale = Math.min(this.scale, MAX_SCALE);
            this.setScaleX(scale);
            this.setScaleY(scale);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
