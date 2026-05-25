package com.auction.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class AutoBidRequestTest {

    @Test
    @DisplayName("Kiểm tra Constructor và Getter: Dữ liệu khởi tạo phải được giữ nguyên vẹn")
    void testConstructorAndGetters() {
        // 1. Chuẩn bị dữ liệu đầu vào
        String expectedRoomId = "ROOM_VIP_01";
        double expectedMaxBid = 10000.0;
        double expectedIncrement = 500.0;

        // 2. Khởi tạo đối tượng
        AutoBidRequest request = new AutoBidRequest(expectedRoomId, expectedMaxBid, expectedIncrement);

        // 3. Xác thực Getter trả về đúng dữ liệu
        assertEquals(expectedRoomId, request.getRoomId(), "Room ID không khớp");
        assertEquals(expectedMaxBid, request.getMaxBid(), "Giá trần (Max Bid) không khớp");
        assertEquals(expectedIncrement, request.getIncrement(), "Bước giá (Increment) không khớp");
    }

    @Test
    @DisplayName("Kiểm tra Serialization: Đối tượng phải giữ nguyên dữ liệu khi truyền qua mạng (Socket)")
    void testSerialization() throws IOException, ClassNotFoundException {
        // 1. Tạo một đối tượng gốc
        AutoBidRequest originalRequest = new AutoBidRequest("ROOM_888", 5000.0, 100.0);

        // 2. Đóng gói đối tượng thành mảng byte (Giả lập việc gửi qua Socket)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalRequest);
        oos.close();

        // 3. Mở gói từ mảng byte (Giả lập việc Server nhận dữ liệu)
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        AutoBidRequest deserializedRequest = (AutoBidRequest) ois.readObject();
        ois.close();

        // 4. Xác thực đối tượng mới được tạo ra từ byte có dữ liệu y hệt đối tượng gốc
        assertNotNull(deserializedRequest);
        assertEquals(originalRequest.getRoomId(), deserializedRequest.getRoomId());
        assertEquals(originalRequest.getMaxBid(), deserializedRequest.getMaxBid());
        assertEquals(originalRequest.getIncrement(), deserializedRequest.getIncrement());
    }
}