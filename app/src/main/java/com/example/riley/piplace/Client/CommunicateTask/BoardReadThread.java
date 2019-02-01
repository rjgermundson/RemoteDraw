package com.example.riley.piplace.Client.CommunicateTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
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
public class BoardReadThread extends Thread {
    private WeakReference<Activity> activity;
    private Socket socket;
    private InputStream inputStream;
    private Scanner input;
    private Bitmap bitmap;

    private BoardReadThread(Activity activity, Socket socket, InputStream inputStream, Bitmap bitmap) {
        this.activity = new WeakReference<>(activity);
        this.socket = socket;
        this.inputStream = inputStream;
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
    public static BoardReadThread createTask(Activity activity, Socket socket, Bitmap bitmap) {
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
        return new BoardReadThread(activity, socket, input, bitmap);
    }

    @Override
    public void run() {
        getInitialBoard();
        while (isConnected()) {
            try {
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        closeSocket();
    }

    private void receive() {
        if (input.hasNextInt()) {
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();
            int widthStretch = imageWidth / BoardActivity.BOARD_PIXEL_WIDTH;
            int heightStretch = imageHeight / BoardActivity.BOARD_PIXEL_HEIGHT;

            Paint paint = new Paint();
            Canvas canvas = new Canvas(bitmap);

            int color = input.nextInt();
            int count = input.nextInt();

            paint.setColor(color);
            for (int i = 0; i < count; i++) {
                int x = input.nextInt();
                int y = input.nextInt();
                canvas.drawRect(x * widthStretch,
                        y * heightStretch,
                        x * widthStretch + widthStretch,
                        y * heightStretch + heightStretch,
                        paint);
            }
            Message message = new Message();
            message.what = BoardActivity.MESSAGE_REFRESH_BOARD;
            BoardActivity.updateHandler.sendMessage(message);
        }
    }

    /**
     * Gets the initial board from the server
     */
    private void getInitialBoard() {
        while (!input.hasNextInt()) { }
        int bytes = input.nextInt();
        System.out.println("LENGTH: " + bytes);
        byte[] bitmapBytes = new byte[bytes];
        int read = 0;
        try {
            int result = -1;
            while (result != 0) {
                System.out.println(read + " bytes read");
                result = inputStream.read(bitmapBytes, read, bytes - read);
                System.out.println(result);
                read += result;
            }
            System.out.println("READ " + read + " bytes");
        } catch (IOException e) {
            closeSocket();
            System.out.println("FAILED TO READ BITMAP");
            return;
        }
        System.out.println("READ BITMAP");
        Bitmap board = BitmapFactory.decodeByteArray(bitmapBytes, 0, bytes);
        if (board == null) {
            System.out.println("NULL BOARD");
            int count = 0;
            while (input.hasNextByte()) {
                input.nextByte();
                count++;
            }
            System.out.println(count + " bytes remaining");
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(board, 0, 0, null);
//        int imageWidth = bitmap.getWidth();
//        int imageHeight = bitmap.getHeight();
//        int widthStretch = imageWidth / BoardActivity.BOARD_PIXEL_WIDTH;
//        int heightStretch = imageHeight / BoardActivity.BOARD_PIXEL_HEIGHT;
//        while (!input.hasNextInt()) {}
//        int width = input.nextInt();
//        int height = input.nextInt();
//
//        Canvas canvas = new Canvas(bitmap);
//        Paint paint = new Paint();
//        for (int i = 0; i < height; i++) {
//            for (int j = 0; j < width; j++) {
//                int red = input.nextInt();
//                int green = input.nextInt();
//                int blue = input.nextInt();
//                if (red != 0 || green != 0 || blue != 0) {
//                    paint.setColor(rgbToInt(red, green, blue));
//                    canvas.drawRect(j * widthStretch,
//                            i * heightStretch,
//                            j * widthStretch + widthStretch,
//                            i * heightStretch + heightStretch,
//                            paint);
//                }
//            }
//        }
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
