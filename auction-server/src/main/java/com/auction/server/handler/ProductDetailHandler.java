package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.AuctionRoom;

public class ProductDetailHandler implements ClientActionHandler {

    @Override
    public boolean canHandle(String action) {
        return "GET_PRODUCT_DETAILS".equals(action);
    }

    @Override
    public void handle(Message message, ClientActionContext context) {
        try {
            // Lấy roomId từ Client gửi lên
            String roomId = (String) message.getData();

            // --- BẮT ĐẦU ĐOẠN GIẢ LẬP (Xóa đi khi ráp DB thật) ---
            AuctionRoom room = new AuctionRoom();
            room.setRoomId(roomId);
            room.setItemName("Sản phẩm mẫu " + roomId);
            room.setCurrentPrice(5000.0);
            room.setSellerName("hanto_seller");
            room.setItemDescription("Đây là mô tả chi tiết của sản phẩm. Máy nguyên seal, chưa bóc hộp...");
            // --- KẾT THÚC ĐOẠN GIẢ LẬP ---

            if (room != null) {
                AuctionImageRegistry.apply(room);
                // Tìm thấy -> Trả về Client với action PRODUCT_DETAILS_SUCCESS
                context.send(new Message("PRODUCT_DETAILS_SUCCESS", "SERVER", room));
            } else {
                // Không tìm thấy -> Trả về lỗi
                context.send(new Message("PRODUCT_DETAILS_ERROR", "SERVER", "Không tìm thấy phòng đấu giá này trên hệ thống."));
            }
        } catch (Exception e) {
            context.send(new Message("PRODUCT_DETAILS_ERROR", "SERVER", "Lỗi máy chủ khi tải chi tiết sản phẩm."));
        }
    }
}
