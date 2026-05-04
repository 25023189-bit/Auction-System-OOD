package com.auction.client.feature.room;

/**
 * Hợp đồng chung cho handler xử lý thao tác người dùng trong phòng đấu giá.
 *
 * Vai trò:
 * - Chuẩn hóa điểm vào handle(request) cho các action như bid hoặc chat.
 * - Tách dữ liệu request khỏi controller FXML.
 *
 * Luồng chính:
 * 1. AuctionController tạo request DTO từ control UI.
 * 2. Controller gọi handle(request) trên handler chuyên trách.
 *
 * Business rules:
 * - Handler phải tự validate dữ liệu cơ bản trước khi gọi service.
 * - Quy tắc nghiệp vụ cuối cùng vẫn được server xác thực.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation cụ thể.
 * - Dependency: DTO generic T và implementation trong feature.room.
 */
public interface ActionHandler<T> {
    void handle(T request);
}
