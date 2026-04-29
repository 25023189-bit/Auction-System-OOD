package com.auction.client.feature.room;

/**
 * Hợp đồng chung cho các handler xử lý thao tác người dùng trong phòng đấu giá.
 */
public interface ActionHandler<T> {
    void handle(T request);
}
