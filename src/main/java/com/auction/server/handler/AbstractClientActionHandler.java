package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.server.dao.AuctionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public abstract class AbstractClientActionHandler implements ClientActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClientActionHandler.class);

    private final Set<String> supportedActions;

    protected AbstractClientActionHandler(String... supportedActions) {
        this.supportedActions = Set.of(supportedActions);
    }

    @Override
    public boolean canHandle(String action) {
        return action != null && supportedActions.contains(action);
    }

    protected void broadcastRoomList(ClientActionContext context) {
        try {
            AuctionDAO auctionDAO = new AuctionDAO();
            List<AuctionRoom> rooms = auctionDAO.getAllActiveAuctions();
            context.broadcastAll(new Message("ROOM_LIST", "SERVER", rooms));
        } catch (Exception e) {
            LOGGER.error("Failed to broadcast room list.", e);
        }
    }

    protected void broadcastPendingAuctionList(ClientActionContext context) {
        try {
            context.broadcastAll(new Message(
                    "ADMIN_PENDING_AUCTION_LIST",
                    "SERVER",
                    context.getPendingAuctionApprovalService().getAllPending()
            ));
        } catch (Exception e) {
            LOGGER.error("Failed to broadcast pending auction list.", e);
        }
    }

    protected double parseBidAmount(Object data) {
        if (data instanceof Double d) {
            return d;
        }
        if (data instanceof Integer i) {
            return i.doubleValue();
        }
        if (data instanceof Long l) {
            return l.doubleValue();
        }
        return Double.parseDouble(data.toString().trim());
    }

    protected String generateId(String prefix, int digits) {
        long modulo = 1L;
        for (int i = 0; i < digits; i++) {
            modulo *= 10;
        }

        long value = System.currentTimeMillis() % modulo;
        return prefix + String.format("%0" + digits + "d", value);
    }
}
