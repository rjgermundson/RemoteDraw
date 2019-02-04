package com.example.riley.piplace.Client.CommunicateTask;

import com.example.riley.piplace.Client.QueryLobbyDatagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * This thread handles asking for servers to connect to
 * Also handles responses from potential lobbies
 */
public class QueryLobbyThread extends Thread {
    private static final int LISTEN_PORT = 5600;
    private DatagramSocket socket;

    private QueryLobbyThread(DatagramSocket socket) {
        this.socket = socket;
    }

    public static QueryLobbyThread createThread(InetAddress host) {
        try {
            DatagramSocket socket = new DatagramSocket(LISTEN_PORT, host);
            return new QueryLobbyThread(socket);
        } catch (SocketException e) {
            return null;
        }
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("PINGING");
            ping();
            receive();
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Broadcast to ask for any server on the LAN for their IP and port
     */
    private void ping() {
        DatagramPacket queryPacket = new QueryLobbyDatagram().toDatagramPacket();
        try {
            socket.send(queryPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive a datagram and parse it for a valid server to connect to
     */
    private void receive() {

    }
}
