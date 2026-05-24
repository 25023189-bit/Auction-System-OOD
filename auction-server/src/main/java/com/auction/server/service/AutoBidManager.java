package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;
import com.auction.server.handler.ClientActionContext;

import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBidManager {

    public static class AutoBidAgent {
        private final String userId;
        private final double maxBid;
        private final double increment;

        public AutoBidAgent(String userId, double maxBid, double increment) {
            this.userId = userId;
            this.maxBid = maxBid;
            this.increment = increment;
        }

        public String getUserId() { return userId; }
        public double getMaxBid() { return maxBid; }
        public double getIncrement() { return increment; }
    }

    private final ConcurrentHashMap<String, PriorityQueue<AutoBidAgent>> roomQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, AutoBidAgent>> roomAgents = new ConcurrentHashMap<>();

    public synchronized void registerAutoBid(String roomId, String userId, double maxBid, double increment) {
        roomQueues.putIfAbsent(roomId, new PriorityQueue<>((a, b) -> Double.compare(b.getMaxBid(), a.getMaxBid())));
        roomAgents.putIfAbsent(roomId, new ConcurrentHashMap<>());

        PriorityQueue<AutoBidAgent> queue = roomQueues.get(roomId);
        ConcurrentHashMap<String, AutoBidAgent> agents = roomAgents.get(roomId);

        AutoBidAgent oldAgent = agents.get(userId);
        if (oldAgent != null) {
            queue.remove(oldAgent);
        }

        AutoBidAgent newAgent = new AutoBidAgent(userId, maxBid, increment);
        agents.put(userId, newAgent);
        queue.add(newAgent);
    }

    public synchronized void cancelAutoBid(String roomId, String userId) {
        ConcurrentHashMap<String, AutoBidAgent> agents = roomAgents.get(roomId);
        PriorityQueue<AutoBidAgent> queue = roomQueues.get(roomId);

        if (agents != null && queue != null) {
            AutoBidAgent agent = agents.remove(userId);
            if (agent != null) {
                queue.remove(agent);
            }
        }
    }

    public synchronized void runAutoBiddingEngine(AuctionRoom room, ClientActionContext context) {
        String roomId = room.getRoomId();
        PriorityQueue<AutoBidAgent> queue = roomQueues.get(roomId);

        if (queue == null || queue.isEmpty()) return;

        boolean priceChanged;

        do {
            priceChanged = false;

            double currentPrice = room.getCurrentPrice();
            String currentWinner = room.getHighestBidder();

            AutoBidAgent[] activeAgents = queue.toArray(new AutoBidAgent[0]);
            UserDAO userDAO = new UserDAO();

            for (AutoBidAgent agent : activeAgents) {

                User agentUser = userDAO.getUserById(agent.getUserId());
                String agentUsername = (agentUser != null && agentUser.getUsername() != null)
                        ? agentUser.getUsername()
                        : agent.getUserId();

                // =========================================================
                // BẢO MẬT 1: CHỐNG TỰ ĐÈ GIÁ BẢN THÂN
                // Cắt khoảng trắng (trim) và bỏ qua chữ Hoa/Thường để so sánh chuẩn 100%
                // =========================================================
                String safeWinner = currentWinner != null ? currentWinner.trim() : "";
                String safeAgent = agentUsername != null ? agentUsername.trim() : "";

                if (safeAgent.equalsIgnoreCase(safeWinner)) {
                    continue; // Đang Top 1 rồi, ngưng đấm!
                }

                // =========================================================
                // BẢO MẬT 2: KIỂM TRA BƯỚC GIÁ TỐI THIỂU
                // =========================================================
                double roomMinStep = room.getBidStep();
                if (agent.getIncrement() < roomMinStep) {
                    continue;
                }

                double nextPrice = currentPrice + agent.getIncrement();

                // =========================================================
                // BẢO MẬT 3: KIỂM TRA NGÂN SÁCH MAX BID
                // =========================================================
                if (nextPrice <= agent.getMaxBid()) {

                    com.auction.common.dto.Message bidResult = context.getRoomService().placeNewBid(roomId, agent.getUserId(), nextPrice);

                    if ("BID_SUCCESS".equals(bidResult.getAction()) || "BID_SUCCESS_EXTENDED".equals(bidResult.getAction())) {
                        room = (AuctionRoom) bidResult.getData();
                        priceChanged = true;
                        context.broadcastToRoom(roomId, bidResult);
                        context.broadcastAll(new Message("UPDATE_PRICE", "SERVER", roomId + "|" + nextPrice));

                        break; // Đấm thành công, chốt giá và quay lại vòng lặp!
                    }
                }
            }
        } while (priceChanged);
    }
}
