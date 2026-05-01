package com.auction.client.feature.auth;

import com.auction.server.service.AuctionService;

/**
 * Command xử lý submit form đăng nhập.
 *
 * Vai trò:
 * - Đóng gói dữ liệu username/password và luồng validate phía client.
 * - Gọi AuctionService.login() khi form hợp lệ.
 *
 * Luồng chính:
 * 1. execute() tạo LoginForm và chạy LoginFormValidator.
 * 2. Nếu hợp lệ, command trim input và gửi request LOGIN qua AuctionService.
 *
 * Business rules:
 * - Không gửi request nếu username hoặc password rỗng.
 * - Lỗi validate phải hiển thị qua AuthPresenter thay vì ném exception.
 *
 * Ghi chú kỹ thuật:
 * - Không thread-safe: giữ state form/service/presenter theo một lần submit.
 * - Dependency: UiCommand, AuctionService, FormValidator<LoginForm>, AuthPresenter.
 */
public class LoginCommand implements UiCommand {
    private final AuctionService auctionService;
    private final FormValidator<LoginForm> validator;
    private final AuthPresenter presenter;
    private final String username;
    private final String password;

    public LoginCommand(AuctionService auctionService,
                        FormValidator<LoginForm> validator,
                        AuthPresenter presenter,
                        String username,
                        String password) {
        this.auctionService = auctionService;
        this.validator = validator;
        this.presenter = presenter;
        this.username = username;
        this.password = password;
    }

    @Override
    public void execute() {
        // Chặn input rỗng ở client để tránh gửi request không hợp lệ lên server.
        ValidationResult result = validator.validate(new LoginForm(username, password));
        if (!result.isValid()) {
            presenter.showLoginError(result.getMessage());
            return;
        }
        auctionService.login(username.trim(), password.trim());
    }
}
