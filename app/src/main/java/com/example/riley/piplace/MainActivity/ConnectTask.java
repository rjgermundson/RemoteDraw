package com.example.riley.piplace.MainActivity;

import android.os.AsyncTask;

import com.example.riley.piplace.Client.CommunicateTask.BoardClientSocket;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ConnectTask extends AsyncTask<Void, Void, Boolean> {
    private WeakReference<MainActivity> mainActivity;
    private String host;
    private int port;
    private Socket socket;

    ConnectTask(MainActivity mainActivity,
                       String host, int port) {
        super();
        this.mainActivity = new WeakReference<>(mainActivity);
        this.host = host;
        this.port = port;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        InetAddress address = getAddress(host);
        System.err.println("Opening");
        if (address == null) {
            return false;
        }
        try {
            System.err.println("Connecting");
            this.socket = connectToHost(address, port);
            System.out.println("Finished connecting");
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
            System.out.println("Post connected");
            BoardClientSocket.setSocket(socket);
            mainActivity.get().openClientBoard();
        } else {
            BoardClientSocket.setSocket(null);
            mainActivity.get().failedToOpenBoard(host);
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
        socket.connect(new InetSocketAddress(address, port), 300);
        if (socket.isOutputShutdown()) {
            closeSocket();
            return null;
        }
        return socket;
    }

    /**
     * Get the IP associated with given hostname
     * @param host Host to check for
     * @return InetAddress for given host
     *         null if invalid host, or no IP found
     */
    private InetAddress getAddress(String host) {
        InetAddress result;
        try {
            result = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return null;
        }
        return result;
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
}
