package com.auction.client.feature.viewmodel;

public class AuthViewModel {
    private String username = "";
    private String password = "";
    private String registerUsername = "";
    private String registerPassword = "";
    private String registerConfirmPassword = "";
    private String registerRole = "BIDDER";
    private String registerOrganization = "";
    private String forgotUsername = "";
    private String forgotPassword = "";
    private String forgotConfirmPassword = "";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegisterUsername() {
        return registerUsername;
    }

    public void setRegisterUsername(String registerUsername) {
        this.registerUsername = registerUsername;
    }

    public String getRegisterPassword() {
        return registerPassword;
    }

    public void setRegisterPassword(String registerPassword) {
        this.registerPassword = registerPassword;
    }

    public String getRegisterConfirmPassword() {
        return registerConfirmPassword;
    }

    public void setRegisterConfirmPassword(String registerConfirmPassword) {
        this.registerConfirmPassword = registerConfirmPassword;
    }

    public String getRegisterRole() {
        return registerRole;
    }

    public void setRegisterRole(String registerRole) {
        this.registerRole = registerRole;
    }

    public String getRegisterOrganization() {
        return registerOrganization;
    }

    public void setRegisterOrganization(String registerOrganization) {
        this.registerOrganization = registerOrganization;
    }

    public String getForgotUsername() {
        return forgotUsername;
    }

    public void setForgotUsername(String forgotUsername) {
        this.forgotUsername = forgotUsername;
    }

    public String getForgotPassword() {
        return forgotPassword;
    }

    public void setForgotPassword(String forgotPassword) {
        this.forgotPassword = forgotPassword;
    }

    public String getForgotConfirmPassword() {
        return forgotConfirmPassword;
    }

    public void setForgotConfirmPassword(String forgotConfirmPassword) {
        this.forgotConfirmPassword = forgotConfirmPassword;
    }
}
