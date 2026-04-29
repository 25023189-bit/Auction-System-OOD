package com.auction.client.feature.lobby;

/**
 * Hợp đồng tạo card lobby với hai trạng thái hiển thị: thường và nổi bật.
 */
public interface AbstractAuctionCardFactory<T, R> {
    R createDefault(T source);

    R createHighlighted(T source);
}
