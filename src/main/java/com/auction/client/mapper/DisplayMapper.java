package com.auction.client.mapper;

public interface DisplayMapper<S, T> {
    T map(S source);
}