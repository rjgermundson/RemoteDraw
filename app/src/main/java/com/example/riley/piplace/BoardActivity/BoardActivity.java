package com.example.riley.piplace.BoardActivity;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.riley.piplace.BoardActivity.ColorPicker.ColorPickerDialog;
import com.example.riley.piplace.BoardActivity.PlayBoard.BoardHolder;
import com.example.riley.piplace.R;

public class BoardActivity extends AppCompatActivity {
    public static final int MESSAGE_REFRESH_BOARD = 20;
    public static final int MESSAGE_SET_BOARD = 21;
    public static final int BOARD_PIXEL_WIDTH = 256;
    public static final int BOARD_PIXEL_HEIGHT = 256;
    public static UpdateBoardHandler updateHandler;  // Todo: Make private and pass to ReadTask and AddPixelListener
    public static boolean isDrag = true;
    static int color = Color.RED;

    /**
     * Set the current color for a board to newColor
     * @param newColor Color to use
     */
    public static void setColor(int newColor) {
        color = newColor;
    }

    /**
     * Get the current color for a board
     * @return The current color a board uses
     */
    public static int getColor() {
        return color;
    }

    /**
     * Alerts this activity that the connection is closed
     */
    public void close() {
        Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Creates the current hosts image that will be used for the board state
     * @param clientBoard Image whose dimensions will be used to construct the board's bitmap
     * @return A bitmap that will be used to represent the board state
     */
    Bitmap getStartBoard(View clientBoard) {
        int imageWidth = (clientBoard.getMeasuredWidth() / BOARD_PIXEL_WIDTH) * BOARD_PIXEL_WIDTH;
        int imageHeight = (clientBoard.getMeasuredWidth() / BOARD_PIXEL_HEIGHT) * BOARD_PIXEL_HEIGHT;
        return Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
    }

    void setColorWheelListener() {
        final ImageButton colorWheel = findViewById(R.id.color_wheel);
        ViewTreeObserver colorWheelObserver = colorWheel.getViewTreeObserver();
        if (colorWheelObserver.isAlive()) {
            colorWheelObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    colorWheel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Bitmap buttonImage = Bitmap.createBitmap(colorWheel.getMeasuredWidth(),
                                                             colorWheel.getMeasuredHeight(),
                                                             Bitmap.Config.ARGB_8888);
                    drawButton(buttonImage);
                    colorWheel.setImageBitmap(buttonImage);
                    colorWheel.setOnClickListener(new ChangeColorListener());
                }
            });
        }
    }

    /**
     * Draws the current color the client is using
     * @param image The image to use for the button
     */
    void drawButton(Bitmap image) {
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawCircle(image.getWidth() / 2, image.getHeight() / 2,
                          image.getWidth() / 2, paint);
    }

    class ChangeColorListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            DialogFragment picker;
            picker = ColorPickerDialog.getInstance();
            if (picker == null) {
                picker = ColorPickerDialog.newInstance(getApplicationContext(), (ImageButton) findViewById(R.id.color_wheel));
            }
            if (picker != null) {
                picker.show(getFragmentManager(), "Picker");
            }
        }
    }

    public static class UpdateBoardHandler extends Handler {
        private static UpdateBoardHandler instance;
        BoardHolder boardHolder;

        private UpdateBoardHandler(BoardHolder boardHolder) {
            this.boardHolder = boardHolder;
        }

        /**
         * Initialize the BoardUpdateHandler
         * @param boardHolder BoardHolder to be in charge of updating
         * @return True if boardHolder != null
         *         False if boardHolder == null
         */
        static boolean initialize(BoardHolder boardHolder) {
            if (boardHolder == null) {
                return false;
            }
            instance = new UpdateBoardHandler(boardHolder);
            return true;
        }

        /**
         * Get the instance of the handler
         * @return The instance of the handler
         *         Null if initialize not called
         */
        static UpdateBoardHandler getInstance() {
            if (instance == null) {
                // Not initialized
                return null;
            }
            return instance;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_REFRESH_BOARD) {
                boardHolder.redraw();
            } else if (msg.what == MESSAGE_SET_BOARD) {
                boardHolder.invalidate();
            }
        }
    }

}
