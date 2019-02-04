package com.example.riley.piplace.Client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * This object produces a Datagram that meets requirements for a
 * Datagram to be broadcast in order to find open lobbies
 */
public class QueryLobbyDatagram {
    private static final int PACKET_SIZE = 256;
    private static final int BROADCAST_PORT = 5200;
    private byte[] data;

    public QueryLobbyDatagram() {
        data = new byte[PACKET_SIZE];
        initializeData();
    }

    /**
     * Returns a valid DatagramPacket that will be responded to by a server
     * @return A valid DatagramPacket following drawing board conventions
     */
    public DatagramPacket toDatagramPacket() {
        InetAddress broadcast;
        try {
            broadcast = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            return null;
        }
        return new DatagramPacket(Arrays.copyOf(data, PACKET_SIZE), PACKET_SIZE,
                                  broadcast, BROADCAST_PORT);
    }

    /**
     * Initialize the data of the datagram to be sent to match
     * client conventions
     */
    private void initializeData() {
        for (int i = 0; i < PACKET_SIZE; i++) {
            data[i] = (byte) i;
        }
    }
}
