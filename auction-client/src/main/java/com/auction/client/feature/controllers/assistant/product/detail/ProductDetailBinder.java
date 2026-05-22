package com.auction.client.feature.controllers.assistant.product.detail;

import javafx.scene.control.Label;

public class ProductDetailBinder {
    private final Label lblProductName;
    private final Label lblDescription;
    private final Label lblStartingPrice;

    public ProductDetailBinder(Label lblProductName, Label lblDescription, Label lblStartingPrice) {
        this.lblProductName = lblProductName;
        this.lblDescription = lblDescription;
        this.lblStartingPrice = lblStartingPrice;
    }

    public void bind(ProductDetailViewModel viewModel) {
        if (viewModel == null) return;
        if (lblProductName != null) lblProductName.setText(viewModel.title());
        if (lblDescription != null) lblDescription.setText(viewModel.description());
        if (lblStartingPrice != null) lblStartingPrice.setText(viewModel.startingPrice());
    }

    public void showLoading() {
        bind(new ProductDetailViewModel("Product Detail Information", "Loading data from server...", ""));
    }

    public void showNotFound() {
        bind(new ProductDetailViewModel("Product Detail Information", "Product information not found!", ""));
    }
}
