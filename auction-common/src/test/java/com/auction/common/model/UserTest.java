package com.auction.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    @Test
    @DisplayName("Test Constructor 1: Truyền customerId, password, role")
    void testConstructor_IdPassRole() {
        User user = new User("C001", "pass123", "BIDDER");

        assertEquals("C001", user.getCustomerId());
        assertEquals("C001", user.getUsername()); // Constructor này ép username = customerId
        assertEquals("pass123", user.getPassword());
        assertEquals("BIDDER", user.getRole());
        assertNull(user.getOrganization());
        assertEquals(0.0, user.getBalance());
    }

    @Test
    @DisplayName("Test Constructor 2: Không có Organization")
    void testConstructor_WithoutOrganization() {
        User user = new User("C002", "hanto", "SELLER", "pass456", 1500.0);

        assertEquals("C002", user.getCustomerId());
        assertEquals("hanto", user.getUsername());
        assertEquals("SELLER", user.getRole());
        assertEquals("pass456", user.getPassword());
        assertEquals(1500.0, user.getBalance());
        assertNull(user.getOrganization());
    }

    @Test
    @DisplayName("Test Constructor 3: Truyền đầy đủ tất cả tham số (Full)")
    void testConstructor_Full() {
        User user = new User("C003", "admin", "ADMIN", "admin123", "UET", 9999.0);

        assertEquals("C003", user.getCustomerId());
        assertEquals("admin", user.getUsername());
        assertEquals("ADMIN", user.getRole());
        assertEquals("admin123", user.getPassword());
        assertEquals("UET", user.getOrganization());
        assertEquals(9999.0, user.getBalance());
    }

    @Test
    @DisplayName("Test toàn bộ Getter, Setter tiêu chuẩn và các trường mặc định")
    void testStandardGettersAndSetters() {
        User user = new User();

        // Kiểm tra giá trị mặc định lúc mới khởi tạo
        assertEquals(5.0, user.getSellerReputation());
        assertEquals(0.0, user.getSuccessfulAuctionRate());
        assertEquals(0.0, user.getAdminCancellationRate());

        // Test Setters
        user.setEmail("han@vnu.edu.vn");
        user.setFullName("To Bao Han");
        user.setCustomerId("U123");
        user.setUsername("hanto");
        user.setPassword("secret");
        user.setRole("SELLER");
        user.setOrganization("FITA");
        user.setBalance(500.0);
        user.setSellerReputation(4.8);
        user.setSuccessfulAuctionRate(0.95);
        user.setAdminCancellationRate(0.02);

        // Test Getters
        assertEquals("han@vnu.edu.vn", user.getEmail());
        assertEquals("To Bao Han", user.getFullName());
        assertEquals("U123", user.getCustomerId());
        assertEquals("hanto", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("SELLER", user.getRole());
        assertEquals("FITA", user.getOrganization());
        assertEquals(500.0, user.getBalance());
        assertEquals(4.8, user.getSellerReputation());
        assertEquals(0.95, user.getSuccessfulAuctionRate());
        assertEquals(0.02, user.getAdminCancellationRate());

        // Test các hàm get đặc biệt
        assertEquals("U123", user.getId());
        assertEquals("secret", user.getConfirmPassword());
    }

    @Test
    @DisplayName("Test logic rẽ nhánh của getName và getUsername khi bị null hoặc rỗng")
    void testNameAndUsernameFallbackLogic() {
        User user = new User();
        user.setCustomerId("FALLBACK_ID");

        // Trường hợp 1: username bị null -> Cả getName và getUsername đều phải trả về customerId
        user.setUsername(null);
        assertEquals("FALLBACK_ID", user.getName());
        assertEquals("FALLBACK_ID", user.getUsername());

        // Trường hợp 2: username là chuỗi rỗng ""
        // -> getName() bắt lỗi isEmpty nên trả về customerId
        // -> getUsername() CHỈ xét null nên nó sẽ trả về chuỗi rỗng ""
        user.setUsername("");
        assertEquals("FALLBACK_ID", user.getName());
        assertEquals("", user.getUsername());

        // Trường hợp 3: username bình thường
        user.setUsername("validUser");
        assertEquals("validUser", user.getName());
        assertEquals("validUser", user.getUsername());
    }
}