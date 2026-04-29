package com.auction.server;

import com.auction.common.dto.Message;
import com.auction.server.handler.*;
import com.auction.server.main.AuctionServer;
import com.auction.server.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final ClientActionContext actionContext;
    private final ClientActionRouter actionRouter;

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private volatile boolean alive = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.actionContext = new ClientActionContext(
                this::sendMessage,
                new AuctionServerEventPublisher(),
                new AuthService(),
                new AuctionRoomService(),
                new PendingAuctionApprovalService()
        );
        this.actionRouter = new ClientActionRouter(List.of(
                new AuthActionHandler(),
                new RoomActionHandler(),
                new SellerActionHandler(),
                new AdminActionHandler(new PendingAuctionRoomFactory())
        ));

        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            LOGGER.error("Error creating communication streams with client.", e);
            alive = false;
        }
    }

    public String getCurrentRoomId() {
        return actionContext.getCurrentRoomId();
    }

    public String getUserId() {
        return actionContext.getUserId();
    }

    public boolean isAlive() {
        return alive && socket != null && !socket.isClosed();
    }

    public void leaveCurrentRoomIfMatches(String roomId) {
        actionContext.leaveCurrentRoomIfMatches(roomId);
    }

    @Override
    public void run() {
        try {
            while (alive) {
                Object raw = in.readObject();
                if (!(raw instanceof Message msg)) {
                    continue;
                }

                if (msg.getAction() == null) {
                    continue;
                }

                actionRouter.route(msg, actionContext);
            }
        } catch (Exception e) {
            LOGGER.info("Client disconnected: {} | {}", getUserId(), e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public synchronized void sendMessage(Message response) {
        try {
            if (!isAlive()) {
                return;
            }

            out.writeObject(response);
            out.flush();
            out.reset();
        } catch (Exception e) {
            closeConnection();
        }
    }

    public synchronized void closeConnection() {
        boolean wasAlive = alive;
        alive = false;
        actionContext.clearCurrentRoom();

        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {
        }

        if (wasAlive) {
            AuctionServer.removeClient(this);
        }
    }
}
