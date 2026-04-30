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

/**
 * Đại diện cho một kết nối socket từ client.
 * Mỗi ClientHandler đọc Message từ client, chuyển cho router xử lý và gửi response ngược lại.
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    // Context giữ userId/currentRoomId và các service cần dùng trong suốt vòng đời kết nối.
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
        // Router gom các nhóm handler theo nghiệp vụ: auth, room, seller, admin.
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

                // Message hợp lệ được chuyển cho router dựa trên action.
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

            // reset() tránh ObjectOutputStream gửi lại object cache cũ khi nội dung Message thay đổi.
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
        // Rời phòng hiện tại để các broadcast sau không còn gửi nhầm tới client đã ngắt.
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
