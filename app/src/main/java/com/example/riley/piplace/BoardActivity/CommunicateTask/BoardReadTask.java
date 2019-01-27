package com.example.riley.piplace.BoardActivity.CommunicateTask;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Message;

import com.example.riley.piplace.BoardActivity.BoardActivity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Scanner;

/**
 * This class is meant to handle communication from the server to the client
 * Setting the board of the client based on the information sent from the server
 */
public class BoardReadTask extends AsyncTask<Void, Void, Void> {
    private static final int BOARD_PIXEL_WIDTH = 64;
    private static final int BOARD_PIXEL_HEIGHT = 64;
    private WeakReference<BoardActivity> activity;
    private Socket socket;
    private Scanner input;
    private Bitmap bitmap;

    private BoardReadTask(BoardActivity activity, Socket socket, InputStream inputStream, Bitmap bitmap) {
        this.activity = new WeakReference<>(activity);
        this.socket = socket;
        this.input = new Scanner(inputStream);
        this.bitmap = bitmap;
    }

    /**
     * Return the instance of a BoardCommunicateTask
     * Must be called after the socket is set
     * @param activity BoardActivity this task is listening for
     * @param socket Socket to communicate on
     * @param bitmap Bitmap representing the client's board
     * @return new BoardCommunicate task if each component is valid
     *         null if activity == null
     *         null if socket.notConnected || socket == null
     */
    public static BoardReadTask createTask(BoardActivity activity, Socket socket, Bitmap bitmap) {
        if (activity == null) {
            return null;
        } else if (!socket.isConnected()) {
            return null;
        }
        InputStream input;
        try {
            input = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new BoardReadTask(activity, socket, input, bitmap);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        getInitialBoard();
        while (isConnected()) {
            try {
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        closeSocket();
        activity.get().close();
        return null;
    }

    private void receive() throws Exception {
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        int widthStretch = imageWidth / BOARD_PIXEL_WIDTH;
        int heightStretch = imageHeight / BOARD_PIXEL_HEIGHT;

        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);

        while (input.hasNextInt()) {
            int x = input.nextInt();
            int y = input.nextInt();
            int red = input.nextInt();
            int green = input.nextInt();
            int blue = input.nextInt();
            paint.setColor(rgbToInt(red, green, blue));
            canvas.drawRect(x * widthStretch,
                    y * heightStretch,
                    x * widthStretch + widthStretch,
                    y * heightStretch + heightStretch,
                    paint);

            Message message = new Message();
            message.what = BoardActivity.MESSAGE_REFRESH_BOARD;
            BoardActivity.updateHandler.sendMessage(message);
        }
    }

    /**
     * Gets the initial board from the server
     */
    private void getInitialBoard() {
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        int widthStretch = imageWidth / BOARD_PIXEL_WIDTH;
        int heightStretch = imageHeight / BOARD_PIXEL_HEIGHT;
        while (!input.hasNextInt()) {}
        int width = input.nextInt();
        int height = input.nextInt();

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int red = input.nextInt();
                int green = input.nextInt();
                int blue = input.nextInt();
                if (red != 0 || green != 0 || blue != 0) {
                    paint.setColor(rgbToInt(red, green, blue));
                    canvas.drawRect(j * widthStretch,
                            i * heightStretch,
                            j * widthStretch + widthStretch,
                            i * heightStretch + heightStretch,
                            paint);
                }
            }
        }
        Message message = new Message();
        message.what = BoardActivity.MESSAGE_REFRESH_BOARD;
        BoardActivity.updateHandler.sendMessage(message);
    }

    private int rgbToInt(int r, int g, int b) {
        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = (b) & 0x000000FF;
        return 0xFF000000 | r | g | b;
    }

    /**
     * Returns whether the connection handler
     * is still connected to the host
     * @return True if connected
     *         False otherwise
     */
    private boolean isConnected() {
        if (socket == null) {
            return false;
        }
        if (!socket.isConnected()) {
            // Socket not connected
            // and may need to be closed
            if (!socket.isClosed()) {
                closeSocket();
            }
            return false;
        }
        return true;
    }

    /**
     * Closes the socket this task communicates on
     */
    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
