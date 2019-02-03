package com.example.riley.piplace.BoardActivity;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.riley.piplace.BoardActivity.ColorPicker.ColorPickerDialog;
import com.example.riley.piplace.Client.CommunicateTask.BoardReadThread;
import com.example.riley.piplace.Client.CommunicateTask.BoardClientSocket;
import com.example.riley.piplace.Client.CommunicateTask.BoardWriteThread;
import com.example.riley.piplace.Client.BoardAddPixelListener;
import com.example.riley.piplace.BoardActivity.PlayBoard.BoardHolder;
import com.example.riley.piplace.Messages.Lines.Line;
import com.example.riley.piplace.R;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BoardActivity extends AppCompatActivity {
    public static final int MESSAGE_REFRESH_BOARD = 20;
    public static UpdateBoardHandler updateHandler;  // Todo: Make private and pass to ReadTask and AddPixelListener
    public static final int BOARD_PIXEL_WIDTH = 64;
    public static final int BOARD_PIXEL_HEIGHT = 64;
    private static int color = Color.RED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.board_activity);
        BlockingQueue<Line> messageQueue = new LinkedBlockingQueue<>();
        if (!setWriteTask(messageQueue)) {
            close();
        }
        setBoardListener(messageQueue);
        setColorWheelListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            BoardClientSocket.getSocket().close();
            BoardClientSocket.setSocket(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
     * Initialize task meant for writing to server for the current board
     * @param messageQueue Queue that serves as pipe for messages to server
     * @return True if successful
     *         False otherwise
     */
    private boolean setWriteTask(BlockingQueue<Line> messageQueue) {
        Socket socket = BoardClientSocket.getSocket();
        BoardWriteThread writeTask = BoardWriteThread.createThread(this, socket, messageQueue);
        if (writeTask != null) {
            writeTask.start();
            return true;
        }
        return false;
    }

    /**
     * Initialize task meant for reading from the server for the current board
     * @param bitmap Map of the client's board
     * @return True if successful
     *         False otherwise
     */
    private boolean setReadTask(Bitmap bitmap) {
        Socket socket = BoardClientSocket.getSocket();
        BoardReadThread readTask = BoardReadThread.createThread(this, socket, bitmap);
        if (readTask != null) {
            readTask.start();
            return true;
        }
        return false;
    }

    /**
     * Initialize the listener for the pixel board once the view is
     * rendered
     * @param messageQueue Queue that serves as pipe for messages to server
     */
    private void setBoardListener(final BlockingQueue<Line> messageQueue) {
        final BoardHolder boardHolder = findViewById(R.id.board_holder);
        ViewTreeObserver boardTreeObserver = boardHolder.getViewTreeObserver();
        if (boardTreeObserver.isAlive()) {
            boardTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    boardHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Bitmap pixelBoard = getStartBoard(boardHolder);
                    UpdateBoardHandler.initialize(boardHolder);
                    BoardActivity.updateHandler = UpdateBoardHandler.getInstance();
                    if (!setReadTask(pixelBoard)) {
                        close();
                    }
                    boardHolder.setImage(pixelBoard);
                    boardHolder.setImageListener(new BoardAddPixelListener(pixelBoard, messageQueue));
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
        return Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
    }

    private void setColorWheelListener() {
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

    private void drawButton(Bitmap image) {
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawCircle(image.getWidth() / 2, image.getHeight() / 2,
                          image.getWidth() / 2, paint);
    }

    public void close() {
        Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show();
    }

    private class ChangeColorListener implements View.OnClickListener {

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
        public static boolean initialize(BoardHolder boardHolder) {
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
        public static UpdateBoardHandler getInstance() {
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
                boardHolder.invalidate();
            }
        }
    }

}
