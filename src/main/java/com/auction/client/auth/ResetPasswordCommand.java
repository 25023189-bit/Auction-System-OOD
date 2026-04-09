package com.auction.client.auth;

import com.auction.server.service.AuctionService;

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
        ValidationResult result = validator.validate(form);
        if (!result.isValid()) {
            presenter.showResetError(result.getMessage());
            return;
        }

        auctionService.resetPassword(form.username().trim(), form.newPassword().trim());
    }
}