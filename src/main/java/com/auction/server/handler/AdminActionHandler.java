package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.Item;
import com.auction.common.model.PendingAuctionRequest;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.AuctionStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AdminActionHandler extends AbstractClientActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminActionHandler.class);

    private final PendingAuctionRoomFactory pendingAuctionRoomFactory;

    public AdminActionHandler(PendingAuctionRoomFactory pendingAuctionRoomFactory) {
        super(
                "ADMIN_GET_USERS",
                "ADMIN_GET_AUCTIONS",
                "ADMIN_GET_PENDING_AUCTIONS",
                "ADMIN_APPROVE_AUCTION",
                "ADMIN_REJECT_AUCTION",
                "ADMIN_DELETE_AUCTION",
                "ADMIN_DELETE_USER"
        );
        this.pendingAuctionRoomFactory = pendingAuctionRoomFactory;
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        switch (message.getAction()) {
            case "ADMIN_GET_USERS" -> handleAdminGetUsers(context);
            case "ADMIN_GET_AUCTIONS" -> handleAdminGetAuctions(context);
            case "ADMIN_GET_PENDING_AUCTIONS" -> handleAdminGetPendingAuctions(context);
            case "ADMIN_APPROVE_AUCTION" -> handleAdminApproveAuction(message, context);
            case "ADMIN_REJECT_AUCTION" -> handleAdminRejectAuction(message, context);
            case "ADMIN_DELETE_AUCTION" -> handleAdminDeleteAuction(message, context);
            case "ADMIN_DELETE_USER" -> handleAdminDeleteUser(message, context);
            default -> context.send(new Message("UNKNOWN_ACTION", "SERVER", "Unsupported action: " + message.getAction()));
        }
    }

    private void handleAdminGetUsers(ClientActionContext context) {
        try {
            UserDAO userDAO = new UserDAO();
            List<User> userList = userDAO.getAllUsers();
            context.send(new Message("ADMIN_USER_LIST", "SERVER", userList));
        } catch (Exception e) {
            LOGGER.error("Unable to load user list.", e);
            context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to load user list."));
        }
    }

    private void handleAdminGetAuctions(ClientActionContext context) {
        try {
            AuctionDAO auctionDAO = new AuctionDAO();
            List<AuctionRoom> rooms = auctionDAO.getAllAuctions();
            context.send(new Message("ADMIN_AUCTION_LIST", "SERVER", rooms));
        } catch (Exception e) {
            LOGGER.error("Unable to load auction list.", e);
            context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to load auction list."));
        }
    }

    private void handleAdminGetPendingAuctions(ClientActionContext context) {
        try {
            context.send(new Message(
                    "ADMIN_PENDING_AUCTION_LIST",
                    "SERVER",
                    context.getPendingAuctionApprovalService().getAllPending()
            ));
        } catch (Exception e) {
            LOGGER.error("Unable to load pending auction requests.", e);
            context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to load pending auction requests."));
        }
    }

    private void handleAdminApproveAuction(Message message, ClientActionContext context) {
        try {
            String requestId = message.getData() != null ? message.getData().toString().trim() : "";
            PendingAuctionRequest request = context.getPendingAuctionApprovalService().approve(requestId);
            if (request == null) {
                context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Pending auction request not found."));
                return;
            }

            AuctionRoom room = pendingAuctionRoomFactory.createRoom(request);
            Item item = pendingAuctionRoomFactory.createItem(request);
            AuctionDAO auctionDAO = new AuctionDAO();

            if (!auctionDAO.createAuctionWithItem(room, item, request.getSellerId())) {
                context.getPendingAuctionApprovalService().submit(request);
                context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to approve auction request."));
                return;
            }

            context.send(new Message(
                    "ADMIN_ACTION_SUCCESS",
                    "AUCTION_APPROVED",
                    "Approved auction: " + request.getRoomId()
            ));
            broadcastRoomList(context);
            broadcastPendingAuctionList(context);
        } catch (Exception e) {
            LOGGER.error("Auction approval error.", e);
            context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Auction approval error."));
        }
    }

    private void handleAdminRejectAuction(Message message, ClientActionContext context) {
        try {
            String requestId = message.getData() != null ? message.getData().toString().trim() : "";
            if (!context.getPendingAuctionApprovalService().reject(requestId)) {
                context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Pending auction request not found."));
                return;
            }

            context.send(new Message(
                    "ADMIN_ACTION_SUCCESS",
                    "AUCTION_REJECTED",
                    "Rejected auction request: " + requestId
            ));
            broadcastPendingAuctionList(context);
        } catch (Exception e) {
            LOGGER.error("Auction rejection error.", e);
            context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Auction rejection error."));
        }
    }

    private void handleAdminDeleteAuction(Message message, ClientActionContext context) {
        try {
            String targetRoomId = message.getData() != null ? message.getData().toString() : "";
            AuctionDAO auctionDAO = new AuctionDAO();

            if (auctionDAO.forceDeleteAuction(targetRoomId)) {
                AuctionStateManager.removeState(targetRoomId);
                context.send(new Message(
                        "ADMIN_ACTION_SUCCESS",
                        "AUCTION_DELETED",
                        "Force-canceled auction: " + targetRoomId
                ));
                context.notifyRoomClosed(targetRoomId);
                broadcastRoomList(context);
            } else {
                context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to cancel this auction!"));
            }
        } catch (Exception e) {
            LOGGER.error("Auction cancellation error.", e);
            context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Auction cancellation error!"));
        }
    }

    private void handleAdminDeleteUser(Message message, ClientActionContext context) {
        try {
            String targetUserId = message.getData() != null
                    ? message.getData().toString().trim().toUpperCase()
                    : "";
            UserDAO userDAO = new UserDAO();

            if (userDAO.deleteUser(targetUserId)) {
                context.send(new Message(
                        "ADMIN_ACTION_SUCCESS",
                        "USER_DELETED",
                        "Da xoa tai khoan: " + targetUserId
                ));
                context.notifyDeletedUser(targetUserId);
            } else {
                context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Unable to delete account."));
            }
        } catch (Exception e) {
            LOGGER.error("Account deletion error.", e);
            context.send(new Message("ADMIN_ACTION_FAIL", "SERVER", "Account deletion error!"));
        }
    }
}
