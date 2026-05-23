package com.auction.client.feature.lobby;

import com.auction.common.model.User;
import com.auction.common.role.RolePolicy;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class LobbyUserInfoBinderTest {
    private Label lblUsername;
    private Label lblBalance;
    private Button btnCreateAuction;
    private RolePolicy mockPolicy;
    private LobbyUserInfoBinder binder;

    @BeforeAll
    static void initToolkit() {
        try { Platform.startup(() -> {}); } catch (Exception ignored) {}
    }

    @BeforeEach
    void setUp() {
        lblUsername = mock(Label.class);
        lblBalance = mock(Label.class);
        btnCreateAuction = mock(Button.class);
        mockPolicy = mock(RolePolicy.class);
        binder = new LobbyUserInfoBinder(lblUsername, lblBalance, btnCreateAuction, mockPolicy);
    }

    @Test
    @DisplayName("Test thoát an toàn khi user chưa đăng nhập (null)")
    void testBind_NullUser() {
        binder.bind(null);
        verifyNoInteractions(lblUsername, lblBalance, btnCreateAuction, mockPolicy);
    }

    @Test
    @DisplayName("Test hiển thị tiền và KHÓA nút Tạo phòng với User thường")
    void testBind_UserWithoutCreatePermission() {
        User user = new User();
        user.setUsername("hanto");
        user.setBalance(5000.0);

        when(mockPolicy.canCreateAuction(user)).thenReturn(false);

        binder.bind(user);

        verify(lblUsername).setText("Welcome: hanto");
        verify(lblBalance).setText(contains("5,000 $"));
        verify(btnCreateAuction).setVisible(false);
        verify(btnCreateAuction).setManaged(false);
    }

    @Test
    @DisplayName("Test hiển thị tiền và MỞ nút Tạo phòng với Seller")
    void testBind_UserWithCreatePermission() {
        User user = new User();
        user.setUsername("seller_pro");

        when(mockPolicy.canCreateAuction(user)).thenReturn(true);

        binder.bind(user);

        verify(btnCreateAuction).setVisible(true);
        verify(btnCreateAuction).setManaged(true);
    }
}
