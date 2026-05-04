package com.auction.client.shared.mapper;

/**
 * Hợp đồng mapper chuyển dữ liệu nguồn sang dữ liệu phục vụ hiển thị.
 *
 * Vai trò:
 * - Chuẩn hóa bước biến đổi dữ liệu server/model trước khi render UI.
 * - Giúp handler và renderer không phụ thuộc trực tiếp vào định dạng dữ liệu thô.
 *
 * Luồng chính:
 * 1. Message handler nhận source từ server hoặc model domain.
 * 2. Handler gọi map(source) để lấy dữ liệu đã chuẩn hóa cho UI.
 *
 * Business rules:
 * - Implementation nên xử lý null/định dạng lỗi theo cách an toàn cho UI.
 * - map() không nên gây side effect lên control JavaFX.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation cụ thể.
 * - Dependency: generic source S và target T.
 */
public interface DisplayMapper<S, T> {
    T map(S source);
}
