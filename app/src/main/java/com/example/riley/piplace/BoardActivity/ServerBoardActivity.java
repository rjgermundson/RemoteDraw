package com.example.riley.piplace.BoardActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewTreeObserver;

import android.widget.ImageButton;
import android.widget.TextView;

import com.example.riley.piplace.BoardActivity.PlayBoard.BoardHolder;
import com.example.riley.piplace.R;
import com.example.riley.piplace.Server.CommunicateTask.BoardServerSocket;
import com.example.riley.piplace.Server.LockedBitmap;
import com.example.riley.piplace.Server.ServerAddPixelListener;
import com.example.riley.piplace.Server.CommunicateTask.ServerListenThread;
import com.example.riley.piplace.Server.CommunicateTask.ServerUpdateThread;


public class ServerBoardActivity extends BoardActivity {
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
        final ImageButton button = findViewById(R.id.toggle_draw);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDrag) {
                    button.setImageResource(R.drawable.ic_edit_black_24dp);
                } else {
                    button.setImageResource(R.drawable.ic_pan_tool_black_24dp);
                }
                isDrag = !isDrag;
            }
        });
        button.performClick();
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

    private void setServerListener() {
        ServerListenThread serverListenTask = new ServerListenThread(BoardServerSocket.getSocket());
        serverListenTask.start();
        ServerUpdateThread serverUpdateThread = new ServerUpdateThread(findViewById(R.id.board_holder));
        serverUpdateThread.start();
    }
}