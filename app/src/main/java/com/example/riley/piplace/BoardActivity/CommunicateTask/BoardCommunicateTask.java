package com.example.riley.piplace.BoardActivity.CommunicateTask;

import android.os.AsyncTask;

import com.example.riley.piplace.BoardActivity.BoardActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * This class communicates with on a given socket
 */
public class BoardCommunicateTask extends AsyncTask<Void, Void, Void> {
    private WeakReference<BoardActivity> activity;
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private BlockingQueue<String> messages;

    private BoardCommunicateTask(BoardActivity activity, Socket socket, InputStream input,
                                 OutputStream output, BlockingQueue<String> messageQueue) {
        this.activity = new WeakReference<>(activity);
        this.socket = socket;
        this.input = input;
        this.output = output;
        this.messages = messageQueue;
    }

    /**
     * Return the instance of a BoardCommunicateTask
     * Must be called after the socket is set
     * and message queue is set
     * @return new BoardCommunicate task if each component is valid
     *         null if activity == null
     *         null if socket.notConnected || socket == null
     *         null if messageQueue == null
     */
    public static BoardCommunicateTask createTask(BoardActivity activity, Socket socket, BlockingQueue<String> messageQueue) {
        if (activity == null) {
            return null;
        } else if (!socket.isConnected()) {
            return null;
        } else if (messageQueue == null) {
            return null;
        }
        InputStream input;
        OutputStream output;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new BoardCommunicateTask(activity, socket, input, output, messageQueue);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (isConnected()) {
            try {
                String message = messages.take();
                send(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        closeSocket();
        activity.get().close();
        return null;
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

    /**
     * Writes to the given host
     * @param message Message to write to host
     * @return True if message sent successfully
     *         False otherwise
     */
    private boolean send(String message) {
        if (message == null) {
            return true;
        }
        byte[] byteMessage = message.getBytes();
        try {
            output.write(byteMessage);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the stream representing what the server
     * sends to the client
     * @return InputStream for the connection between
     *         the client and server
     */
    public InputStream getInputStream() {
        return this.input;
    }
}
