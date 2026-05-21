package com.auction.client.network.dispatcher;

/**
 * Hợp đồng thực thi kết quả định tuyến response ở phía client.
 *
 * Vai trò:
 * - Tách bước gọi MessageHandler ra khỏi ResponseRouter.
 * - Chuẩn hóa điểm vào dispatch cho phản hồi từ server.
 *
 * Luồng chính:
 * 1. AuctionController nhận MessageRouteResult từ router.
 * 2. AuctionController gọi dispatch(result) để handler cập nhật UI/session.
 */
public interface ResponseDispatcher {
    void dispatch(MessageRouteResult routeResult);
}
