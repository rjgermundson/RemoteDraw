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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int width = (board.getWidth() / boardPixelWidth) * boardPixelWidth;
            int height = (board.getHeight() / boardPixelHeight) * boardPixelHeight;
            int widthStretch = width / boardPixelWidth;
            int heightStretch = height / boardPixelHeight;

            int x = boardPixelWidth * Math.round(event.getX()) / width;
            int y = boardPixelHeight * Math.round(event.getY()) / height;

            // Draw the given pixel onto the board
            Bitmap pixelBoard = LockedBitmap.get();
            Canvas canvas = new Canvas(pixelBoard);
            int color = BoardActivity.getColor();
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
            LockedBitmap.release();

            // Alert the UI to update the board
            Message message = new Message();
            message.what = BoardActivity.MESSAGE_REFRESH_BOARD;
            ServerBoardActivity.updateHandler.sendMessage(message);
        } else {
            board.performClick();
        }
        return true;
    }
}
