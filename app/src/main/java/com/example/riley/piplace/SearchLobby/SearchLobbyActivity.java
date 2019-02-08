package com.example.riley.piplace.SearchLobby;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.riley.piplace.R;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Arrays;

public class SearchLobbyActivity extends AppCompatActivity {
    public static final int MESSAGE_INSERT_LIST = 30;
    public static final int MESSAGE_REMOVE_LIST = 31;
    public UpdateListHandler updateListHandler;
    private LobbyListAdapter lobbyAdapter;
    private QueryLobbyThread queryThread;
    private byte[] address;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.search_lobby_activity);
        setList();
        UpdateListHandler.initialize(lobbyAdapter);
        updateListHandler = UpdateListHandler.getInstance();
        setQuery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (queryThread != null) {
            queryThread.close();
        }
    }

    /**
     * Adds the given lobby to the current lobby list
     * @param info LobbyInfo to add
     */
    public void addLobby(LobbyInfo info) {
        if (!Arrays.equals(info.getAddress(), address)) {
            lobbyAdapter.addLobby(info);
        }
    }

    /**
     * Removes the given lobby from the current lobby list
     * @param info LobbyInfo to remove
     */
    public void removeLobby(LobbyInfo info) {
        lobbyAdapter.removeLobby(info);
    }

    private void setList() {
        RecyclerView lobbyList = findViewById(R.id.available_lobby_list);
        lobbyList.setLayoutManager(new LinearLayoutManager(this));
        lobbyList.setItemAnimator(new DefaultItemAnimator());
        lobbyAdapter = new LobbyListAdapter(this);
        lobbyList.setAdapter(lobbyAdapter);
    }

    private void setQuery() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }
        address = BigInteger.valueOf(ip).toByteArray();

        try {
            InetAddress host = InetAddress.getByAddress(address);
            queryThread = QueryLobbyThread.createThread(host, this);
            if (queryThread != null) {
                queryThread.start();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to create query thread", Toast.LENGTH_SHORT).show();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failed to create query thread", Toast.LENGTH_SHORT).show();
        }
    }

    public static class UpdateListHandler extends Handler {
        private static UpdateListHandler instance;
        private LobbyListAdapter lobbyAdapter;

        private UpdateListHandler(LobbyListAdapter lobbyAdapter) {
            this.lobbyAdapter = lobbyAdapter;
        }

        /**
         * Initialize the BoardUpdateHandler
         */
        static boolean initialize(LobbyListAdapter lobbyAdapter) {
            if (lobbyAdapter == null) {
                return false;
            }
            instance = new UpdateListHandler(lobbyAdapter);
            return true;
        }

        /**
         * Get the instance of the handler
         * @return The instance of the handler
         *         Null if initialize not called
         */
        static UpdateListHandler getInstance() {
            if (instance == null) {
                // Not initialized
                return null;
            }
            return instance;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_INSERT_LIST) {
                lobbyAdapter.notifyItemInserted(lobbyAdapter.getItemCount());
            } else if (msg.what == MESSAGE_REMOVE_LIST) {
                lobbyAdapter.notifyItemRemoved(0);
            }
        }
    }
}
