package com.example.riley.piplace.Server.CommunicateTask;

import com.example.riley.piplace.Utility;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This thread advertises the current hosted board to any client
 * asking for it
 */
public class LobbyAdvertiserThread extends Thread {
    private static final String TEST_SERVER = "10.18.225.176";
    private static final int PACKET_SIZE = 256;
    private static final int BROADCAST_PORT = 5600;
    private boolean running = true;
    private DatagramSocket socket;
    private String name;
    private InetAddress host;
    private int port;

    private LobbyAdvertiserThread(DatagramSocket socket, InetAddress host, int port, String name) {
        this.socket = socket;
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public static LobbyAdvertiserThread createThread(InetAddress host, int port, String name) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setReuseAddress(true);
            return new LobbyAdvertiserThread(socket, host, port, name);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void run() {
        alertLANServer();
        while (running) {
            DatagramPacket packet = listen();
            if (validPacket(packet)) {
                respond(packet);
            }
        }
    }

    public void close() {
        this.running = false;
        // Todo Alert LAN server of closing
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
        } else if (packet.getAddress().equals(host)) {
            System.out.println("Same as host");
        }
        byte[] data = packet.getData();
        for (int i = 0; i < PACKET_SIZE; i++) {
            if (data[i] != (byte) i) {
                return false;
            }
        }
        return true;
    }

    /**
     * Responds to the source of this packet with information
     * about where to connect to for this server board
     * @param packet Packet to respond to
     */
    private void respond(DatagramPacket packet) {
        byte[] data = packet.getData();
        InetAddress clientAddress = packet.getAddress();
        int clientPort = packet.getPort();
        System.out.println("PORT: " + clientPort);
        System.out.println("ADDRESS: " + clientAddress);
        // TODO: Finish response for UDP Broadcast
    }

    // Alerts the LAN server hosted on the UW LAN network
    // because they seem to have disabled multicast and UDP broadcast
    private void alertLANServer() {
        InetAddress server;
        int port = 5050;
        try {
            server = InetAddress.getByName(TEST_SERVER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        byte[] byteInfo = new byte[PACKET_SIZE];
        byteInfo[0] = (byte) 0x80;
        System.out.println((int) byteInfo[0]);
        System.arraycopy(host.getAddress(), 0, byteInfo, 8, 4);
        System.arraycopy(Utility.intToBytes(port), 0, byteInfo, 12, 4);
        System.arraycopy(Utility.intToBytes(0), 0, byteInfo, 16, 4);
        System.arraycopy(Utility.intToBytes(10), 0, byteInfo, 20, 4);
        System.out.println(name);
        byte[] nameBytes = name.getBytes();
        for (int i = 0; i < nameBytes.length; i++) {
            byteInfo[i + 24] = nameBytes[i];
        }
        for (int i = 56; i < byteInfo.length; i++) {
            byteInfo[i] = (byte) i;
        }
        DatagramPacket info = new DatagramPacket(byteInfo, PACKET_SIZE, server, port);
        try {
            socket.send(info);
            System.out.println("SENT PACKET");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
