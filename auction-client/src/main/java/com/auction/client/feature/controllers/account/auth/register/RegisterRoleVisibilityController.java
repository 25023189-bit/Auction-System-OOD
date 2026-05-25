package com.auction.client.feature.controllers.account.auth.register;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RegisterRoleVisibilityController {
    private final ComboBox<String> cbRegRole;
    private final Label lblRegOrganization;
    private final TextField txtRegOrganization;

    public RegisterRoleVisibilityController(ComboBox<String> cbRegRole, Label lblRegOrganization, TextField txtRegOrganization) {
        this.cbRegRole = cbRegRole;
        this.lblRegOrganization = lblRegOrganization;
        this.txtRegOrganization = txtRegOrganization;
    }

    public void update() {
        boolean sellerSelected = cbRegRole != null && "SELLER".equalsIgnoreCase(cbRegRole.getValue());
        if (txtRegOrganization != null) {
            txtRegOrganization.setVisible(sellerSelected);
            txtRegOrganization.setManaged(sellerSelected);
            if (!sellerSelected) txtRegOrganization.clear();
        }
        if (lblRegOrganization != null) {
            lblRegOrganization.setVisible(sellerSelected);
            lblRegOrganization.setManaged(sellerSelected);
        }
    }
}
