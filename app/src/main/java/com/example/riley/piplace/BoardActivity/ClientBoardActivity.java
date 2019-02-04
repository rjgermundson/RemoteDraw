package com.example.riley.piplace.BoardActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;

import com.example.riley.piplace.BoardActivity.PlayBoard.BoardHolder;
import com.example.riley.piplace.Client.BoardAddPixelListener;
import com.example.riley.piplace.Client.CommunicateTask.BoardClientSocket;
import com.example.riley.piplace.Client.CommunicateTask.BoardReadThread;
import com.example.riley.piplace.Client.CommunicateTask.BoardWriteThread;
import com.example.riley.piplace.Messages.Lines.Line;
import com.example.riley.piplace.R;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientBoardActivity extends BoardActivity {

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
        final ImageButton button = findViewById(R.id.toggle_draw);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDrag) {
                    button.setImageResource(R.drawable.ic_pan_tool_black_24dp);
                } else {
                    button.setImageResource(R.drawable.ic_edit_black_24dp);
                }
                isDrag = !isDrag;
            }
        });
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
}
