package com.example.riley.piplace.SearchLobby;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.riley.piplace.R;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class LobbyListAdapter extends RecyclerView.Adapter<LobbyListAdapter.LobbyViewHolder> {
    private WeakReference<SearchLobbyActivity> activity;
    private List<LobbyInfo> lobbies;

    public LobbyListAdapter(SearchLobbyActivity lobbyActivity) {
        this.activity = new WeakReference<>(lobbyActivity);
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
        LobbyInfo info = lobbies.get(position);
        holder.nameText.setText(info.getName());
        holder.countText.setText(Integer.toString(info.getCount()));
    }

    @Override
    public int getItemCount() {
        return lobbies.size();
    }

    /**
     * Adds the given lobby to the list of lobbies
     * @param lobby Lobby to add to list
     */
    public void addLobby(LobbyInfo lobby) {
        lobbies.add(lobby);
        notifyItemInserted(lobbies.size());
    }

    class LobbyViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView nameText;
        TextView countText;

        LobbyViewHolder(View view) {
            super(view);
            this.view = view;
            this.nameText = view.findViewById(R.id.lobby_name);
            this.countText = view.findViewById(R.id.lobby_count);
        }

    }
}
