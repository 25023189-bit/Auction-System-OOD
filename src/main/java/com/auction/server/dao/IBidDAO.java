package com.auction.server.dao;

public interface IBidDAO {
    BidResult placeBid(String auctionId, String bidderId, double bidAmount);

    /**
     * Mã trạng thái nghiệp vụ của thao tác đặt giá.
     * <p>
     * Vai trò:
     * - Chuẩn hóa lý do thành công/thất bại để service không phải parse chuỗi lỗi.
     * - Làm dữ liệu đi kèm trong BidResult.
     * <p>
     * Luồng chính:
     * 1. BidDAO chọn BidStatus phù hợp khi validate hoặc thao tác DB thất bại.
     * 2. AuctionRoomService đọc status/message để trả response cho client.
     * <p>
     * Business rules:
     * - SUCCESS chỉ dùng khi toàn bộ transaction đặt giá commit thành công.
     * - Các trạng thái fail phải phản ánh đúng nguyên nhân: không có phiên, giá thấp, thiếu số dư hoặc lỗi hệ thống.
     * <p>
     * Ghi chú kỹ thuật:
     * - Thread-safe: enum bất biến theo thiết kế của Java.
     * - Dependency: Được dùng bởi BidDAO.BidResult và AuctionRoomService.
     */
    public enum BidStatus {
        SUCCESS,
        AUCTION_NOT_FOUND,
        BID_TOO_LOW,
        INSUFFICIENT_BALANCE,
        ERROR
    }

    /**
     * Giá trị kết quả trả về sau khi xử lý một yêu cầu đặt giá.
     * <p>
     * Vai trò:
     * - Đóng gói success flag, BidStatus và thông báo lỗi nếu có.
     * - Tách kết quả transaction DB khỏi Message gửi ra client.
     * <p>
     * Luồng chính:
     * 1. BidDAO tạo BidResult bằng success() hoặc fail().
     * 2. AuctionRoomService chuyển kết quả này thành BID_SUCCESS hoặc BID_FAIL.
     * <p>
     * Business rules:
     * - success=true chỉ đi kèm BidStatus.SUCCESS.
     * - fail() luôn giữ message để tầng service có thể trả lý do cụ thể.
     * <p>
     * Ghi chú kỹ thuật:
     * - Thread-safe: immutable sau khi khởi tạo, các field đều final.
     * - Dependency: BidStatus và AuctionRoomService.
     */
    public static class BidResult {
        private final boolean success;
        private final BidStatus status;
        private final String message;

        private BidResult(boolean success, BidStatus status, String message) {
            this.success = success;
            this.status = status;
            this.message = message;
        }

        public static BidResult success() {
            return new BidResult(true, BidStatus.SUCCESS, null);
        }

        public static BidResult fail(BidStatus status, String message) {
            return new BidResult(false, status, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public BidStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
