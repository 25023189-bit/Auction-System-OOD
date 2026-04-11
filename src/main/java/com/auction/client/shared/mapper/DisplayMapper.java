package com.auction.client.shared.mapper;

public interface DisplayMapper<S, T> {
    T map(S source);
}