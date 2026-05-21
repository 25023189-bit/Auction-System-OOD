package com.auction.client.feature.controllers.viewmodel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthViewModelTest {

    @Test
    @DisplayName("Test giá trị khởi tạo mặc định của AuthViewModel")
    void testDefaultValues() {
        AuthViewModel vm = new AuthViewModel();

        // Xác thực toàn bộ các chuỗi rỗng mặc định ban đầu
        assertEquals("", vm.getUsername());
        assertEquals("", vm.getPassword());
        assertEquals("", vm.getRegisterUsername());
        assertEquals("", vm.getRegisterPassword());
        assertEquals("", vm.getRegisterConfirmPassword());
        assertEquals("", vm.getRegisterOrganization());
        assertEquals("", vm.getForgotUsername());
        assertEquals("", vm.getForgotPassword());
        assertEquals("", vm.getForgotConfirmPassword());

        // Biến role mặc định bắt buộc phải là BIDDER theo luật giao diện
        assertEquals("BIDDER", vm.getRegisterRole());
    }

    @Test
    @DisplayName("Test toàn bộ các hàm Getter và Setter")
    void testGettersAndSetters() {
        AuthViewModel vm = new AuthViewModel();

        // Tiến hành dội bom dữ liệu qua Setter
        vm.setUsername("tobahan_uet");
        vm.setPassword("matkhau123");
        vm.setRegisterUsername("han_to_vip");
        vm.setRegisterPassword("strongPass!");
        vm.setRegisterConfirmPassword("strongPass!");
        vm.setRegisterRole("SELLER");
        vm.setRegisterOrganization("VNU_UET");
        vm.setForgotUsername("forgot_han");
        vm.setForgotPassword("newPass123");
        vm.setForgotConfirmPassword("newPass123");

        // Rút dữ liệu ra qua Getter để ăn trọn điểm dòng lệnh
        assertEquals("tobahan_uet", vm.getUsername());
        assertEquals("matkhau123", vm.getPassword());
        assertEquals("han_to_vip", vm.getRegisterUsername());
        assertEquals("strongPass!", vm.getRegisterPassword());
        assertEquals("strongPass!", vm.getRegisterConfirmPassword());
        assertEquals("SELLER", vm.getRegisterRole());
        assertEquals("VNU_UET", vm.getRegisterOrganization());
        assertEquals("forgot_han", vm.getForgotUsername());
        assertEquals("newPass123", vm.getForgotPassword());
        assertEquals("newPass123", vm.getForgotConfirmPassword());
    }
}