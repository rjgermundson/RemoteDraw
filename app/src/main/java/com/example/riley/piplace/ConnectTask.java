package com.example.riley.piplace;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.riley.piplace.BoardActivity.ClientBoardActivity;
import com.example.riley.piplace.Client.CommunicateTask.BoardClientSocket;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class ConnectTask extends AsyncTask<Void, Void, Boolean> {
    private WeakReference<Activity> activity;
    private InetAddress host;
    private int port;
    private Socket socket;

    public ConnectTask(Activity activity, InetAddress host, int port) {
        super();
        this.activity = new WeakReference<>(activity);
        this.host = host;
        this.port = port;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            this.socket = connectToHost(host, port);
        } catch (IOException e) {
            e.printStackTrace();
            this.socket = null;
        }
        return (socket != null && socket.isConnected());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            try {
                socket.setReuseAddress(true);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            BoardClientSocket.setSocket(socket);
            openClientBoard();
        } else {
            BoardClientSocket.setSocket(null);
            toast("Failed to connect");
        }
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        System.out.println("CANCELLED");
    }

    /**
     * Returns a socket to the given address
     * @param address Address to connect to
     * @param port Port at address to connect to
     * @return A TCP socket to the given address
     *         null if socket write closed
     * @throws IOException If failed to create socket
     */
    private Socket connectToHost(InetAddress address, int port) throws IOException {
        Socket socket = new Socket();
        System.out.println(address + " : " + port);
        socket.connect(new InetSocketAddress(address, port), 300);
        if (socket.isOutputShutdown()) {
            closeSocket();
            return null;
        }
        return socket;
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
     * Opens a board activity from the calling activity
     */
    private void openClientBoard() {
        activity.get().startActivity(new Intent(activity.get(), ClientBoardActivity.class));
    }

    /**
     * Alert the user of an error
     * @param message Message to tell the user
     */
    private void toast(String message) {
        Toast.makeText(activity.get().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
