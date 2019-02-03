package com.example.riley.piplace.Server;

import android.graphics.Bitmap;
import android.util.Pair;

import com.example.riley.piplace.Messages.Lines.Line;
import com.example.riley.piplace.Utility;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * This task handles a single client connection to this board
 */
public class ServerPerClientThread extends Thread {
    private Queue<Line> lines = new LinkedList<>();
    private int clientID;
    private Socket socket;
    private OutputStream output;
    private DataInputStream input;

    ServerPerClientThread(Socket socket, int id) {
        this.socket = socket;
        clientID = clientID;
        try {
            output = socket.getOutputStream();
        } catch (IOException e) {
            closeSocket();
        }
        try {
            this.input = new DataInputStream(socket.getInputStream());
        } catch (IOException inputException) {
            try {
                output.write(-1);
            } catch (IOException e) {
                closeSocket();
            }
        }
    }

    @Override
    public void run() {
        super.run();
        sendBoard();
        while (isConnected()) {
            // Listen to the client for action
            try {
                receive();
                if (lines.size() > 0) {
                    send(lines.remove());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check to see if the client has made any actions
     */
    private void receive() throws IOException {
        int color = 0;
        int count;
        if (input.available() > 0) {
            color = input.readInt();
            count = input.readInt();
            System.out.println("COUNT: " + count);
        } else {
            return;
        }

        // Construct the line that the client drew
        Line line = new Line(color, clientID);
        if (count == 0) {
            // Count should never be zero
            // Todo: Send error flag
            return;
        }
        for (int i = 0; i < count; i++) {
            int x = input.readInt();
            int y = input.readInt();
            System.out.println(x + ", " + y);
            line.addPixel(new Pair<>(x, y));
        }
        ServerUpdateThread.addLine(line);
    }

    /**
     * Sends the given line to the client
     * @param line The line to send to the client
     */
    public void sendLine(Line line) {
        lines.add(line);
    }

    /**
     * Writes the given line to the client in the proper format
     * @param line The line to write to the client
     */
    private void send(Line line) throws IOException {
        output.write(line.getBytes());
    }

    /**
     * Sends the client the current state of this server board
     */
    private void sendBoard() {
        Bitmap board = LockedBitmap.get();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        board.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        try {
            byte[] boardBytes = byteStream.toByteArray();
            // Send integer length as byte array
            output.write(Utility.intToBytes(boardBytes.length));
            // Send bitmap
            output.write(boardBytes);
        } catch (IOException e) {
            closeSocket();
        }
        LockedBitmap.release();
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
        System.out.println("CLOSE");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
