package com.example.riley.piplace.Client.CommunicateTask;

import android.app.Activity;

import com.example.riley.piplace.Messages.Lines.Line;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * This class is meant to handle the communication from the client to the server
 */
public class BoardWriteThread extends Thread {
    private WeakReference<Activity> activity;
    private Socket socket;
    private OutputStream output;
    private BlockingQueue<Line> messages;

    private BoardWriteThread(Activity activity, Socket socket,
                             OutputStream output, BlockingQueue<Line> messageQueue) {
        this.activity = new WeakReference<>(activity);
        this.socket = socket;
        this.output = output;
        this.messages = messageQueue;
    }

    /**
     * Return the instance of a BoardCommunicateTask
     * Must be called after the socket is set
     * and message queue is set
     * @param activity Activity this thread is created in
     * @param socket Socket this thread will use for communication
     * @param messageQueue
     * @return new BoardCommunicate task if each component is valid
     *         null if activity == null
     *         null if socket.notConnected || socket == null
     *         null if messageQueue == null
     */
    public static BoardWriteThread createThread(Activity activity, Socket socket, BlockingQueue<Line> messageQueue) {
        if (activity == null) {
            return null;
        } else if (!socket.isConnected()) {
            return null;
        } else if (messageQueue == null) {
            return null;
        }
        OutputStream output;
        try {
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new BoardWriteThread(activity, socket, output, messageQueue);
    }

    @Override
    public void run() {
        while (isConnected()) {
            try {
                Line message = messages.take();
                send(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        closeSocket();
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
     * @param line Line to write to host
     * @return True if message sent successfully
     *         False otherwise
     */
    private boolean send(Line line) {
        if (line == null) {
            return true;
        }
        try {
            output.write(line.getBytes());
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
