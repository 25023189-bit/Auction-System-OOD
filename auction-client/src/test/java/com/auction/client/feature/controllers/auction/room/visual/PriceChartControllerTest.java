package com.auction.client.feature.controllers.auction.room.visual;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceChartControllerTest {
    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    void initializesTracksValidPriceChangesAndLimitsHistory() throws Exception {
        Label label = new Label();
        PriceChartController controller = new PriceChartController(null, label);
        controller.initializeWithStartPrice(100.0);
        controller.bindCurrentPriceLabel();
        controller.bindCurrentPriceLabel();
        waitForFxEvents();

        for (int value = 101; value <= 118; value++) {
            label.setText("Current Price: " + value + " $");
        }
        label.setText("invalid");
        waitForFxEvents();

        XYChart.Series<String, Number> series = series(controller);
        assertEquals(15, series.getData().size());
        assertEquals(118.0, series.getData().get(series.getData().size() - 1).getYValue().doubleValue());
    }

    @Test
    void ignoresBindingWhenNoLabelAndUnparseableText() throws Exception {
        PriceChartController controller = new PriceChartController(null, null);
        controller.initializeWithStartPrice(12.0);
        controller.bindCurrentPriceLabel();
        waitForFxEvents();
        assertEquals(1, series(controller).getData().size());
        assertTrue(series(controller).getData().get(0).getYValue().doubleValue() > 0);
    }

    @SuppressWarnings("unchecked")
    private XYChart.Series<String, Number> series(PriceChartController controller) throws Exception {
        Field field = PriceChartController.class.getDeclaredField("priceSeries");
        field.setAccessible(true);
        return (XYChart.Series<String, Number>) field.get(controller);
    }

    private void waitForFxEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}
