package com.example.riley.piplace.MainActivity;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.riley.piplace.BoardActivity.BoardActivity;
import com.example.riley.piplace.R;

public class MainActivity extends AppCompatActivity {
    private static final int PORT = 5050;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        setButton();
    }

    /**
     * Initialize the connect button
     */
    private void setButton() {
        Button connect = findViewById(R.id.connect_button);
        final EditText hostText = findViewById(R.id.host_input);
        connect.setOnClickListener(new ConnectOnClickListener(this, hostText));
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

    public void failedToOpenBoard(String host) {
        Toast.makeText(this, "Failed to connect to " + host, Toast.LENGTH_SHORT).show();
    }

    public void openBoard(String host) {
        startActivity(new Intent(this, BoardActivity.class));
    }
}
