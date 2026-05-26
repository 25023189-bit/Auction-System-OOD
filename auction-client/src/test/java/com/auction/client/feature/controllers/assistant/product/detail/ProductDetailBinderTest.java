package com.auction.client.feature.controllers.assistant.product.detail;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductDetailBinderTest {
    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // JavaFX was already started by another test class.
        }
    }

    @Test
    void bindsAllProductDetailLabels() {
        Label title = new Label();
        Label description = new Label();
        Label price = new Label();
        Label bids = new Label();
        Label time = new Label();
        ProductDetailBinder binder = new ProductDetailBinder(title, description, price, bids, time, null);

        binder.bind(new ProductDetailViewModel("Laptop", "New", "500 $", "7", "4m 3s", ""));

        assertEquals("Laptop", title.getText());
        assertEquals("New", description.getText());
        assertEquals("500 $", price.getText());
        assertEquals("7", bids.getText());
        assertEquals("4m 3s", time.getText());
    }

    @Test
    void handlesNullValuesAndMissingLabels() {
        ProductDetailBinder binder = new ProductDetailBinder(null, null, null, null, null, null);

        assertDoesNotThrow(() -> binder.bind(null));
        assertDoesNotThrow(() -> binder.bind(
                new ProductDetailViewModel("Title", "Description", "1 $", "0", "Ended", "")));
        assertDoesNotThrow(binder::showLoading);
        assertDoesNotThrow(binder::showNotFound);
    }
}
