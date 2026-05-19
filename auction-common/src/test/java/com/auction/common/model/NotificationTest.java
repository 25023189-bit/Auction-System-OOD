package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    @DisplayName("Test Constructor và các giá trị sinh tự động")
    void testConstructorAndDefaultValues() {
        // Truyền null cho NotificationType để tránh lỗi không khớp Enum
        Notification notif = new Notification("USER_01", "ROOM_01", "Bạn đã chiến thắng!", null);

        // Kiểm tra dữ liệu Constructor
        assertEquals("USER_01", notif.getUserId());
        assertEquals("ROOM_01", notif.getAuctionId());
        assertEquals("Bạn đã chiến thắng!", notif.getMessage());
        assertNull(notif.getType());

        // Kiểm tra giá trị mặc định của biến isRead (phải là false)
        assertFalse(notif.isRead(), "Thông báo mới tạo mặc định phải ở trạng thái chưa đọc (false)");

        // Kiểm tra thời gian tạo tự động (createdAt)
        assertNotNull(notif.getCreatedAt(), "Thời gian tạo không được null vì đã được gán tự động");

        // Priority chưa được set trong Constructor nên phải là null
        assertNull(notif.getPriority());
    }

    @Test
    @DisplayName("Test toàn bộ Setter và Getter còn lại")
    void testGettersAndSetters() {
        Notification notif = new Notification("A", "B", "C", null);
        LocalDateTime pastTime = LocalDateTime.now().minusDays(2);

        // Cập nhật toàn bộ qua Setter
        notif.setNotificationId(101);
        notif.setUserId("NEW_USER");
        notif.setAuctionId("NEW_ROOM");
        notif.setMessage("Cập nhật giá");
        notif.setType(null);
        notif.setRead(true); // Đã đọc
        notif.setPriority("URGENT");
        notif.setCreatedAt(pastTime);

        // Kiểm chứng kết quả
        assertEquals(101, notif.getNotificationId());
        assertEquals("NEW_USER", notif.getUserId());
        assertEquals("NEW_ROOM", notif.getAuctionId());
        assertEquals("Cập nhật giá", notif.getMessage());
        assertNull(notif.getType());
        assertTrue(notif.isRead());
        assertEquals("URGENT", notif.getPriority());
        assertEquals(pastTime, notif.getCreatedAt());
    }
}