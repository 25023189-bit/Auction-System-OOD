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
        String organization = "SELLER".equalsIgnoreCase(role) ? form.organization() : null;

        // ✅ FIXED: Now passing email and fullName
        auctionService.register(
                form.customerId(),
                form.username().trim(),
                form.email().trim(),           // <-- ADDED
                form.fullName().trim(),        // <-- ADDED
                form.password().trim(),
                role,
                organization
        );
    }
}