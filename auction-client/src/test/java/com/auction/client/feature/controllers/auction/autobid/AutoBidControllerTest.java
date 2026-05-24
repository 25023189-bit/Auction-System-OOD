package com.auction.client.feature.controllers.auction.autobid;

import com.auction.client.network.socket.ClientConnection;
import com.auction.client.session.SessionStore;
import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutoBidControllerTest {
    private SessionStore sessionStore;
    private ClientConnection clientConnection;
    private Button toggleButton;
    private TextField maxBidField;
    private TextField stepField;
    private AutoBidController controller;

    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // JavaFX toolkit already started by another test.
        }
    }

    @BeforeEach
    void setUp() {
        sessionStore = mock(SessionStore.class);
        clientConnection = mock(ClientConnection.class);
        toggleButton = new Button();
        maxBidField = new TextField("1500");
        stepField = new TextField("100");

        AuctionRoom room = new AuctionRoom();
        room.setRoomId("ROOM_AUTO");
        room.setCurrentPrice(1000.0);
        room.setBidStep(100.0);
        when(sessionStore.getCurrentRoom()).thenReturn(room);
        when(sessionStore.getCurrentRoomId()).thenReturn("ROOM_AUTO");

        controller = new AutoBidController(
                new VBox(), maxBidField, stepField, toggleButton, new TextArea(), sessionStore, clientConnection
        );
    }

    @Test
    void setAutoBidDoesNotMarkOnBeforeServerSuccess() {
        controller.handleSetAutoBid();

        assertFalse(controller.isAutoBidActive());
        assertTrue(toggleButton.isDisabled());
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(clientConnection).sendMessage(messageCaptor.capture());
        assertTrue("SET_AUTO_BID".equals(messageCaptor.getValue().getAction()));
    }

    @Test
    void setAutoBidRejectsNonNumericInputInsteadOfCleaningIt() {
        maxBidField.setText("abc1500");

        controller.handleSetAutoBid();

        assertFalse(controller.isAutoBidActive());
        assertFalse(toggleButton.isDisabled());
        verify(clientConnection, never()).sendMessage(org.mockito.ArgumentMatchers.any(Message.class));
    }

    @Test
    void setSuccessMarksOnAndSetFailedKeepsOff() {
        controller.handleSetAutoBid();
        controller.handleSetSuccess();

        assertTrue(controller.isAutoBidActive());
        assertFalse(toggleButton.isDisabled());

        controller.handleSetFailed("invalid");

        assertFalse(controller.isAutoBidActive());
        assertFalse(toggleButton.isDisabled());
    }

    @Test
    void cancelAutoBidDoesNotMarkOffBeforeServerSuccess() {
        controller.handleSetSuccess();

        controller.handleCancelAutoBid();

        assertTrue(controller.isAutoBidActive());
        assertTrue(toggleButton.isDisabled());
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(clientConnection).sendMessage(messageCaptor.capture());
        assertTrue("CANCEL_AUTO_BID".equals(messageCaptor.getValue().getAction()));
    }

    @Test
    void cancelSuccessMarksOffAndCancelFailedKeepsOn() {
        controller.handleSetSuccess();

        controller.handleCancelAutoBid();
        controller.handleCancelFailed("missing");
        assertTrue(controller.isAutoBidActive());

        controller.handleCancelAutoBid();
        controller.handleCancelSuccess();
        assertFalse(controller.isAutoBidActive());
    }
}
