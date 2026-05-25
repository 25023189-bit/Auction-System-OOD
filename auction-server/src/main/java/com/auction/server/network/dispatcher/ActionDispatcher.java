package com.auction.server.network.dispatcher;

/**
 * Hợp đồng thực thi kết quả định tuyến action ở phía server.
 *
 * Vai trò:
 * - Tách bước gọi handler ra khỏi ClientActionRouter.
 * - Chuẩn hóa điểm vào dispatch cho các request từ client.
 *
 * Luồng chính:
 * 1. ClientHandler nhận ActionRouteResult từ router.
 * 2. ClientHandler gọi dispatch(result) để handler thật sự được thực thi.
 *
 * Business rules:
 * - Dispatcher phải kiểm tra result hợp lệ trước khi gọi handler.
 * - Action không có handler cần được xử lý tập trung ở dispatcher.
 */
public interface ActionDispatcher {
    void dispatch(ActionRouteResult routeResult);
}
