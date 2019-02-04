package com.example.riley.piplace.Server.CommunicateTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * This thread advertises the current hosted board to any client
 * asking for it
 */
public class LobbyAdvertiserThread extends Thread {
    private static final int PACKET_SIZE = 256;
    private static final int BROADCAST_PORT = 5200;
    private DatagramSocket socket;
    private InetAddress host;
    private int port;

    private LobbyAdvertiserThread(DatagramSocket socket, InetAddress host, int port) {
        this.socket = socket;
        this.host = host;
        this.port = port;
    }

    public static LobbyAdvertiserThread createThread(InetAddress host, int port) {
        try {
            DatagramSocket socket = new DatagramSocket(BROADCAST_PORT);
            return new LobbyAdvertiserThread(socket, host, port);
        } catch (SocketException e) {
            return null;
        }
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Listening for datagrams");
            DatagramPacket packet = listen();
            if (validPacket(packet)) {
                respond(packet);
            }

        }
    }

    /**
     * Listens for datagrams from potential clients asking for
     * servers
     * @return packet that was broadcast
     */
    private DatagramPacket listen() {
        DatagramPacket result = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        try {
            socket.receive(result);
        } catch (IOException e) {
            return null;
        }
        return result;
    }

    /**
     * Determines whether the given packet is a packet requesting
     * this host's information
     * @param packet The packet to parse
     * @return True if packet is requesting this service (follows convention)
     *         False otherwise
     */
    private boolean validPacket(DatagramPacket packet) {
        if (packet == null) {
            return false;
        }
        byte[] data = packet.getData();
        for (int i = 0; i < PACKET_SIZE; i++) {
            System.out.println(String.format("Byte " + i + ": %02x", data[i]));
        }
        return true;
    }

    /**
     * Responds to the source of this packet with information
     * about where to connect to for this server board
     * @param packet Packet to respond to
     */
    private void respond(DatagramPacket packet) {

    }


}
