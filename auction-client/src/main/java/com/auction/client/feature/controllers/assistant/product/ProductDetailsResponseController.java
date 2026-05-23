package com.auction.client.feature.controllers.assistant.product;

import com.auction.client.feature.controllers.assistant.product.detail.ProductDetailBinder;
import com.auction.client.feature.controllers.assistant.product.detail.ProductDetailMapper;
import com.auction.client.service.AuctionService;
import javafx.application.Platform;

import java.util.function.Consumer;

public class ProductDetailsResponseController {
    private final AuctionService auctionService;
    private final ProductDetailMapper detailMapper;
    private final ProductDetailBinder detailBinder;

    public ProductDetailsResponseController(AuctionService auctionService) {
        this(auctionService, null, null);
    }

    public ProductDetailsResponseController(
            AuctionService auctionService,
            ProductDetailMapper detailMapper,
            ProductDetailBinder detailBinder
    ) {
        this.auctionService = auctionService;
        this.detailMapper = detailMapper;
        this.detailBinder = detailBinder;
    }

    public void listen(Consumer<Object> callback) {
        auctionService.setProductDetailsCallback(callback);
    }

    public void listenAndBind() {
        if (auctionService == null || detailMapper == null || detailBinder == null) {
            return;
        }
        listen(data -> Platform.runLater(() -> detailBinder.bind(detailMapper.map(data))));
    }
}
