package com.auction.server.service;

import com.auction.client.feature.controllers.AuctionController;
import com.auction.common.dto.Message;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection {
    public static String currentUser = null;

    private final String host = "localhost";
    private final int port = 8080;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final AuctionController controller;

    public ClientConnection(AuctionController controller) {
        this.controller = controller;
    }

    public void connect() {
        new Thread(() -> {
            try {
                controller.updateConnectionStatus("Connecting...");

                socket = new Socket(host, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                controller.updateConnectionStatus("Connected to server!");

                while (!Thread.currentThread().isInterrupted() && socket != null && !socket.isClosed()) {
                    Message response = (Message) in.readObject();
                    System.out.println("[ClientConnection] Received action: " + response.getAction());
                    controller.onServerResponse(response);
                }
            } catch (java.net.SocketException se) {
                if (se.getMessage() != null && se.getMessage().toLowerCase().contains("socket closed")) {
                    System.out.println("[ClientConnection] Network connection closed safely.");
                } else {
                    System.err.println("[ClientConnection] Lost connection to server: " + se.getMessage());
                }
            } catch (EOFException eof) {
                System.out.println("[ClientConnection] Server closed the connection.");
            } catch (Exception e) {
                System.err.println("[ClientConnection] Reader thread error: " + e.getMessage());
            }
        }).start();
    }

    public void sendMessage(Message msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
                out.reset();
            } else {
                System.err.println("[ClientConnection] ObjectOutputStream is not initialized.");
            }
        } catch (Exception e) {
            System.err.println("[ClientConnection] Failed to send message to server:");
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("[ClientConnection] Disconnected from server safely.");
        } catch (Exception e) {
            System.err.println("[ClientConnection] Failed to close connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
