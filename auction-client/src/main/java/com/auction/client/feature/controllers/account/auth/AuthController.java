package com.auction.client.feature.controllers.account.auth;

import com.auction.client.feature.auth.AuthActionFacade;

public class AuthController {
    private final AuthActionFacade authActionFacade;

    public AuthController(AuthActionFacade authActionFacade) {
        this.authActionFacade = authActionFacade;
    }

    public void login(String username, String password) {
        authActionFacade.login(username, password);
    }
}
