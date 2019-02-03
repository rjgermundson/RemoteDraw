package com.example.riley.piplace.Server.CommunicateTask;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Message;
import android.util.Pair;
import android.view.View;

import com.example.riley.piplace.BoardActivity.ServerBoardActivity;
import com.example.riley.piplace.Messages.Lines.Line;
import com.example.riley.piplace.Server.LockedBitmap;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class draws to the Server's board
 */
public class ServerUpdateThread extends Thread {
    private static BlockingQueue<Line> strokes = new LinkedBlockingQueue<>();
    private WeakReference<View> view;

    /**
     * Constructor for ServerUpdateThread
     * @param view The view the bitmap is set to
     */
    public ServerUpdateThread(View view) {
        this.view = new WeakReference<>(view);
    }

    public static void addLine(Line l) {
        strokes.add(l);
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                Line line = strokes.take();
                if (line != null) {
                    // Must draw then send
                    // Otherwise starting board can be desynchronized
                    drawLine(line);
                    ServerListenThread.sendLine(line);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Draws the given line on the board the server phone shows
     * @param line The line to draw
     */
    private void drawLine(Line line) {
        int color = line.getColor();
        Paint paint = new Paint();
        paint.setColor(color);

        Bitmap pixelBoard = LockedBitmap.get();
        int width = (pixelBoard.getWidth() / ServerBoardActivity.BOARD_PIXEL_WIDTH) * ServerBoardActivity.BOARD_PIXEL_WIDTH;
        int height = (pixelBoard.getHeight() / ServerBoardActivity.BOARD_PIXEL_HEIGHT) * ServerBoardActivity.BOARD_PIXEL_HEIGHT;

        int widthStretch = width / ServerBoardActivity.BOARD_PIXEL_WIDTH;
        int heightStretch = height / ServerBoardActivity.BOARD_PIXEL_HEIGHT;

        Canvas canvas = new Canvas(pixelBoard);
        for (Pair<Integer, Integer> p : line.getPixels()) {
            int x = p.first;
            int y = p.second;
            // Draw each change to the board
            canvas.drawRect(x * widthStretch,
                    y * heightStretch,
                    x * widthStretch + widthStretch,
                    y * heightStretch + heightStretch,
                    paint);
        }

        // Alert the UI to update the board
        Message message = new Message();
        message.what = ServerBoardActivity.MESSAGE_REFRESH_BOARD;
        ServerBoardActivity.updateHandler.sendMessage(message);

        LockedBitmap.release();
    }
}
