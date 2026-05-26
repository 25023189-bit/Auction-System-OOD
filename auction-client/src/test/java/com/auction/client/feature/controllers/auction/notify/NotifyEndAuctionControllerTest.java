package com.auction.client.feature.controllers.auction.notify;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotifyEndAuctionControllerTest {
    private NotifyEndAuctionController controller;
    private Label icon;
    private Label title;
    private Label price;
    private Label reason;
    private Label transaction;
    private Button sellerButton;
    private Button lobbyButton;
    private Button otherButton;

    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new NotifyEndAuctionController();
        icon = injectLabel("lblStatusIcon");
        title = injectLabel("lblTitle");
        injectLabel("lblBody");
        injectLabel("lblAuctionId");
        injectLabel("lblItemName");
        price = injectLabel("lblFinalPrice");
        injectLabel("lblWinner");
        reason = injectLabel("lblEndReason");
        transaction = injectLabel("lblTransaction");
        injectLabel("lblNextAction");
        lobbyButton = injectButton("btnBackToLobby");
        sellerButton = injectButton("btnSellerDashboard");
        otherButton = injectButton("btnViewOtherAuctions");
        Method initialize = NotifyEndAuctionController.class.getDeclaredMethod("initialize");
        initialize.setAccessible(true);
        initialize.invoke(controller);
    }

    @Test
    void presentsOutcomeInformationAndSellerActions() {
        controller.setNotificationData(viewModel(EndAuctionResultType.SELLER_SOLD, "SELLER", 2000.0, true));

        assertEquals("WIN", icon.getText());
        assertEquals("Complete", title.getText());
        assertEquals("2,000 $", price.getText());
        assertEquals("TIME EXPIRED", reason.getText());
        assertEquals("Da ap dung", transaction.getText());
        assertTrue(sellerButton.isVisible());
        assertTrue(sellerButton.isManaged());

        controller.setNotificationData(viewModel(EndAuctionResultType.CLOSED_BY_ADMIN, "BIDDER", 0.0, false));
        assertEquals("STOP", icon.getText());
        assertEquals("Chua co thong tin", price.getText());
        assertEquals("Khong ap dung", transaction.getText());
        assertFalse(sellerButton.isVisible());
        assertFalse(sellerButton.isManaged());
    }

    @Test
    void mapsAllIconTypesAndInvokesConfiguredActions() {
        controller.setNotificationData(viewModel(EndAuctionResultType.NO_WINNER, "BIDDER", 1.0, false));
        assertEquals("END", icon.getText());
        controller.setNotificationData(viewModel(EndAuctionResultType.UNKNOWN, "BIDDER", 1.0, false));
        assertEquals("INFO", icon.getText());
        controller.setNotificationData(null);

        Runnable lobby = mock(Runnable.class);
        Runnable dashboard = mock(Runnable.class);
        Runnable other = mock(Runnable.class);
        controller.setActionHandlers(lobby, dashboard, other);
        lobbyButton.fire();
        sellerButton.fire();
        otherButton.fire();

        verify(lobby).run();
        verify(dashboard).run();
        verify(other).run();
    }

    private EndAuctionNotificationViewModel viewModel(
            EndAuctionResultType resultType,
            String role,
            double finalPrice,
            boolean applied
    ) {
        return new EndAuctionNotificationViewModel(
                "R1", "Camera", finalPrice, "winner", "user", role,
                AuctionEndReason.TIME_EXPIRED, resultType, true, applied,
                "Complete", "Body", "Next"
        );
    }

    private Label injectLabel(String name) throws Exception {
        Label label = new Label();
        setField(name, label);
        return label;
    }

    private Button injectButton(String name) throws Exception {
        Button button = new Button();
        setField(name, button);
        return button;
    }

    private void setField(String name, Object value) throws Exception {
        Field field = NotifyEndAuctionController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }
}
