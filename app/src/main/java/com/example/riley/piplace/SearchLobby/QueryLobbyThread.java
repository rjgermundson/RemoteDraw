package com.example.riley.piplace.SearchLobby;

import android.os.Message;

import com.example.riley.piplace.BoardActivity.BoardActivity;
import com.example.riley.piplace.BoardActivity.ServerBoardActivity;
import com.example.riley.piplace.Utility;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * This thread handles asking for servers to connect to
 * Also handles responses from potential lobbies
 */
public class QueryLobbyThread extends Thread {
    private static final String TEST_SERVER = "10.18.225.176";
    private static final int PACKET_SIZE = 256;
    private static final int LISTEN_PORT = 5200;

    private WeakReference<SearchLobbyActivity> activity;
    private DatagramSocket socket;
    private boolean running = true;

    private QueryLobbyThread(DatagramSocket socket, SearchLobbyActivity activity) {
        this.activity = new WeakReference<>(activity);
        this.socket = socket;
    }

    public static QueryLobbyThread createThread(InetAddress host, SearchLobbyActivity activity) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setReuseAddress(true);
            return new QueryLobbyThread(socket, activity);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void run() {
        while (running) {
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
     * End the current thread;
     */
    public void close() {
        this.running = false;
    }

    /**
     * Broadcast to ask for any server on the LAN for their IP and port
     */
    private void ping() {
        DatagramPacket serverPacket;
        try {
            serverPacket = new QueryLobbyDatagram().toServerPacket(InetAddress.getByName(TEST_SERVER), 5050);
            socket.send(serverPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DatagramPacket queryPacket = new QueryLobbyDatagram().toBroadcastPacket();
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
        DatagramPacket rec = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        try {
            socket.receive(rec);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        List<LobbyInfo> infoList = parse(rec);
        for (LobbyInfo lobby : infoList) {
            activity.get().addLobby(lobby);
            System.out.println(lobby.hashCode());
            Message message = new Message();
            message.what = SearchLobbyActivity.MESSAGE_INSERT_LIST;
            activity.get().updateListHandler.sendMessage(message);
        }
    }

    private List<LobbyInfo> parse(DatagramPacket receive) {
        List<LobbyInfo> result = new LinkedList<>();
        byte[] data = receive.getData();
        byte[] totalPackets = new byte[4];
        byte[] totalInPacket = new byte[4];
        System.arraycopy(data, 0, totalPackets, 0, totalPackets.length);
        System.arraycopy(data, 4, totalInPacket, 0, totalInPacket.length);
        for (int i = 0; i < Utility.bytesToInt(totalInPacket); i++) {
            byte[] IP = new byte[4];
            byte[] portBytes = new byte[4];
            byte[] countBytes = new byte[4];
            byte[] limitBytes = new byte[4];
            byte[] nameBytes = new byte[32];
            System.arraycopy(data, 8, IP, 0, IP.length);
            System.arraycopy(data, 12, portBytes, 0, portBytes.length);
            System.arraycopy(data, 16, countBytes, 0, countBytes.length);
            System.arraycopy(data, 20, limitBytes, 0, limitBytes.length);
            System.arraycopy(data, 24, nameBytes, 0, nameBytes.length);
            int port = Utility.bytesToInt(portBytes);
            int count = Utility.bytesToInt(countBytes);
            int limit = Utility.bytesToInt(limitBytes);
            LobbyInfo lobby = new LobbyInfo(new String(nameBytes), count, limit, IP, port);
            result.add(lobby);
        }
        return result;
    }
}
