package com.example.riley.piplace.Client;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import com.example.riley.piplace.BoardActivity.BoardActivity;

import java.util.Locale;
import java.util.concurrent.BlockingQueue;

/**
 * Class that handles adding pixels to the board and sends to the server
 */
public class BoardAddPixelListener implements View.OnTouchListener {
    private BlockingQueue<String> messageQueue;  // Pipeline to server
    private Bitmap pixelBoard;

    public BoardAddPixelListener(Bitmap pixelBoard,
                                 BlockingQueue<String> messageQueue) {
        this.messageQueue = messageQueue;
        this.pixelBoard = pixelBoard;
    }

    /**
     * When touched the board records what pixel the user wants to change
     * What changes to make. And sends the message to the server via the
     * message queue
     * @param board ImageView representing the board
     * @param event MotionEvent that occurred on board
     * @return True if message sent successfully
     *         False otherwise
     */
    @Override
    public boolean onTouch(View board, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Round the height and weight to be exactly divisible by pixel dimensions
            int width = (board.getWidth() / BoardActivity.BOARD_PIXEL_WIDTH) * BoardActivity.BOARD_PIXEL_WIDTH;
            int height = (board.getHeight() / BoardActivity.BOARD_PIXEL_HEIGHT) * BoardActivity.BOARD_PIXEL_HEIGHT;

            int widthStretch = width / BoardActivity.BOARD_PIXEL_WIDTH;
            int heightStretch = height / BoardActivity.BOARD_PIXEL_HEIGHT;

            int x = BoardActivity.BOARD_PIXEL_WIDTH * Math.round(event.getX()) / width;
            int y = BoardActivity.BOARD_PIXEL_HEIGHT * Math.round(event.getY()) / height;

            // Draw the change on the local board
            Canvas canvas = new Canvas(pixelBoard);
            int color = BoardActivity.getColor();
            Paint paint = new Paint();
            paint.setColor(color);
            canvas.drawRect(x * widthStretch,
                    y * heightStretch,
                    x * widthStretch + widthStretch,
                    y * heightStretch + heightStretch,
                    paint);

            // Alert the UI to update the board
            Message message = new Message();
            message.what = BoardActivity.MESSAGE_REFRESH_BOARD;
            BoardActivity.updateHandler.sendMessage(message);

            // Send the change to the server
            String changeMessage = String.format(Locale.US, color + " 1 " + x + " " + y + " ");
            messageQueue.add(changeMessage);
        } else {
            board.performClick();
        }
        return true;
    }
}