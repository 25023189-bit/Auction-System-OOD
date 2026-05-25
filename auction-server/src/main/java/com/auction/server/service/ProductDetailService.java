package com.auction.server.service;

import com.auction.common.dto.BidHistoryDTO;
import com.auction.common.model.AuctionRoom;
import com.auction.common.model.BidTransaction;
import com.auction.common.model.ProductDetailResponse;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.TransactionDAO;
import com.auction.server.handler.AuctionImageRegistry;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service tổng hợp dữ liệu chi tiết sản phẩm cho popup product detail.
 *
 * Vai trò:
 * - Đọc AuctionRoom và lịch sử bid để tạo ProductDetailResponse.
 * - Chuyển BidTransaction từ DB sang BidHistoryDTO phù hợp cho client hiển thị.
 *
 * Luồng chính:
 * 1. RoomActionHandler gọi getProductDetails(roomId) khi client mở popup.
 * 2. Service lấy room, tính thời gian còn lại, map lịch sử bid và trả DTO tổng hợp.
 *
 * Business rules:
 * - Nếu không tìm thấy room thì trả null để handler gửi PRODUCT_DETAILS_FAIL.
 * - Thời gian còn lại không âm; hết giờ hoặc thiếu endTime thì trả 0 milliseconds.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe theo instance: giữ DAO instance, không có synchronization.
 * - Dependency: AuctionDAO, TransactionDAO, ProductDetailResponse, BidHistoryDTO, ChronoUnit.
 */
public class ProductDetailService {

    private AuctionDAO auctionDAO;
    private TransactionDAO transactionDAO;

    public ProductDetailService() {
        this.auctionDAO = new AuctionDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public ProductDetailResponse getProductDetails(String roomId) {

        AuctionRoom room = auctionDAO.getAuctionById(roomId);

        if (room == null) return null;
        AuctionImageRegistry.apply(room);

        ProductDetailResponse detail = new ProductDetailResponse();

        detail.setTitle(room.getItemName());
        detail.setDescription(room.getItemDescription());
        detail.setStartPrice(room.getCurrentPrice());
        detail.setCurrentPrice(room.getCurrentPrice());
        detail.setTimeLeftMillis(calculateRemainingTime(room.getEndTime()));
        detail.setBase64Image(room.getBase64Image());

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
