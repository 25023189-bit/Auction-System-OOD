package com.auction.client.feature.controllers.assistant.product.detail;

import com.auction.client.shared.utils.ImageUtils;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ProductDetailBinder {
    private final Label lblProductName;
    private final Label lblDescription;
    private final Label lblStartingPrice;
    private final ImageView imgItem;

    public ProductDetailBinder(Label lblProductName, Label lblDescription, Label lblStartingPrice) {
        this(lblProductName, lblDescription, lblStartingPrice, null);
    }

    public ProductDetailBinder(Label lblProductName, Label lblDescription, Label lblStartingPrice, ImageView imgItem) {
        this.lblProductName = lblProductName;
        this.lblDescription = lblDescription;
        this.lblStartingPrice = lblStartingPrice;
        this.imgItem = imgItem;
    }

    public void bind(ProductDetailViewModel viewModel) {
        if (viewModel == null) return;
        if (lblProductName != null) lblProductName.setText(viewModel.title());
        if (lblDescription != null) lblDescription.setText(viewModel.description());
        if (lblStartingPrice != null) lblStartingPrice.setText(viewModel.startingPrice());
        ImageUtils.applyBase64OrPlaceholder(imgItem, viewModel.base64Image());
    }

    public void showLoading() {
        bind(new ProductDetailViewModel("Product Detail Information", "Loading data from server...", "", ""));
    }

    public void showNotFound() {
        bind(new ProductDetailViewModel("Product Detail Information", "Product information not found!", "", ""));
    }
}
