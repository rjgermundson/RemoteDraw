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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This thread handles asking for servers to connect to
 * Also handles responses from potential lobbies
 */
public class QueryLobbyThread extends Thread {
    private static final String TEST_SERVER = "10.19.122.92";
    private static final int PACKET_SIZE = 256;
    private static final int PER_SERVER_SPACE = 48;
    private static final int LISTEN_PORT = 5200;

    private WeakReference<SearchLobbyActivity> activity;
    private DatagramSocket socket;
    private boolean runnable = true;
    private boolean paused = false;

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
        while (runnable) {
            if (!paused) {
                ping();
                receive();
            }
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
        this.runnable = false;
    }

    /**
     * Pause the thread from sending on network
     */
    public void pause() {
        this.paused = true;
    }

    /**
     * Unpause the thread from sending on network
     */
    public void unpause() {
        this.paused = false;
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
        System.out.println("PARSING RECEIVED PACKET");
        List<LobbyInfo> infoList = parse(rec);
        for (LobbyInfo lobby : infoList) {
            Message message = new Message();
            if (lobby.getDelete()) {
                activity.get().removeLobby(lobby);
                message.what = SearchLobbyActivity.MESSAGE_REMOVE_LIST;
            } else {
                activity.get().addLobby(lobby);
                message.what = SearchLobbyActivity.MESSAGE_INSERT_LIST;
            }
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
        int deleteFlag = 1;
        for (int i = 0; i < Utility.bytesToInt(totalInPacket); i++) {
            System.out.println(String.format("%02x : %02x", data[9], deleteFlag));
            boolean delete = (data[9] & deleteFlag) == 1;
            deleteFlag = deleteFlag << 1;
            byte[] IP = new byte[4];
            byte[] portBytes = new byte[4];
            byte[] countBytes = new byte[4];
            byte[] limitBytes = new byte[4];
            byte[] nameBytes = new byte[32];
            System.arraycopy(data, 16 + PER_SERVER_SPACE * i, IP, 0, IP.length);
            System.arraycopy(data, 20 + PER_SERVER_SPACE * i, portBytes, 0, portBytes.length);
            System.arraycopy(data, 24 + PER_SERVER_SPACE * i, countBytes, 0, countBytes.length);
            System.arraycopy(data, 28 + PER_SERVER_SPACE * i, limitBytes, 0, limitBytes.length);
            System.arraycopy(data, 32 + PER_SERVER_SPACE * i, nameBytes, 0, nameBytes.length);
            int port = Utility.bytesToInt(portBytes);
            int count = Utility.bytesToInt(countBytes);
            int limit = Utility.bytesToInt(limitBytes);
            LobbyInfo lobby = new LobbyInfo(new String(nameBytes), count, limit, IP, port, delete);
            result.add(lobby);
        }
        return result;
    }
}
