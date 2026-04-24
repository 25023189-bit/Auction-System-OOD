package com.auction.server.main;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.server.ClientHandler;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AuctionRoomService;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionServer {

    private static final List<ClientHandler> CLIENTS = new CopyOnWriteArrayList<>();
    private static final ScheduledExecutorService AUCTION_WATCHER = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        int port = 8080;

        try {
            startExpiredAuctionWatcher();

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server running on port: " + port);
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();

                    ClientHandler handler = new ClientHandler(socket);
                    addClient(handler);

                    Thread thread = new Thread(handler);
                    thread.start();
                }
            }

        } catch (Exception e) {
            System.out.println("Server error");
            e.printStackTrace();
        }
    }

    public static void broadcast(Message msg) {
        System.out.println("Broadcast: " + msg.getAction());

        for (ClientHandler client : CLIENTS) {
            if (client != null && client.isAlive()) {
                client.sendMessage(msg);
            } else {
                removeClient(client);
            }
        }
    }

    public static void broadcastAll(Message msg) {
        for (ClientHandler client : CLIENTS) {
            if (client != null && client.isAlive()) {
                client.sendMessage(msg);
            } else {
                removeClient(client);
            }
        }
    }

    public static void broadcastToRoom(String roomId, Message msg) {
        for (ClientHandler client : CLIENTS) {
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

    public static void notifyRoomClosed(String roomId) {
        Message message = new Message("AUCTION_CLOSED_NOTIFY", "SERVER", roomId);
        for (ClientHandler client : CLIENTS) {
            if (client == null || !client.isAlive()) {
                removeClient(client);
                continue;
            }

            if (roomId != null && roomId.equals(client.getCurrentRoomId())) {
                client.sendMessage(message);
                client.leaveCurrentRoomIfMatches(roomId);
            }
        }
    }

    public static void addClient(ClientHandler client) {
        if (client != null) {
            CLIENTS.add(client);
            System.out.println("Client connected | Online: " + CLIENTS.size());
        }
    }

    public static void removeClient(ClientHandler client) {
        if (client != null) {
            client.closeConnection();
        }
    }

    public static void unregisterClient(ClientHandler client) {
        if (client != null && CLIENTS.remove(client)) {
            System.out.println("Client disconnected | Online: " + CLIENTS.size());
        }
    }

    public static void notifyDeletedUser(String targetUserId) {
        if (targetUserId == null || targetUserId.isBlank()) {
            return;
        }

        for (ClientHandler client : CLIENTS) {
            if (client != null && targetUserId.equals(client.getUserId())) {
                client.sendMessage(new Message("BANNED", "SERVER", "Your account has been deleted by an admin!"));
                client.closeConnection();
                break;
            }
        }
    }

    private static void startExpiredAuctionWatcher() {
        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionRoomService roomService = new AuctionRoomService();

        AUCTION_WATCHER.scheduleAtFixedRate(() -> {
            try {
                List<AuctionRoom> activeRooms = auctionDAO.getAllActiveAuctions();
                for (AuctionRoom room : activeRooms) {
                    if (room != null && room.getRoomId() != null) {
                        roomService.finalizeExpiredAuctionIfNeeded(room.getRoomId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Auction watcher error: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
}
