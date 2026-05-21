package com.auction.server.service;

import com.auction.common.model.AuctionRoom;
import com.auction.server.handler.ClientActionContext; // Import thêm cái này

import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBidManager {

    // Không cần khai báo biến hay Constructor gì ở đây nữa, class trống trơn sạch sẽ!

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

        // Vòng lặp chiến đấu: Chạy liên tục cho đến khi không còn Robot nào muốn/đủ tiền nâng giá nữa
        do {
            priceChanged = false;

            // Lấy giá và người thắng HIỆN TẠI (Đã được cập nhật sau mỗi cú đấm)
            double currentPrice = room.getCurrentPrice();
            String currentWinner = room.getHighestBidder();

            // Chuyển Queue thành mảng để duyệt
            AutoBidAgent[] activeAgents = queue.toArray(new AutoBidAgent[0]);

            // Thêm một "cuốn sổ" UserDAO để Robot tự đi tra cứu tên thật
            com.auction.server.dao.UserDAO userDAO = new com.auction.server.dao.UserDAO();

            for (AutoBidAgent agent : activeAgents) {

                // --- BẮT ĐẦU ĐOẠN SỬA ---
                // Robot tự lấy ID của mình tra vào DB để biết Username hiển thị là gì
                com.auction.common.model.User agentUser = userDAO.getUserById(agent.getUserId());
                String agentUsername = (agentUser != null && agentUser.getUsername() != null)
                        ? agentUser.getUsername()
                        : agent.getUserId();

                // 1. QUY TẮC SỐ 1: Không tự đè giá của chính mình (So sánh Username chuẩn xác!)
                if (agentUsername.equals(currentWinner)) {
                    continue;
                }
                // --- KẾT THÚC ĐOẠN SỬA ---

                // 2. Tính giá dự kiến
                double nextPrice = currentPrice + agent.getIncrement();

                // 3. QUY TẮC SỐ 2: Kiểm tra giới hạn Max Bid
                if (nextPrice <= agent.getMaxBid()) {

                    // Lúc này agent.getUserId() là ID THẬT -> Đưa cho DB nó mới chịu ghi nhận!
                    com.auction.common.dto.Message bidResult = context.getRoomService().placeNewBid(roomId, agent.getUserId(), nextPrice);

                    if ("BID_SUCCESS".equals(bidResult.getAction()) || "BID_SUCCESS_EXTENDED".equals(bidResult.getAction())) {
                        room = (AuctionRoom) bidResult.getData();
                        priceChanged = true;

                        context.broadcastToRoom(roomId, bidResult);
                        String updatePayload = roomId + "|" + nextPrice;
                        context.broadcastAll(new com.auction.common.dto.Message("UPDATE_PRICE", "SERVER", updatePayload));

                        break; // Đấm thành công, chốt giá vòng này!
                    }
                }
            }
        } while (priceChanged);
    }
}