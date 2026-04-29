package com.auction.client.feature.room;

/**
 * DTO nhỏ lấy trực tiếp số tiền bid từ TextField trước khi parse.
 */
public record BidRequest(String amountText) {}
