package com.auction.client.feature.auth;

import com.auction.server.service.AuctionService;

/**
 * Command xử lý submit form đặt lại mật khẩu.
 *
 * Vai trò:
 * - Validate username, mật khẩu mới và xác nhận mật khẩu phía client.
 * - Gọi AuctionService.resetPassword() khi form hợp lệ.
 *
 * Luồng chính:
 * 1. execute() chạy ResetPasswordFormValidator.
 * 2. Nếu hợp lệ, command gửi username và newPassword đã trim lên server.
 *
 * Business rules:
 * - Không gửi request nếu mật khẩu mới không khớp confirm password.
 * - Lỗi validate phải hiển thị qua AuthPresenter.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: giữ form/service/presenter theo một lần submit.
 * - Dependency: UiCommand, AuctionService, FormValidator<ResetPasswordForm>, AuthPresenter.
 */
public class ResetPasswordCommand implements UiCommand {
    private final AuctionService auctionService;
    private final FormValidator<ResetPasswordForm> validator;
    private final AuthPresenter presenter;
    private final ResetPasswordForm form;

    public ResetPasswordCommand(AuctionService auctionService, FormValidator<ResetPasswordForm> validator, AuthPresenter presenter, ResetPasswordForm form) {
        this.auctionService = auctionService;
        this.validator = validator;
        this.presenter = presenter;
        this.form = form;
    }

    @Override
    public void execute() {
        // Chỉ gửi username và mật khẩu mới sau khi xác nhận nhập lại khớp.
        ValidationResult result = validator.validate(form);
        if (!result.isValid()) {
            presenter.showResetError(result.getMessage());
            return;
        }

        auctionService.resetPassword(form.username().trim(), form.newPassword().trim());
    }
}
