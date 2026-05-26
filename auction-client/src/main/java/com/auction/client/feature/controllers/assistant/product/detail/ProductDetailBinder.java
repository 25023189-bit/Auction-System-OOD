package com.auction.client.feature.controllers.assistant.product.detail;

import com.auction.client.shared.utils.ImageUtils;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ProductDetailBinder {
    private final Label lblProductName;
    private final Label lblDescription;
    private final Label lblCurrentPrice;
    private final Label lblBidCount;
    private final Label lblTimeRemaining;
    private final ImageView imgItem;

    public ProductDetailBinder(Label lblProductName, Label lblDescription, Label lblCurrentPrice) {
        this(lblProductName, lblDescription, lblCurrentPrice, null, null, null);
    }

    public ProductDetailBinder(
            Label lblProductName,
            Label lblDescription,
            Label lblCurrentPrice,
            Label lblBidCount,
            Label lblTimeRemaining,
            ImageView imgItem
    ) {
        this.lblProductName = lblProductName;
        this.lblDescription = lblDescription;
        this.lblCurrentPrice = lblCurrentPrice;
        this.lblBidCount = lblBidCount;
        this.lblTimeRemaining = lblTimeRemaining;
        this.imgItem = imgItem;
    }

    public void bind(ProductDetailViewModel viewModel) {
        if (viewModel == null) {
            return;
        }
        if (lblProductName != null) {
            lblProductName.setText(viewModel.title());
        }
        if (lblDescription != null) {
            lblDescription.setText(viewModel.description());
        }
        if (lblCurrentPrice != null) {
            lblCurrentPrice.setText(viewModel.currentPrice());
        }
        if (lblBidCount != null) {
            lblBidCount.setText(viewModel.bidCount());
        }
        if (lblTimeRemaining != null) {
            lblTimeRemaining.setText(viewModel.timeRemaining());
        }
        ImageUtils.applyBase64OrPlaceholder(imgItem, viewModel.base64Image());
    }

    public void showLoading() {
        bind(new ProductDetailViewModel(
                "Product Detail Information", "Loading data from server...", "", "", "", ""));
    }

    public void showNotFound() {
        bind(new ProductDetailViewModel(
                "Product Detail Information", "Product information not found!", "", "", "", ""));
    }
}
