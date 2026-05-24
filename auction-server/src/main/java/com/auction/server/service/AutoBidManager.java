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

    // ====================================================================
    // ĐÂY LÀ HÀM XỬ LÝ AUTO-BID ĐÃ ĐƯỢC BỌC LẠI CẨN THẬN
    // ====================================================================
    public synchronized void triggerAutoBids(String roomId, AuctionRoom room, ClientActionContext context) {
        PriorityQueue<AutoBidAgent> queue = roomQueues.get(roomId);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        boolean priceChanged;

        do {
            priceChanged = false;
            double currentPrice = room.getCurrentPrice();

            // LẤY TRỰC TIẾP ID NGƯỜI CHIẾN THẮNG (Không thèm lấy tên nữa)
            String currentWinnerId = room.getHighestBidder();

            AutoBidAgent[] activeAgents = queue.toArray(new AutoBidAgent[0]);

            for (AutoBidAgent agent : activeAgents) {

                // =========================================================
                // BẢO MẬT 1: CHỐNG TỰ ĐÈ GIÁ BẢN THÂN (PHIÊN BẢN CHUẨN XÁC 100%)
                // So sánh thẳng ID, khỏi lo lệch tên!
                // =========================================================
                if (currentWinnerId != null && currentWinnerId.equals(agent.getUserId())) {
                    continue; // ID trùng nhau -> Đang Top 1 rồi -> Nằm im!
                }

                // =========================================================
                // BẢO MẬT 2: KIỂM TRA BƯỚC GIÁ TỐI THIỂU
                // =========================================================
                if (agent.getIncrement() < room.getBidStep()) {
                    continue;
                }

                double nextPrice = currentPrice + agent.getIncrement();

                // =========================================================
                // BẢO MẬT 3: KIỂM TRA NGÂN SÁCH MAX BID
                // =========================================================
                if (nextPrice <= agent.getMaxBid()) {

                    com.auction.common.dto.Message bidResult = context.getRoomService().placeNewBid(roomId, agent.getUserId(), nextPrice);

                    if ("BID_SUCCESS".equals(bidResult.getAction()) || "BID_SUCCESS_EXTENDED".equals(bidResult.getAction())) {

                        // Cập nhật lại toàn bộ căn phòng bằng data mới nhất
                        room = (AuctionRoom) bidResult.getData();
                        priceChanged = true;

                        context.broadcastToRoom(roomId, bidResult);
                        context.broadcastAll(new Message("UPDATE_PRICE", "SERVER", roomId + "|" + nextPrice));

                        break; // Đấm thành công, chốt giá, thoát vòng lặp for để quay lại do-while
                    }
                }
            }
        } while (priceChanged);
    }
}