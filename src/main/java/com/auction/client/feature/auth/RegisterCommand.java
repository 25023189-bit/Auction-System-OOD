package com.auction.client.feature.auth;

import com.auction.server.service.AuctionService;

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
        ValidationResult result = validator.validate(form);
        if (!result.isValid()) {
            presenter.showRegisterError(result.getMessage());
            return;
        }

        String role = "SELLER".equalsIgnoreCase(form.role()) ? "SELLER" : "BIDDER";
        // Đổi .username() thành .customerId()
        auctionService.register(form.customerId().trim(), form.password().trim(), role);
    }
}