package com.example.riley.piplace.SearchLobby;

import java.util.Arrays;

/**
 * This class holds the lobby information taken from a server
 */
public class LobbyInfo {
    private String name;
    private int count;
    private int limit;
    private byte[] address;
    private int port;
    private boolean delete;


    LobbyInfo(String name, int count, int limit, byte[] address, int port, boolean delete) {
        this.name = name;
        this.count = count;
        this.limit = limit;
        this.address = address;
        this.port = port;
        this.delete = delete;
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

    public int getPort() {
        return port;
    }

    public boolean getDelete() { return delete; }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < name.length(); i++) {
            result += i * 37 * name.charAt(i);
        }
        result = result + port + count + address[0] + address[1];
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LobbyInfo)) {
            return false;
        }
        LobbyInfo other = (LobbyInfo) o;

        if (!this.name.equals(other.name)) {
            return false;
        }
        return Arrays.equals(this.address, other.address);
    }
}
