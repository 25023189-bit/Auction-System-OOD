package com.auction.client.shared.mapper;

/**
 * Mapper chung để chuyển dữ liệu server/model sang dữ liệu phục vụ hiển thị.
 */
public interface DisplayMapper<S, T> {
    T map(S source);
}
