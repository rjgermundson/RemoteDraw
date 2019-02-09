package com.example.riley.piplace.Client;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.example.riley.piplace.BoardActivity.BoardActivity;
import com.example.riley.piplace.BoardActivity.ServerBoardActivity;
import com.example.riley.piplace.Messages.Lines.Line;
import com.example.riley.piplace.Server.CommunicateTask.ServerListenThread;
import com.example.riley.piplace.Server.LockedBitmap;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Class that handles adding pixels to the board and sends to the server
 */
public class BoardAddPixelListener implements View.OnTouchListener {
    private BlockingQueue<Line> messageQueue;  // Pipeline to server
    private Bitmap pixelBoard;
    private int prevX;
    private int prevY;

    public BoardAddPixelListener(Bitmap pixelBoard,
                                 BlockingQueue<Line> messageQueue) {
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
        // Round the height and weight to be exactly divisible by pixel dimensions
        int width = (board.getWidth() / BoardActivity.BOARD_PIXEL_WIDTH) * BoardActivity.BOARD_PIXEL_WIDTH;
        int height = (board.getHeight() / BoardActivity.BOARD_PIXEL_HEIGHT) * BoardActivity.BOARD_PIXEL_HEIGHT;
        int widthStretch = width / BoardActivity.BOARD_PIXEL_WIDTH;
        int heightStretch = height / BoardActivity.BOARD_PIXEL_HEIGHT;

        int x = BoardActivity.BOARD_PIXEL_WIDTH * Math.round(event.getX()) / width;
        int y = BoardActivity.BOARD_PIXEL_HEIGHT * Math.round(event.getY()) / height;
        int color = BoardActivity.getColor();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            prevX = x;
            prevY = y;

            // Draw the change on the local board
            Canvas canvas = new Canvas(pixelBoard);
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
            Line line = new Line(color, 0);
            line.addPixel(new Pair<>(x, y));
            messageQueue.add(line);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && event.getHistorySize() > 0) {
            Canvas canvas = new Canvas(pixelBoard);
            Paint paint = new Paint();
            paint.setColor(color);
            Line line = new Line(color, -1);
            line.setLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
            for (Pair<Integer, Integer> p : line.getPixels()) {
                canvas.drawRect(p.first * widthStretch,
                        p.second * heightStretch,
                        p.first * widthStretch + widthStretch,
                        p.second * heightStretch + heightStretch,
                        paint);
            }
            messageQueue.add(line);

            Message message = new Message();
            message.what = BoardActivity.MESSAGE_REFRESH_BOARD;
            ServerBoardActivity.updateHandler.sendMessage(message);
        } else {
            board.performClick();
        }
        return true;
    }
}