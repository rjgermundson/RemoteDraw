package com.example.riley.piplace.Server.CommunicateTask;

import android.os.AsyncTask;

import com.example.riley.piplace.MainActivity.MainActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 * This class creates the server socket for the board activity
 */
public class HostTask extends AsyncTask<Void, Void, Boolean> {
    private WeakReference<MainActivity> mainActivity;
    private ServerSocket serverSocket;
    private String ip;
    private int port;

    public HostTask(MainActivity activity, String ip, int port) {
        this.mainActivity = new WeakReference<>(activity);
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(ip);
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                InetSocketAddress address = new InetSocketAddress(inetAddress, port);
                serverSocket.bind(address);
            } catch (IOException e) {
                System.out.println("Failed to create socket with IP: " + ip);
            }
        } catch (UnknownHostException e) {
            System.out.println("Failed to get address associated with IP: " + ip);
        }
        return serverSocket != null;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            BoardServerSocket.setSocket(serverSocket);
            mainActivity.get().openServerBoard(ip, port);
        } else {
            mainActivity.get().failedToHost();
        }
    }
}
