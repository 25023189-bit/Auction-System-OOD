package com.auction.client.feature.room;

/**
 * DTO chứa nội dung chat trước khi gửi lên server.
 */
public record ChatRequest(String content) {}
