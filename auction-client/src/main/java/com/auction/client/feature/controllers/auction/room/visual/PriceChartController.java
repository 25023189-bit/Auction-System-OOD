package com.auction.client.feature.controllers.auction.room.visual;

import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PriceChartController {
    private final AreaChart<String, Number> priceChart;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private XYChart.Series<String, Number> priceSeries;

    public PriceChartController(AreaChart<String, Number> priceChart) {
        this.priceChart = priceChart;
    }

    public void initialize(double startPrice) {
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Lịch sử giá");
        if (priceChart != null) {
            priceChart.getData().clear();
            priceChart.getData().add(priceSeries);
        }
        addPoint(startPrice);
    }

    public void addPoint(double price) {
        if (priceSeries == null) return;
        priceSeries.getData().add(new XYChart.Data<>(LocalTime.now().format(timeFormatter), price));
        if (priceSeries.getData().size() > 15) {
            priceSeries.getData().remove(0);
        }
    }
}
