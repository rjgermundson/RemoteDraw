package com.example.riley.piplace.BoardActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.example.riley.piplace.BoardActivity.CommunicateTask.BoardCommunicateTask;
import com.example.riley.piplace.BoardActivity.CommunicateTask.BoardSocket;
import com.example.riley.piplace.BoardActivity.PlayBoard.BoardAddPixelListener;
import com.example.riley.piplace.BoardActivity.PlayBoard.BoardHolder;
import com.example.riley.piplace.R;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BoardActivity extends AppCompatActivity {
    private static final int BOARD_PIXEL_WIDTH = 64;
    private static final int BOARD_PIXEL_HEIGHT = 64;
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();  // Pipeline to server
    private InputStream inputFromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.board_activity);
        setCommunicateTask();
        setBoardListener();
    }

    /**
     * Initialize task meant for communicating with the current board
     */
    private void setCommunicateTask() {
        BoardCommunicateTask task = BoardCommunicateTask.createTask(this, BoardSocket.getSocket(), messageQueue);
        if (task != null) {
            task.execute();
            inputFromServer = task.getInputStream();
        }
    }

    /**
     * Initialize the listener for the pixel board once the view is
     * rendered
     */
    private void setBoardListener() {
        final BoardHolder boardHolder = findViewById(R.id.board_holder);
        ViewTreeObserver boardTreeObserver = boardHolder.getViewTreeObserver();
        if (boardTreeObserver.isAlive()) {
            boardTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    boardHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Bitmap pixelBoard = getStartBoard(boardHolder);
                    boardHolder.setImage(pixelBoard);
                    boardHolder.setImageListener(new BoardAddPixelListener(pixelBoard, messageQueue,
                                                                           BOARD_PIXEL_WIDTH,
                                                                           BOARD_PIXEL_HEIGHT));
                }
            });
        }
    }

    /**
     * Receives the server's copy of the board state
     * @param clientBoard Image to construct the board's bitmap from
     * @return A bitmap representing the board state
     */
    private Bitmap getStartBoard(View clientBoard) {
        int imageWidth = (clientBoard.getMeasuredWidth() / BOARD_PIXEL_WIDTH) * BOARD_PIXEL_WIDTH;
        int imageHeight = (clientBoard.getMeasuredWidth() / BOARD_PIXEL_HEIGHT) * BOARD_PIXEL_HEIGHT;
        Scanner input = new Scanner(inputFromServer);
        while (!input.hasNext()) {}  // Wait for server response
        int width = input.nextInt();
        int height = input.nextInt();
        Bitmap boardBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);

        int widthStretch = imageWidth / BOARD_PIXEL_WIDTH;
        int heightStretch = imageHeight / BOARD_PIXEL_HEIGHT;

        Canvas canvas = new Canvas(boardBitmap);
        Paint paint = new Paint();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int red = input.nextInt();
                int green = input.nextInt();
                int blue = input.nextInt();
                if (red != 0 || green != 0 || blue != 0) {
                    paint.setColor(Color.argb(255, red, green, blue));
                    canvas.drawRect(j * widthStretch,
                            i * heightStretch,
                            j * widthStretch + widthStretch,
                            i * heightStretch + heightStretch,
                            paint);
                }
            }
        }
        return boardBitmap;
    }


    public void close() {
        Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show();
    }
}
