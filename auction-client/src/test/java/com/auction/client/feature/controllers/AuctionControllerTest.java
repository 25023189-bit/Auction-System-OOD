package com.auction.client.feature.controllers;

import com.auction.client.feature.controllers.app.root.AuctionController;
import com.auction.client.service.AuctionService;
import com.auction.client.session.SessionStore;
import com.auction.client.shared.support.DefaultFxThreadExecutor;
import com.auction.common.dto.Message;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuctionControllerTest {

    private AuctionController controller;
    private AuctionService mockAuctionService;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new AuctionController();
        mockAuctionService = mock(AuctionService.class);
        injectField("auctionService", mockAuctionService);
        injectField("sessionStore", mock(SessionStore.class));
        injectField("fxThreadExecutor", new DefaultFxThreadExecutor());
    }

    @Test
    @DisplayName("Connection status updates status label")
    void updateConnectionStatus_UpdatesLabel() throws Exception {
        Label status = new Label();
        injectField("lblStatus", status);

        controller.updateConnectionStatus("Connected");

        assertEquals("Status: Connected", status.getText());
    }

    @Test
    @DisplayName("Product details response delegates to response coordinator")
    void onServerResponse_ProductDetails() throws Exception {
        Message responseMsg = new Message("PRODUCT_DETAILS_SUCCESS", "SERVER", "MockProductData");
        CountDownLatch callbackLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            callbackLatch.countDown();
            return null;
        }).when(mockAuctionService).fireProductDetailsReceived("MockProductData");

        controller.onServerResponse(responseMsg);

        assertTrue(callbackLatch.await(1, TimeUnit.SECONDS));
        verify(mockAuctionService).fireProductDetailsReceived("MockProductData");
    }

    private void injectField(String name, Object value) throws Exception {
        Field field = AuctionController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }
}
