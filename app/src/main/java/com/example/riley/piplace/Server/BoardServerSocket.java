package com.example.riley.piplace.Server;

import java.net.ServerSocket;

/**
 * Class allowing a socket to be used between activities to listen for clients
 */
public class BoardServerSocket {
    private static ServerSocket serverSocket;

    private BoardServerSocket() {}

    public synchronized static ServerSocket getSocket() {
        return serverSocket;
    }

    public synchronized static void setSocket(ServerSocket socket) {
        serverSocket = socket;
    }
}
