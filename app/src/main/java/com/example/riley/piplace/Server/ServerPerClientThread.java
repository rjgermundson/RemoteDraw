package com.example.riley.piplace.Server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Pair;

import com.example.riley.piplace.Messages.Lines.Line;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

/**
 * This task handles a single client connection to this board
 */
public class ServerPerClientThread extends Thread {
    private Queue<Line> lines = new LinkedList<>();
    private int clientID;
    private Socket socket;
    private OutputStream outputStream;
    private Scanner input;

    ServerPerClientThread(Socket socket, int id) {
        this.socket = socket;
        clientID = clientID;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("FAILED TO GET OUTPUT STREAM");
            closeSocket();
        }
        try {
            this.input = new Scanner(socket.getInputStream());
        } catch (IOException inputException) {
            System.out.println("FAILED TO GET INPUTSTREAM");
            try {
                outputStream.write(-1);
            } catch (IOException e) {
                System.out.println("FAILED TO WRITE FAILURE");
                closeSocket();
            }
        }
        System.out.println("CREATED SPCTask");
    }

    @Override
    public void run() {
        super.run();
        System.out.println("DOING IN BACK");
        sendBoard();
        while (isConnected()) {
            // Listen to the client for action
            try {
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (lines.size() > 0) {
                send(lines.remove());
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
    private void receive() {
        if (input.hasNextInt()) {
            // Construct the line that the client drew
            int color = input.nextInt();
            Line line = new Line(color, clientID);
            int count = input.nextInt();
            if (count == 0) {
                // Count should never be zero
                // Todo: Send error flag
                return;
            }
            for (int i = 0; i < count; i++) {
                int x = input.nextInt();
                int y = input.nextInt();
                line.addPixel(new Pair<>(x, y));
            }
            ServerUpdateThread.addLine(line);
        }
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
    private void send(Line line) {
        StringBuilder builder = new StringBuilder();
        Set<Pair<Integer, Integer>> pixels = line.getPixels();
        builder.append(line.getColor());
        builder.append(" ");
        builder.append(pixels.size());
        builder.append(" ");
        for (Pair p : pixels) {
            builder.append(p.first);
            builder.append(" ");
            builder.append(p.second);
            builder.append(" ");
        }
        try {
            outputStream.write(builder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            closeSocket();
        }
    }

    /**
     * Sends the client the current state of this server board
     */
    private void sendBoard() {
        Bitmap board = LockedBitmap.get();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        board.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        System.out.println("COMPRESSED");
        try {
            byte[] boardBytes = byteStream.toByteArray();
            Bitmap test = BitmapFactory.decodeByteArray(boardBytes, 0, boardBytes.length);
            if (test == null) {
                System.out.println("DIDN'T RECREATE THE BITMAP");
            }
            outputStream.write((boardBytes.length + " ").getBytes());
            System.out.println("Wrote: " + boardBytes.length);
            outputStream.write(boardBytes);
            outputStream.flush();
            outputStream.write('a');
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
