package com.example.riley.piplace.BoardActivity;

import android.app.DialogFragment;
import android.content.Intent;
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
import android.widget.TextView;

import com.example.riley.piplace.BoardActivity.ColorPicker.ColorPickerDialog;
import com.example.riley.piplace.BoardActivity.PlayBoard.BoardHolder;
import com.example.riley.piplace.R;
import com.example.riley.piplace.Server.CommunicateTask.BoardServerSocket;
import com.example.riley.piplace.Server.LockedBitmap;
import com.example.riley.piplace.Server.ServerAddPixelListener;
import com.example.riley.piplace.Server.CommunicateTask.ServerListenThread;
import com.example.riley.piplace.Server.CommunicateTask.ServerUpdateThread;


public class ServerBoardActivity extends AppCompatActivity {
    public static final int MESSAGE_REFRESH_BOARD = 20;
    public static UpdateBoardHandler updateHandler;  // Todo: Make private and pass to ReadTask and AddPixelListener?
    public static final int BOARD_PIXEL_WIDTH = 64;
    public static final int BOARD_PIXEL_HEIGHT = 64;
    private static int color = Color.RED;

    private String IP;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.server_board_activity);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        IP = extras.getString("IP");
        port = extras.getInt("PORT");
        setInfo();
        setBoardListener();
        setColorWheelListener();
        setServerListener();
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
     * Initialize the session fields
     */
    private void setInfo() {
        TextView ipText = findViewById(R.id.ip_field);
        ipText.setText(IP);
        TextView portText = findViewById(R.id.port_field);
        portText.setText("" + port);
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
                    LockedBitmap.setBitmap(pixelBoard);
                    UpdateBoardHandler.initialize(boardHolder);
                    ServerBoardActivity.updateHandler = UpdateBoardHandler.getInstance();
                    boardHolder.setImage(pixelBoard);
                    boardHolder.setImageListener(new ServerAddPixelListener(BOARD_PIXEL_WIDTH,
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
                    colorWheel.setOnClickListener(new ServerBoardActivity.ChangeColorListener());
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

    private void setServerListener() {
        ServerListenThread serverListenTask = new ServerListenThread(BoardServerSocket.getSocket());
        serverListenTask.start();
        ServerUpdateThread serverUpdateThread = new ServerUpdateThread(findViewById(R.id.board_holder));
        serverUpdateThread.start();
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
