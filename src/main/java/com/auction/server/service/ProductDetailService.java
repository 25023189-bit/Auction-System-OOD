package com.auction.server.service;

import com.auction.common.dto.BidHistoryDTO;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.ProductDetailResponse;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.TransactionDAO;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service gom dữ liệu chi tiết sản phẩm/phòng để trả cho popup product detail.
 */
public class ProductDetailService {

    private final AuctionDAO auctionDAO;
    private final TransactionDAO transactionDAO;

    public ProductDetailService() {
        this.auctionDAO = new AuctionDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public ProductDetailResponse getProductDetails(String roomId) {

        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) return null;

        ProductDetailResponse detail = new ProductDetailResponse();

        detail.setTitle(room.getItemName());
        detail.setDescription(room.getItemDescription());
        detail.setStartPrice(room.getCurrentPrice());
        detail.setCurrentPrice(room.getCurrentPrice());
        detail.setTimeLeftMillis(calculateRemainingTime(room.getEndTime()));

        // Chuyển lịch sử bid từ model DB sang DTO hiển thị cho client.
        List<BidTransaction> dbTransactions = transactionDAO.getHistoryByRoom(roomId);
        List<BidHistoryDTO> historyDTOs = new ArrayList<>();

        if (dbTransactions != null) {
            for (BidTransaction tx : dbTransactions) {
                String bidderDisplay = (tx.getBidderId() != null) ? "User " + tx.getBidderId() : "Anonymous";
                historyDTOs.add(new BidHistoryDTO(
                        bidderDisplay,
                        tx.getBidAmount(),
                        tx.getBidTime()
                ));
            }
        }
        detail.setBidHistory(historyDTOs);

        return detail;
    }

    // Trả về milliseconds còn lại; nếu hết giờ hoặc thiếu endTime thì trả 0.
    private long calculateRemainingTime(LocalDateTime endTime) {
        if (endTime == null) return 0;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime)) return 0;
        return ChronoUnit.MILLIS.between(now, endTime);
    }
}
