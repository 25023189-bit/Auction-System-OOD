package com.auction.client.feature.auth;

public record ResetPasswordForm(String username, String newPassword, String confirmPassword) {
}

/**
 * DTO chứa dữ liệu đặt lại mật khẩu trước khi validate.
 *
 * Vai trò:
 * - Gom username, newPassword và confirmPassword từ form quên mật khẩu.
 * - Là input cho ResetPasswordFormValidator và AuthActionFacade.resetPassword.
 *
 * Luồng chính:
 * 1. AuctionController tạo ResetPasswordForm từ các field forgot password.
 * 2. Validator kiểm tra dữ liệu, facade gửi request nếu hợp lệ.
 *
 * Business rules:
 * - newPassword và confirmPassword phải khớp ở bước validate.
 * - DTO không tự xử lý độ mạnh mật khẩu; server vẫn kiểm tra cuối cùng.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: record immutable sau khi khởi tạo.
 * - Dependency: AuthActionFacade.resetPassword và ResetPasswordFormValidator.
 */

