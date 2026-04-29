package com.auction.client.feature.auth;

import com.auction.server.service.AuctionService;

/**
 * Command xử lý submit đăng ký tài khoản.
 * Chuẩn hóa role trước khi gọi service để server nhận dữ liệu nhất quán.
 */
public class RegisterCommand implements UiCommand {
    private final AuctionService auctionService;
    private final FormValidator<RegisterForm> validator;
    private final AuthPresenter presenter;
    private final RegisterForm form;

    public RegisterCommand(AuctionService auctionService,
                           FormValidator<RegisterForm> validator,
                           AuthPresenter presenter,
                           RegisterForm form) {
        this.auctionService = auctionService;
        this.validator = validator;
        this.presenter = presenter;
        this.form = form;
    }

    @Override
    public void execute() {
        // Validator kiểm tra các lỗi nhập liệu cơ bản trước khi gửi request mạng.
        ValidationResult result = validator.validate(form);
        if (!result.isValid()) {
            presenter.showRegisterError(result.getMessage());
            return;
        }

        String role = "SELLER".equalsIgnoreCase(form.role()) ? "SELLER" : "BIDDER";
        String organization = "SELLER".equalsIgnoreCase(role) ? form.organization() : null;

        // Email và fullName là thông tin hồ sơ bắt buộc gửi cùng request đăng ký.
        auctionService.register(
                form.customerId(),
                form.username().trim(),
                form.email().trim(),
                form.fullName().trim(),
                form.password().trim(),
                role,
                organization
        );
    }
}
