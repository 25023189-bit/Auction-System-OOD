package com.auction.server.handler;

import com.auction.common.dto.Message;
import com.auction.common.model.User;
import com.auction.server.dao.UserDAO;
import com.auction.server.service.AuthService;
import com.auction.server.service.ForgotPasswordService;
import com.auction.server.service.PasswordStrengthValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthActionHandlerTest {

    private AuthActionHandler handler;
    private ClientActionContext mockContext;
    private AuthService mockAuthService;

    // Mock các constructor khởi tạo nằm bên trong class nghiệp vụ
    private MockedConstruction<ForgotPasswordService> mockForgotServiceConstruction;
    private MockedConstruction<PasswordStrengthValidator> mockValidatorConstruction;
    private MockedConstruction<UserDAO> mockUserDbConstruction;

    @BeforeEach
    void setUp() {
        mockContext = mock(ClientActionContext.class);
        mockAuthService = mock(AuthService.class);
        when(mockContext.getAuthService()).thenReturn(mockAuthService);

        // BẮT BUỘC: Phải kích hoạt mock construction TRƯỚC KHI "new AuthActionHandler()"
        // vì các service này được khởi tạo ngay trong constructor của file gốc.
        mockForgotServiceConstruction = mockConstruction(ForgotPasswordService.class);
        mockValidatorConstruction = mockConstruction(PasswordStrengthValidator.class);

        handler = new AuthActionHandler();
    }

    @AfterEach
    void tearDown() {
        // Dọn dẹp chiến trường mock
        if (mockForgotServiceConstruction != null) mockForgotServiceConstruction.close();
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
        // Gửi thiếu dữ liệu (Form chuẩn cần ít nhất 7 trường)
        Message message = new Message("REGISTER", "client", "id|username|email");

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "REGISTER_FAIL".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test luồng ĐĂNG KÝ thất bại do chọn sai vai trò hoặc mật khẩu yếu")
    void testHandleRegister_ValidationFails() {
        // Case 1: Sai Role (Không phải BIDDER/SELLER/ADMIN)
        String wrongRoleData = "||test@uet.vn|HanTo|123456|GIAO_VIEN|UET";
        Message msgWrongRole = new Message("REGISTER", "client", wrongRoleData);

        handler.handle(msgWrongRole, mockContext);
        verify(mockContext, atLeastOnce()).send(argThat(msg -> "REGISTER_FAIL".equals(msg.getAction())));

        // Case 2: Mật khẩu quá yếu
        String weakPassData = "||test@uet.vn|HanTo|123|BIDDER|UET";
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
        String validData = "BD50001|hanto|han@uet.vn|To Bao Han|StrongPass123!|BIDDER|UET";
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
        verify(mockContext).send(successResponse);
    }

    @Test
    @DisplayName("Test luồng QUÊN MẬT KHẨU (FORGOT_PASSWORD) xử lý đa kịch bản phản hồi")
    void testHandleForgotPassword_AllCases() {
        Message message = new Message("FORGOT_PASSWORD", "hanto_uet", null);
        ForgotPasswordService mockForgot = mockForgotServiceConstruction.constructed().get(0);

        // Case 1: Gửi OTP thành công
        when(mockForgot.processForgotPassword("hanto_uet")).thenReturn("SUCCESS|TOKEN_123");
        handler.handle(message, mockContext);
        verify(mockContext, atLeastOnce()).send(argThat(msg -> "FORGOT_PASSWORD_OTP_SENT".equals(msg.getAction())));

        // Case 2: Không tìm thấy tài khoản (USER_NOT_FOUND)
        when(mockForgot.processForgotPassword("hanto_uet")).thenReturn("USER_NOT_FOUND");
        handler.handle(message, mockContext);
        verify(mockContext, atLeastOnce()).send(argThat(msg -> "FORGOT_PASSWORD_FAIL".equals(msg.getAction())));

        // Case 3: Tài khoản bị giới hạn lượt gửi (RATE_LIMITED)
        when(mockForgot.processForgotPassword("hanto_uet")).thenReturn("RATE_LIMITED");
        when(mockForgot.getRemainingRequests("hanto_uet")).thenReturn(2);
        handler.handle(message, mockContext);
        verify(mockContext, atLeastOnce()).send(argThat(msg -> "FORGOT_PASSWORD_FAIL".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test luồng XÁC THỰC MÃ OTP (VERIFY_OTP) thành công")
    void testHandleVerifyOTP_Success() {
        Message message = new Message("VERIFY_OTP", "client", "TOKEN_123|999999");
        ForgotPasswordService mockForgot = mockForgotServiceConstruction.constructed().get(0);

        when(mockForgot.verifyOTPAndGetTemporaryPassword("TOKEN_123", "999999")).thenReturn("SUCCESS|TempPassWord");

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "OTP_VERIFY_SUCCESS".equals(msg.getAction())));
    }

    @Test
    @DisplayName("Test luồng CẬP NHẬT MẬT KHẨU MỚI (UPDATE_NEW_PASSWORD) thành công")
    void testHandleUpdateNewPassword_Success() {
        Message message = new Message("UPDATE_NEW_PASSWORD", "USER_HAN", "NewPass123!|NewPass123!|");
        ForgotPasswordService mockForgot = mockForgotServiceConstruction.constructed().get(0);

        when(mockForgot.validateAndUpdatePassword("USER_HAN", "NewPass123!", "NewPass123!")).thenReturn("SUCCESS");

        handler.handle(message, mockContext);
        verify(mockContext).send(argThat(msg -> "PASSWORD_UPDATE_SUCCESS".equals(msg.getAction())));
    }
}