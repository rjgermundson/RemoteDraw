package com.example.riley.piplace.BoardActivity.CommunicateTask;

import java.net.Socket;

/**
 * Class representing a socket that can be used to communicate with a pixel board
 */
public class BoardSocket {
    private static Socket board_socket;

    private BoardSocket() { }

    public synchronized static Socket getSocket() {
        return board_socket;
    }

    public synchronized static void setSocket(Socket socket) {
        board_socket = socket;
    }
}
