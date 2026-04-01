package com.auction.server.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hàm 1: Băm mật khẩu (Dùng khi người dùng Đăng ký)
    public static String hashPassword(String plainTextPassword) {
        // Trộn thêm "muối" (salt 12) để tăng độ bảo mật tuyệt đối
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    // Hàm 2: Kiểm tra mật khẩu (Dùng khi người dùng Đăng nhập)
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}