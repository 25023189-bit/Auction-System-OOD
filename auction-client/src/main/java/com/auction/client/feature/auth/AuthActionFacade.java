package com.auction.client.feature.auth;

import com.auction.client.service.AuctionService;

/**
 * Facade gom cac hanh dong xac thuc tu man hinh login.
 */
public class AuthActionFacade {
    private final AuctionService auctionService;
    private final AuthPresenter presenter;
    private final FormValidator<LoginForm> loginValidator;
    private final FormValidator<RegisterForm> registerValidator;
    private final FormValidator<ResetPasswordForm> resetPasswordValidator;

    public AuthActionFacade(AuctionService auctionService, AuthPresenter presenter) {
        this.auctionService = auctionService;
        this.presenter = presenter;
        this.loginValidator = new LoginFormValidator();
        this.registerValidator = new RegisterFormValidator();
        this.resetPasswordValidator = new ResetPasswordFormValidator();
    }

    public void login(String username, String password) {
        ValidationResult result = loginValidator.validate(new LoginForm(username, password));
        if (!result.isValid()) {
            presenter.showLoginError(result.getMessage());
            return;
        }

        auctionService.login(username.trim(), password.trim());
    }

    public void register(RegisterForm form) {
        ValidationResult result = registerValidator.validate(form);
        if (!result.isValid()) {
            presenter.showRegisterError(result.getMessage());
            return;
        }

        String role = "SELLER".equalsIgnoreCase(form.role()) ? "SELLER" : "BIDDER";
        String organization = "SELLER".equals(role) ? form.organization() : null;

        auctionService.register(
                form.customerId(),
                form.username().trim(),
                safeTrim(form.fullName()),
                form.password().trim(),
                role,
                organization
        );
    }

    public void resetPassword(ResetPasswordForm form) {
        ValidationResult result = resetPasswordValidator.validate(form);
        if (!result.isValid()) {
            presenter.showResetError(result.getMessage());
            return;
        }

        auctionService.resetPassword(
                form.username().trim(),
                form.newPassword().trim(),
                form.confirmPassword().trim()
        );
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
