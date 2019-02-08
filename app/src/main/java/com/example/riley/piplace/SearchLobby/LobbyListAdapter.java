package com.example.riley.piplace.SearchLobby;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riley.piplace.ConnectTask;
import com.example.riley.piplace.R;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LobbyListAdapter extends RecyclerView.Adapter<LobbyListAdapter.LobbyViewHolder> {
    private WeakReference<SearchLobbyActivity> activity;
    private Set<LobbyInfo> lobbyInfoSet;
    private List<LobbyInfo> lobbies;

    public LobbyListAdapter(SearchLobbyActivity lobbyActivity) {
        this.activity = new WeakReference<>(lobbyActivity);
        this.lobbyInfoSet = new HashSet<>();
        this.lobbies = new LinkedList<>();
    }

    @NonNull
    @Override
    public LobbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.lobby_holder, parent, false);
        return new LobbyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LobbyViewHolder holder, int position) {
        final LobbyInfo info = lobbies.get(position);
        holder.nameText.setText(info.getName());
        holder.countText.setText(Integer.toString(info.getCount()) + " / " + info.getLimit());
        try {
            holder.addressText.setText("" + InetAddress.getByAddress(info.getAddress()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = info.getName();
                InetAddress address = null;
                try {
                    address = InetAddress.getByAddress(info.getAddress());
                } catch (UnknownHostException e) {
                    toast("Failed to connect to " + info.getName());
                    return;
                }
                int port = info.getPort();
                ConnectTask connectTask = new ConnectTask(activity.get(), address, port);
                connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lobbyInfoSet.size();
    }

    /**
     * Adds the given lobby to the list of lobbies
     * @param lobby Lobby to add to list
     */
    public void addLobby(LobbyInfo lobby) {
        if (lobbyInfoSet.add(lobby)) {
            lobbies.add(lobby);
        }
    }

    /**
     * Removes the given lobby from the list of lobbies
     * @param info Lobby to remove from the list
     */
    public void removeLobby(LobbyInfo info) {
        lobbyInfoSet.remove(info);
        lobbies.remove(info);
    }

    private void toast(String message) {
        Toast.makeText(activity.get().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    class LobbyViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView nameText;
        TextView countText;
        TextView addressText;

        LobbyViewHolder(View view) {
            super(view);
            this.view = view;
            this.nameText = view.findViewById(R.id.lobby_name);
            this.countText = view.findViewById(R.id.lobby_count);
            this.addressText = view.findViewById(R.id.lobby_address);
        }
    }
}
