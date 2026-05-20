package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.AuthService;
import com.auction.server.service.PasswordStrengthValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthActionHandlerTest {

    private AuthActionHandler handler;
    private ClientActionContext mockContext;
    private AuthService mockAuthService;

    // Mock các constructor khởi tạo nằm bên trong class nghiệp vụ
    private MockedConstruction<PasswordStrengthValidator> mockValidatorConstruction;
    private MockedConstruction<UserDAO> mockUserDbConstruction;

    @BeforeEach
    void setUp() {
        mockContext = mock(ClientActionContext.class);
        mockAuthService = mock(AuthService.class);
        when(mockContext.getAuthService()).thenReturn(mockAuthService);

        // BẮT BUỘC: Phải kích hoạt mock construction TRƯỚC KHI "new AuthActionHandler()"
        // vì các service này được khởi tạo ngay trong constructor của file gốc.
        mockValidatorConstruction = mockConstruction(PasswordStrengthValidator.class);

        handler = new AuthActionHandler();
    }

    @AfterEach
    void tearDown() {
        // Dọn dẹp chiến trường mock
        if (mockValidatorConstruction != null) mockValidatorConstruction.close();
        if (mockUserDbConstruction != null) mockUserDbConstruction.close();
    }

    @Test
    @DisplayName("Test xử lý mã Action không được hỗ trợ")
    void testHandle_UnknownAction() {
        Message message = new Message("HACK_MAT_KHAU", "Rác");
        handler.handle(message, mockContext);

        verify(mockContext).send(argThat(msg -> "UNKNOWN_ACTION".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test luồng ĐĂNG NHẬP (LOGIN) thành công và ghi nhận session")
    void testHandleLogin_Success() {
        Message message = new Message("LOGIN", "hanto_uet", "pass123");

        User loggedInUser = new User();
        loggedInUser.setCustomerId("BD50001");

        Message mockResponse = new Message("LOGIN_SUCCESS", "SERVER", loggedInUser);
        when(mockAuthService.login("hanto_uet", "pass123")).thenReturn(mockResponse);

        handler.handle(message, mockContext);

        // Xác thực hệ thống phải lưu lại UserId vào session kết nối
        verify(mockContext).setUserId("BD50001");
        verify(mockContext).send(mockResponse);
    }

    @Test
    @DisplayName("Test luồng ĐĂNG KÝ (REGISTER) bị lỗi do thiếu trường dữ liệu (|)")
    void testHandleRegister_InvalidDataLength() {
        // Gửi thiếu dữ liệu (Form chuẩn cần ít nhất 6 trường)
        Message message = new Message("REGISTER", "client", "id|username|fullName");

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "REGISTER_FAIL".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test luồng ĐĂNG KÝ thất bại do chọn sai vai trò hoặc mật khẩu yếu")
    void testHandleRegister_ValidationFails() {
        // Case 1: Sai Role (Không phải BIDDER/SELLER/ADMIN)
        String wrongRoleData = "||HanTo|123456|GIAO_VIEN|UET";
        Message msgWrongRole = new Message("REGISTER", "client", wrongRoleData);

        handler.handle(msgWrongRole, mockContext);
        verify(mockContext, atLeastOnce()).send(argThat(msg -> "REGISTER_FAIL".equals(msg.getAction())));

        // Case 2: Mật khẩu quá yếu
        String weakPassData = "||HanTo|123|BIDDER|UET";
        Message msgWeakPass = new Message("REGISTER", "client", weakPassData);

        // Lấy thực thể Validator giả lập ra để ép nó trả về false (Mật khẩu yếu)
        PasswordStrengthValidator mockValidator = mockValidatorConstruction.constructed().get(0);
        when(mockValidator.isStrong("123")).thenReturn(false);
        when(mockValidator.getLastError()).thenReturn("Too short");

        handler.handle(msgWeakPass, mockContext);
        verify(mockContext, atLeastOnce()).send(argThat(msg -> "REGISTER_FAIL".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test luồng ĐĂNG KÝ (REGISTER) thành công rực rỡ")
    void testHandleRegister_Success() {
        String validData = "BD50001|hanto|To Bao Han|StrongPass123!|BIDDER|UET";
        Message message = new Message("REGISTER", "client", validData);

        PasswordStrengthValidator mockValidator = mockValidatorConstruction.constructed().get(0);
        when(mockValidator.isStrong("StrongPass123!")).thenReturn(true);

        // Ép AuthService trả về thông báo Đăng ký thành công
        Message successResponse = new Message("REGISTER_SUCCESS", "SERVER", "Đăng ký xong");
        when(mockAuthService.registerUser(any(), eq("StrongPass123!"))).thenReturn(successResponse);

        // Đánh lừa hàm sinh ID tự động bằng cách mock UserDAO local
        mockUserDbConstruction = mockConstruction(UserDAO.class, (mock, context) -> {
            when(mock.generateNextCustomerId()).thenReturn("BD59999");
        });

        handler.handle(message, mockContext);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockAuthService).registerUser(userCaptor.capture(), eq("StrongPass123!"));

        User registeredUser = userCaptor.getValue();
        assertEquals("hanto", registeredUser.getUsername());
        assertEquals("To Bao Han", registeredUser.getFullName());

        verify(mockContext).send(successResponse);
    }

}
