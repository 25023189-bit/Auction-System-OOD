package com.auction.client.auth;

import com.auction.server.service.AuctionService;

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
        ValidationResult result = validator.validate(new LoginForm(username, password));
        if (!result.isValid()) {
            presenter.showLoginError(result.getMessage());
            return;
        }
        auctionService.login(username.trim(), password.trim());
    }
}