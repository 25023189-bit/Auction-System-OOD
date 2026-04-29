package com.auction.client.core.ui;

/**
 * Hợp đồng bind dữ liệu model/viewmodel vào control UI.
 */
public interface ViewStateBinder<T> {
    void bind(T data);
}
