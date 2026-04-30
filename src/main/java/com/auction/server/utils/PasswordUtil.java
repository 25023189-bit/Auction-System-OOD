package com.auction.server.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích hash và kiểm tra mật khẩu bằng BCrypt.
 */
public class PasswordUtil {

    // Hash mật khẩu trước khi lưu DB.
    public static String hashPassword(String plainTextPassword) {
        // BCrypt tự sinh salt; cost 12 cân bằng giữa bảo mật và hiệu năng.
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    // Kiểm tra mật khẩu lúc đăng nhập bằng hash đã lưu.
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
