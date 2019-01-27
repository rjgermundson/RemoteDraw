package com.example.riley.piplace.BoardActivity.PlayBoard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.riley.piplace.BoardActivity.BoardActivity;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * Class that handles adding pixels to the board
 */
public class BoardAddPixelListener implements View.OnTouchListener {
    private int boardPixelWidth;
    private int boardPixelHeight;

    private BlockingQueue<String> messageQueue;  // Pipeline to server
    private Bitmap pixelBoard;

    public BoardAddPixelListener(Bitmap pixelBoard,
                                 BlockingQueue<String> messageQueue,
                                 int width, int height) {
        this.boardPixelWidth = width;
        this.boardPixelHeight = height;
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
        System.out.println(board.getMeasuredWidth() + ":" + board.getMeasuredHeight());
        System.out.println(event.getX() + ", " + event.getY());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int width = (board.getWidth() / boardPixelWidth) * boardPixelWidth;
            int height = (board.getHeight() / boardPixelHeight) * boardPixelHeight;
            int widthStretch = width / boardPixelWidth;
            int heightStretch = height / boardPixelHeight;

            int x = boardPixelWidth * Math.round(event.getX()) / width;
            int y = boardPixelHeight * Math.round(event.getY()) / height;

            Canvas canvas = new Canvas(pixelBoard);
            int color = BoardActivity.getColor();
            Paint paint = new Paint();
            paint.setColor(color);
            canvas.drawRect(x * widthStretch,
                    y * heightStretch,
                    x * widthStretch + widthStretch,
                    y * heightStretch + heightStretch,
                    paint);
            ((ImageView) board).setImageBitmap(pixelBoard);
            String message = String.format(Locale.US, "%3d %3d %3d %3d %3d|", x, y, Color.red(color), Color.green(color), Color.blue(color));
            messageQueue.add(message);
        }
        return true;
    }
}