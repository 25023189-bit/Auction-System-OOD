package com.auction.client.feature.auth;

/**
 * Hợp đồng command cho thao tác submit từ UI.
 *
 * Vai trò:
 * - Đóng gói một hành động người dùng thành method execute().
 * - Tách xử lý validate/gửi service khỏi controller FXML.
 *
 * Luồng chính:
 * 1. Controller tạo command với dữ liệu form và dependency cần thiết.
 * 2. Controller gọi execute() khi người dùng submit.
 *
 * Business rules:
 * - Command phải tự quyết định có gửi request hay hiển thị lỗi validate.
 * - execute() không nên yêu cầu caller biết chi tiết protocol Message.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation cụ thể.
 * - Dependency: các implementation LoginCommand, RegisterCommand, ResetPasswordCommand.
 */
public interface UiCommand {
    void execute();
}
