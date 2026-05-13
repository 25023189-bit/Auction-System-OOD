package com.auction.server.service;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO mockUserDAO;

    @Mock
    private AuctionDAO mockAuctionDAO;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setCustomerId("U001");
        sampleUser.setUsername("testuser");
    }

    @Test
    @DisplayName("Login thất bại khi sai tài khoản/mật khẩu")
    void login_InvalidCredentials_ReturnsFailMessage() {
        when(mockUserDAO.login("wrongUser", "wrongPass")).thenReturn(null);

        Message response = authService.login("wrongUser", "wrongPass");

        assertEquals("LOGIN_FAIL", response.getAction());
        assertEquals("SERVER", response.getId());
        assertEquals("Sai tài khoản (ID/Username) hoặc mật khẩu!", response.getData());
    }

    @Test
    @DisplayName("Login thành công với quyền BUYER (Không tính toán tỷ lệ)")
    void login_ValidBuyer_ReturnsSuccessMessage() {
        sampleUser.setRole("BUYER");
        when(mockUserDAO.login("validUser", "validPass")).thenReturn(sampleUser);

        Message response = authService.login("validUser", "validPass");

        assertEquals("LOGIN_SUCCESS", response.getAction());
        assertSame(sampleUser, response.getData(), "Dữ liệu trả về phải là chính đối tượng User đó");
        verify(mockAuctionDAO, never()).getSellerAuctionStats(anyString());
    }

    @Test
    @DisplayName("Login thành công với quyền SELLER (Có tính toán tỷ lệ)")
    void login_ValidSeller_ReturnsSuccessMessageWithStats() {
        sampleUser.setRole("SELLER");
        when(mockUserDAO.login("sellerUser", "validPass")).thenReturn(sampleUser);

        // GIẢI PHÁP: Dùng Mockito làm giả luôn cái SellerAuctionStats để né lỗi Constructor
        AuctionDAO.SellerAuctionStats mockStats = mock(AuctionDAO.SellerAuctionStats.class);
        when(mockStats.getSuccessfulAuctionRate()).thenReturn(85.5);
        when(mockStats.getAdminCancellationRate()).thenReturn(2.0);

        when(mockAuctionDAO.getSellerAuctionStats("U001")).thenReturn(mockStats);

        Message response = authService.login("sellerUser", "validPass");

        assertEquals("LOGIN_SUCCESS", response.getAction());
        User returnedUser = (User) response.getData();
        assertEquals(85.5, returnedUser.getSuccessfulAuctionRate());
        assertEquals(2.0, returnedUser.getAdminCancellationRate());
    }

    @Test
    void registerUser_Success_ReturnsSuccessMessage() {
        when(mockUserDAO.registerUser(sampleUser, "rawPass123")).thenReturn("SUCCESS");

        Message response = authService.registerUser(sampleUser, "rawPass123");

        assertEquals("REGISTER_SUCCESS", response.getAction());
        assertEquals("testuser", response.getData());
    }

    @Test
    void registerUser_Duplicate_ReturnsFailMessage() {
        when(mockUserDAO.registerUser(sampleUser, "rawPass123")).thenReturn("DUPLICATE");

        Message response = authService.registerUser(sampleUser, "rawPass123");

        assertEquals("REGISTER_FAIL", response.getAction());
        assertEquals("Username is already in use!", response.getData());
    }

    @Test
    void registerUser_DatabaseError_ReturnsFailMessage() {
        when(mockUserDAO.registerUser(sampleUser, "rawPass123")).thenReturn("DB_CONNECTION_LOST");

        Message response = authService.registerUser(sampleUser, "rawPass123");

        assertEquals("REGISTER_FAIL", response.getAction());
        assertEquals("DB_CONNECTION_LOST", response.getData());
    }

    @Test
    void resetPassword_InvalidDataFormat_ReturnsFailMessage() {
        Message response = authService.resetPassword("U001", "newPassNoColon");

        assertEquals("RESET_FAIL", response.getAction());
        assertEquals("Invalid request data!", response.getData());
    }

    @Test
    void resetPassword_ValidDataButDatabaseFails_ReturnsFailMessage() {
        when(mockUserDAO.resetPassword("U001", "newPass", "confirmPass")).thenReturn(false);

        Message response = authService.resetPassword("U001", "newPass:confirmPass");

        assertEquals("RESET_FAIL", response.getAction());
        assertTrue(response.getData().toString().contains("Unable to change password"));
    }

    @Test
    void resetPassword_ValidDataAndSuccess_ReturnsSuccessMessage() {
        when(mockUserDAO.resetPassword("U001", "newPass", "newPass")).thenReturn(true);

        Message response = authService.resetPassword("U001", "newPass:newPass");

        assertEquals("RESET_SUCCESS", response.getAction());
        assertEquals("Password changed successfully!", response.getData());
    }
}