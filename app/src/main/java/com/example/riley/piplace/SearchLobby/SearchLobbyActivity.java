package com.example.riley.piplace.SearchLobby;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.riley.piplace.Client.CommunicateTask.QueryLobbyThread;
import com.example.riley.piplace.R;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class SearchLobbyActivity extends AppCompatActivity {
    private LobbyListAdapter lobbyAdapter;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.search_lobby_activity);
        setList();
        setQuery();
    }

    /**
     * Adds the given lobby to the current lobby list
     */
    public void addLobby(LobbyInfo info) {
        lobbyAdapter.addLobby(info);
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
        byte[] ipBytes = BigInteger.valueOf(ip).toByteArray();

        try {
            for (int i = 0; i < ipBytes.length; i++) {
                System.out.println(String.format("%02x", ipBytes[i]));
            }
            InetAddress host = InetAddress.getByAddress(ipBytes);
            QueryLobbyThread queryThread = QueryLobbyThread.createThread(host);
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
}
