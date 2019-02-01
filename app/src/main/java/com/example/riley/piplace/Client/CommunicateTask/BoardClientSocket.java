package com.example.riley.piplace.Client.CommunicateTask;

import java.net.Socket;

/**
 * Class allowing a socket to be used between activities to communicate for a client board
 */
public class BoardClientSocket extends Socket {
    private static Socket boardSocket;

    private BoardClientSocket() { }

    public synchronized static Socket getSocket() {
        return boardSocket;
    }

    public synchronized static void setSocket(Socket socket) {
        boardSocket = socket;
    }
}
