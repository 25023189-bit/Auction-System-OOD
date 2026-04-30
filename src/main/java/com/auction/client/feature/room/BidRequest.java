package com.auction.client.feature.room;


public record BidRequest(String amountText) {
}
/**
 * DTO nhỏ lấy trực tiếp số tiền bid từ TextField trước khi parse.
 */
