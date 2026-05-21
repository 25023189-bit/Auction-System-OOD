package com.auction.client.feature.auth;

/**
 * Hợp đồng validate dữ liệu form trước khi facade gọi service.
 *
 * Vai trò:
 * - Chuẩn hóa validate cho từng DTO form như login, register và reset password.
 * - Trả ValidationResult để facade biết có được gửi request mạng hay không.
 *
 * Luồng chính:
 * 1. AuthActionFacade tạo form DTO từ dữ liệu control.
 * 2. AuthActionFacade gọi validate(form) và chỉ tiếp tục khi kết quả hợp lệ.
 *
 * Business rules:
 * - Dữ liệu không hợp lệ phải trả ValidationResult.fail(message) có lý do rõ ràng.
 * - Validator chỉ kiểm tra phía client, server vẫn là nơi xác thực cuối cùng.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe phụ thuộc implementation; validator hiện tại stateless.
 * - Dependency: ValidationResult và DTO form generic T.
 */
public interface FormValidator<T> {
    ValidationResult validate(T form);
}
