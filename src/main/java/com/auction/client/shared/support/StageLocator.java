package com.auction.client.shared.support;

import javafx.stage.Stage;

/**
 * Hợp đồng tìm Stage chính của ứng dụng.
 */
public interface StageLocator {
    Stage resolveMainStage();
}
