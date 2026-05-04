package com.auction.client.feature.room;


public record BidRequest(String amountText) {
}
/**
 * DTO chứa text số tiền bid lấy trực tiếp từ UI.
 *
 * Vai trò:
 * - Truyền raw amountText từ TextField sang BidActionHandler.
 * - Tách dữ liệu input khỏi controller FXML.
 *
 * Luồng chính:
 * 1. AuctionController tạo BidRequest khi người dùng bấm đặt giá.
 * 2. BidActionHandler parse amountText thành double và gửi request nếu hợp lệ.
 *
 * Business rules:
 * - amountText được giữ nguyên để handler có thể báo lỗi parse chính xác.
 * - Server vẫn là nơi kiểm tra giá tối thiểu và số dư cuối cùng.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: record immutable sau khi khởi tạo.
 * - Dependency: BidActionHandler.
 */
