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

        boolean processNext;
        do {
            processNext = false;

            // 1. Nhấc người có MaxBid cao nhất ra khỏi hàng đợi
            AutoBidAgent topAgent = queue.poll();
            if (topAgent == null) break;

            // 2. Kiểm tra xem người này có đang là người giữ giá không
            if (topAgent.getUserId().equals(room.getHighestBidder())) {

                // Nếu ĐANG THẮNG -> Tạm tránh sang 1 bên để xem người thứ 2 có muốn bật lại không
                AutoBidAgent secondAgent = queue.poll(); // Nhấc người thứ 2 ra

                if (secondAgent != null) {
                    double nextPrice = room.getCurrentPrice() + secondAgent.getIncrement();

                    // Nếu người thứ 2 đủ tiền -> Phản công!
                    if (nextPrice <= secondAgent.getMaxBid()) {
                        room.setCurrentPrice(nextPrice);
                        room.setHighestBidder(secondAgent.getUserId());

                        context.broadcastToRoom(roomId, new com.auction.common.dto.Message("BID_SUCCESS", room));

                        processNext = true;       // Tiếp tục vòng lặp
                        queue.add(secondAgent);   // Trả người thứ 2 vào hàng đợi
                    }
                    // Nếu người thứ 2 không đủ tiền -> Bị loại luôn (không add lại vào queue)
                }

                // Dù người thứ 2 có đánh hay không, vẫn phải giữ người top 1 lại trong hàng đợi
                queue.add(topAgent);

            } else {

                // Nếu người top 1 KHÔNG PHẢI người đang thắng -> Được phép đấm luôn!
                double nextPrice = room.getCurrentPrice() + topAgent.getIncrement();

                if (nextPrice <= topAgent.getMaxBid()) {
                    room.setCurrentPrice(nextPrice);
                    room.setHighestBidder(topAgent.getUserId());

                    context.broadcastToRoom(roomId, new com.auction.common.dto.Message("BID_SUCCESS", room));

                    processNext = true;     // Tiếp tục vòng lặp
                    queue.add(topAgent);    // Đánh xong trả về hàng đợi để chờ đánh tiếp
                }
                // Nếu top 1 mà còn hết tiền -> Bị loại!
            }

        } while (processNext);
    }
}