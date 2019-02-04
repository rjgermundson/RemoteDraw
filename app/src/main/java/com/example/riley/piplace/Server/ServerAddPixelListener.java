package com.example.riley.piplace.Server;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Message;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.example.riley.piplace.BoardActivity.BoardActivity;
import com.example.riley.piplace.BoardActivity.ServerBoardActivity;
import com.example.riley.piplace.Messages.Lines.Line;
import com.example.riley.piplace.Server.CommunicateTask.ServerListenThread;

public class ServerAddPixelListener implements View.OnTouchListener {
    private int boardPixelWidth;
    private int boardPixelHeight;
    private int prevX;
    private int prevY;

    public ServerAddPixelListener(int width, int height) {
        this.boardPixelWidth = width;
        this.boardPixelHeight = height;
    }

    /**
     * When touched the board records what pixel the user wants to change
     * What changes to make. And sends the message to all clients of this server
     *
     * @param board ImageView representing the board
     * @param event MotionEvent that occurred on board
     * @return True if message sent successfully
     *         False otherwise
     */
    @Override
    public boolean onTouch(View board, MotionEvent event) {

        int width = (board.getWidth() / boardPixelWidth) * boardPixelWidth;
        int height = (board.getHeight() / boardPixelHeight) * boardPixelHeight;
        int widthStretch = width / boardPixelWidth;
        int heightStretch = height / boardPixelHeight;

        int x = boardPixelWidth * Math.round(event.getX()) / width;
        int y = boardPixelHeight * Math.round(event.getY()) / height;
        int color = BoardActivity.getColor();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Draw the given pixel onto the board
            prevX = x;
            prevY = y;
            Bitmap pixelBoard = LockedBitmap.get();
            Canvas canvas = new Canvas(pixelBoard);
            Paint paint = new Paint();
            paint.setColor(color);
            canvas.drawRect(x * widthStretch,
                    y * heightStretch,
                    x * widthStretch + widthStretch,
                    y * heightStretch + heightStretch,
                    paint);
            Line line = new Line(color, -1);
            line.addPixel(new Pair<>(x, y));
            ServerListenThread.sendLine(line);

            // Alert the UI to update the board
            Message message = new Message();
            message.what = BoardActivity.MESSAGE_REFRESH_BOARD;
            ServerBoardActivity.updateHandler.sendMessage(message);
            LockedBitmap.release();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && event.getHistorySize() > 0) {
            Bitmap pixelBoard = LockedBitmap.get();
            Canvas canvas = new Canvas(pixelBoard);
            Paint paint = new Paint();
            paint.setColor(color);
            Line line = new Line(color, -1);
            line.addPixel(new Pair<>(prevX, prevY));
            while (prevX != x || prevY != y) {
                if (prevX < x) {
                    prevX++;
                } else if (prevX > x) {
                    prevX--;
                }
                if (prevY < y) {
                    prevY++;
                } else if (prevY > y) {
                    prevY--;
                }
                canvas.drawRect(prevX * widthStretch,
                        prevY * heightStretch,
                        prevX * widthStretch + widthStretch,
                        prevY * heightStretch + heightStretch,
                        paint);
                line.addPixel(new Pair<>(prevX, prevY));
            }
            ServerListenThread.sendLine(line);

            Message message = new Message();
            message.what = BoardActivity.MESSAGE_REFRESH_BOARD;
            ServerBoardActivity.updateHandler.sendMessage(message);
            LockedBitmap.release();
        }
        return true;
    }
}
