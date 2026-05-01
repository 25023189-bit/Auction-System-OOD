package com.auction.client.feature.room;

/**
 * DTO chứa nội dung chat trước khi gửi lên server.
 *
 * Vai trò:
 * - Truyền raw content từ ô nhập chat sang ChatActionHandler.
 * - Tách dữ liệu UI khỏi logic gửi tin.
 *
 * Luồng chính:
 * 1. AuctionController tạo ChatRequest khi người dùng bấm gửi chat.
 * 2. ChatActionHandler kiểm tra/trim content rồi gọi AuctionService.
 *
 * Business rules:
 * - DTO không tự loại bỏ nội dung rỗng; handler chịu trách nhiệm validate.
 * - Server sẽ broadcast tin hợp lệ cho các client trong phòng.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: record immutable sau khi khởi tạo.
 * - Dependency: ChatActionHandler.
 */
public record ChatRequest(String content) {}
