package com.auction.server.main;

import com.auction.common.dto.Message;
import com.auction.server.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionServer {

    public static List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        int port = 8080;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("🚀 Server running on port: " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(socket);
                addClient(handler);

                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (Exception e) {
            System.out.println("❌ Server error");
            e.printStackTrace();
        }
    }

    // ================== BROADCAST ==================

    public static void broadcast(Message msg) {
        System.out.println("📡 Broadcast: " + msg.getAction());

        for (ClientHandler client : clients) {
            if (client != null && client.isAlive()) {
                client.sendMessage(msg);
            } else {
                removeClient(client);
            }
        }
    }

    public static void broadcastAll(Message msg) {
        for (ClientHandler client : clients) {
            if (client != null && client.isAlive()) {
                client.sendMessage(msg);
            } else {
                removeClient(client);
            }
        }
    }

    public static void broadcastToRoom(String roomId, Message msg) {
        for (ClientHandler client : clients) {

            if (client == null || !client.isAlive()) {
                removeClient(client);
                continue;
            }

            String clientRoom = client.getCurrentRoomId();

            if (roomId != null && roomId.equals(clientRoom)) {
                client.sendMessage(msg);
            }
        }
    }

    // ================== CLIENT MANAGER ==================

    public static void addClient(ClientHandler client) {
        if (client != null) {
            clients.add(client);
            System.out.println("✅ Client connected | Online: " + clients.size());
        }
    }

    public static void removeClient(ClientHandler client) {
        if (client != null) {
            clients.remove(client);
            client.closeConnection();
            System.out.println("❌ Client disconnected | Online: " + clients.size());
        }
    }
}