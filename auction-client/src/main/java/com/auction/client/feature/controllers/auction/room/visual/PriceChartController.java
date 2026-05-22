package com.auction.client.feature.controllers.auction.room.visual;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PriceChartController {
    private final AreaChart<String, Number> priceChart;
    private final Label currentPriceLabel;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private XYChart.Series<String, Number> priceSeries;
    private ChangeListener<String> currentPriceListener;

    public PriceChartController(AreaChart<String, Number> priceChart, Label currentPriceLabel) {
        this.priceChart = priceChart;
        this.currentPriceLabel = currentPriceLabel;
    }

    public void initializeWithStartPrice(double startPrice) {
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Lich su gia");

        if (priceChart != null) {
            priceChart.getData().clear();
            priceChart.getData().add(priceSeries);
        }

        updateOnNewBid(startPrice);
    }

    public void bindCurrentPriceLabel() {
        if (currentPriceLabel == null || currentPriceListener != null) {
            return;
        }

        currentPriceListener = (observable, oldValue, newValue) -> {
            Double price = parsePrice(newValue);
            if (price != null) {
                updateOnNewBid(price);
            }
        };
        currentPriceLabel.textProperty().addListener(currentPriceListener);
    }

    private Double parsePrice(String labelText) {
        if (labelText == null) {
            return null;
        }

        String cleanPrice = labelText.replaceAll("[^\\d.]", "");
        if (cleanPrice.isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void updateOnNewBid(double newPrice) {
        Platform.runLater(() -> addPoint(newPrice));
    }

    private void addPoint(double price) {
        if (priceSeries == null) {
            return;
        }

        priceSeries.getData().add(new XYChart.Data<>(LocalTime.now().format(timeFormatter), price));
        if (priceSeries.getData().size() > 15) {
            priceSeries.getData().remove(0);
        }
    }
}
