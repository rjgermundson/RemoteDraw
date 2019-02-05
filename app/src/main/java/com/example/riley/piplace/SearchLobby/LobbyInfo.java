package com.example.riley.piplace.SearchLobby;

/**
 * This class holds the lobby information taken from a server
 */
public class LobbyInfo {
    private String name;
    private int count;
    private int limit;
    private byte[] address;


    public LobbyInfo(String name, int count, int limit, byte[] address) {
        this.name = name;
        this.count = count;
        this.limit = limit;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public int getLimit() {
        return limit;
    }

    public byte[] getAddress() {
        return address;
    }
}
