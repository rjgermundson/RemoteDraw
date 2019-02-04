package com.example.riley.piplace.MainActivity;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.riley.piplace.BoardActivity.ClientBoardActivity;
import com.example.riley.piplace.BoardActivity.ServerBoardActivity;
import com.example.riley.piplace.Client.CommunicateTask.QueryLobbyThread;
import com.example.riley.piplace.R;
import com.example.riley.piplace.Server.CommunicateTask.HostTask;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    private static final int PORT = 5665;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        setConnectButton();
        setHostButton();
        setQuery();
    }

    /**
     * Initialize the connect button
     */
    private void setConnectButton() {
        Button connect = findViewById(R.id.connect_button);
        final EditText hostText = findViewById(R.id.host_input);
        connect.setOnClickListener(new ConnectOnClickListener(this, hostText));
    }

    /**
     * Initialize the host button
     */
    private void setHostButton() {
        Button host = findViewById(R.id.host_button);
        host.setOnClickListener(new HostClickListener(this));
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

    private class ConnectOnClickListener implements View.OnClickListener {
        private MainActivity mainActivity;
        private EditText hostText;

        ConnectOnClickListener(MainActivity activity, EditText hostText) {
            this.mainActivity = activity;
            this.hostText = hostText;
        }

        @Override
        public void onClick(View v) {
            String host = hostText.getText().toString();
            ConnectTask task = new ConnectTask(mainActivity, host, PORT);
            task.execute();
        }
    }

    private class HostClickListener implements View.OnClickListener {
        private MainActivity mainActivity;

        HostClickListener(MainActivity activity) {
            this.mainActivity = activity;
        }

        @Override
        public void onClick(View v) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                ip = Integer.reverseBytes(ip);
            }
            byte[] ipBytes = BigInteger.valueOf(ip).toByteArray();

            String hostIP = "";
            try {
                hostIP = InetAddress.getByAddress(ipBytes).getHostAddress();
                HostTask task = new HostTask(mainActivity, hostIP, PORT);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                failedToHost();
            }
        }
    }

    public void failedToOpenBoard(String host) {
        Toast.makeText(this, "Failed to connect to " + host, Toast.LENGTH_SHORT).show();
    }

    public void failedToHost() {
        Toast.makeText(this, "Failed to open server", Toast.LENGTH_SHORT).show();
    }

    public void openClientBoard() {
        startActivity(new Intent(this, ClientBoardActivity.class));
    }

    public void openServerBoard(String IP, int port) {
        Bundle bundle = new Bundle();
        bundle.putString("IP", IP);
        bundle.putInt("PORT", port);
        Intent intent = new Intent(this, ServerBoardActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
