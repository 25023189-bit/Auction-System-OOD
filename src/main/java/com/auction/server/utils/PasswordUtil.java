package com.auction.server.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích hash và kiểm tra mật khẩu bằng BCrypt.
 *
 * Vai trò:
 * - Hash mật khẩu thô trước khi lưu vào database.
 * - Verify mật khẩu người dùng nhập với password_hash đã lưu.
 *
 * Luồng chính:
 * 1. UserDAO gọi hashPassword() khi đăng ký hoặc đổi mật khẩu.
 * 2. UserDAO.login() gọi checkPassword() để xác thực mật khẩu.
 *
 * Business rules:
 * - Không lưu hoặc so sánh mật khẩu thô trong database.
 * - BCrypt tự sinh salt và dùng cost 12 cho mỗi lần hash.
 *
 * Ghi chú kỹ thuật:
 * - Thread-safe: stateless, chỉ dùng static method của BCrypt.
 * - Dependency: org.mindrot.jbcrypt.BCrypt.
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
