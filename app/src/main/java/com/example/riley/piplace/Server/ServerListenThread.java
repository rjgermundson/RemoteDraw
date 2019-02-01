package com.example.riley.piplace.Server;

import com.example.riley.piplace.Messages.Lines.Line;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listens on the server's socket for clients to connect to
 * Listens on the set BoardServerSocket
 */
public class ServerListenThread extends Thread {
    private static final int MAX_CONNECTIONS = 10;
    private static final ServerPerClientThread[] clients = new ServerPerClientThread[MAX_CONNECTIONS];


    private ServerSocket serverSocket;

    public ServerListenThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        super.run();
        if (serverSocket != null) {
            while (true) {
                try {
                    int id = getID();
                    if (id != -1) {
                        Socket clientSocket = serverSocket.accept();
                        ServerPerClientThread thread = new ServerPerClientThread(clientSocket, id);
                        clients[id] = thread;
                        thread.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Something caused us to fail when listening");
                    break;
                }
            }
        }
    }

    /**
     * Finds an open client id
     * @return id number
     *         -1 if no space open
     */
    private int getID() {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null || !clients[i].isAlive()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sends out the given line to all clients of the server
     * @param line The line to send out
     */
    public static void sendLine(Line line) {
        for (int i = 0; i < clients.length; i++) {
            if (i != line.getID() && clients[i] != null
                && !clients[i].isAlive()) {
                clients[i].sendLine(line);
            }
        }
    }
}
