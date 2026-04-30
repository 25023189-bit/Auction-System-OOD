package com.auction.server.service;

import com.auction.config.ConfigManager;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.io.UnsupportedEncodingException;

/**
 * Service gửi email cho chức năng quên mật khẩu.
 * Đọc cấu hình SMTP từ ConfigManager, có retry và dùng HTML template.
 */
public class EmailService {
    private final ConfigManager config;
    // Số lần gửi lại lấy từ file cấu hình để dễ chỉnh khi deploy.
    private final int maxRetries;

    public EmailService() {
        this.config = ConfigManager.getInstance();
        this.maxRetries = config.getEmailMaxRetries();
    }

    /**
     * Gửi mật khẩu tạm thời cho user khi họ yêu cầu reset password.
     */
    public boolean sendPasswordResetEmail(String toEmail, String username, String newPassword) {
        String subject = "🔑 Auction System - Password Reset";
        String htmlContent = buildPasswordResetEmailTemplate(username, newPassword);
        return sendEmailWithRetry(toEmail, subject, htmlContent);
    }

    /**
     * Gửi mã OTP nếu bật lại luồng xác thực bằng OTP.
     */
    public boolean sendOTPEmail(String toEmail, String username, String otp, int expirationMinutes) {
        String subject = "🔐 Your Auction System Password Reset Code";
        String htmlContent = buildOTPEmailTemplate(username, otp, expirationMinutes);
        return sendEmailWithRetry(toEmail, subject, htmlContent);
    }

    /**
     * Gửi email xác nhận sau khi đổi mật khẩu thành công.
     */
    public boolean sendPasswordChangedConfirmation(String toEmail, String username, String timestamp) {
        String subject = "✅ Auction System - Password Changed Successfully";
        String htmlContent = buildPasswordChangedEmailTemplate(username, timestamp);
        return sendEmailWithRetry(toEmail, subject, htmlContent);
    }

    private boolean sendEmailWithRetry(String toEmail, String subject, String htmlContent) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                attempt++;
                return sendEmail(toEmail, subject, htmlContent);
            } catch (MessagingException e) {
                System.err.println("⚠️ Email send attempt " + attempt + "/" + maxRetries + " failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(2000); // Chờ ngắn trước khi thử gửi lại.
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        System.err.println("❌ Failed to send email after " + maxRetries + " attempts");
        return false;
    }

    private boolean sendEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getSmtpHost());
        props.put("mail.smtp.port", config.getSmtpPort());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getEmailFrom(), config.getEmailPassword());
            }
        });

        Message message = new MimeMessage(session);

        // Gắn tên người gửi thân thiện; nếu encoding lỗi thì fallback về email thô.
        try {
            message.setFrom(new InternetAddress(config.getEmailFrom(), "Auction System"));
        } catch (UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress(config.getEmailFrom()));
        }

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        Transport.send(message);
        System.out.println("✅ Email sent successfully to: " + toEmail);
        return true;
    }

    private String buildPasswordResetEmailTemplate(String username, String newPassword) {
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #2c3e50; color: white; padding: 20px; border-radius: 5px; text-align: center; }" +
                ".content { padding: 20px; background-color: #f9f9f9; border-left: 4px solid #3498db; }" +
                ".password-box { background-color: #ecf0f1; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: center; }" +
                ".password-box code { font-size: 18px; font-weight: bold; color: #e74c3c; font-family: monospace; }" +
                ".warning { background-color: #fff3cd; border: 1px solid #ffc107; padding: 10px; border-radius: 3px; margin: 15px 0; }" +
                ".footer { text-align: center; color: #7f8c8d; font-size: 12px; padding: 20px; border-top: 1px solid #ecf0f1; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>🔑 Password Reset Request</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello <strong>" + escapeHtml(username) + "</strong>,</p>" +
                "<p>We received a request to reset your password. Your new temporary password is:</p>" +
                "<div class='password-box'>" +
                "<code>" + escapeHtml(newPassword) + "</code>" +
                "</div>" +
                "<div class='warning'>" +
                "<p><strong>⚠️ IMPORTANT SECURITY NOTICE:</strong></p>" +
                "<ul>" +
                "<li>This is a temporary password and will expire in 24 hours</li>" +
                "<li>Please change this password immediately after logging in</li>" +
                "<li>Never share this password with anyone</li>" +
                "<li>If you did not request this, please ignore this email</li>" +
                "</ul>" +
                "</div>" +
                "<p style='color: #7f8c8d;'><small>This is an automated email from Auction System. Please do not reply to this email.</small></p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 Auction System. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildOTPEmailTemplate(String username, String otp, int expirationMinutes) {
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #2c3e50; color: white; padding: 20px; border-radius: 5px; text-align: center; }" +
                ".content { padding: 20px; background-color: #f9f9f9; }" +
                ".otp-box { background-color: #d4edda; border: 2px solid #28a745; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: center; }" +
                ".otp-box .code { font-size: 32px; font-weight: bold; color: #155724; font-family: monospace; letter-spacing: 5px; }" +
                ".timer { background-color: #fff3cd; padding: 10px; border-radius: 3px; text-align: center; color: #856404; }" +
                ".footer { text-align: center; color: #7f8c8d; font-size: 12px; padding: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>🔐 Verification Code</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello <strong>" + escapeHtml(username) + "</strong>,</p>" +
                "<p>Your password reset verification code is:</p>" +
                "<div class='otp-box'>" +
                "<div class='code'>" + otp + "</div>" +
                "</div>" +
                "<div class='timer'>" +
                "<p>⏰ This code will expire in <strong>" + expirationMinutes + " minutes</strong></p>" +
                "</div>" +
                "<p style='margin-top: 20px;'><small>If you did not request a password reset, please ignore this email or contact our support team.</small></p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 Auction System. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildPasswordChangedEmailTemplate(String username, String timestamp) {
        return "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #27ae60; color: white; padding: 20px; border-radius: 5px; text-align: center; }" +
                ".content { padding: 20px; background-color: #f9f9f9; }" +
                ".success-box { background-color: #d4edda; border-left: 4px solid #28a745; padding: 15px; border-radius: 3px; }" +
                ".footer { text-align: center; color: #7f8c8d; font-size: 12px; padding: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>✅ Password Changed Successfully</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello <strong>" + escapeHtml(username) + "</strong>,</p>" +
                "<div class='success-box'>" +
                "<p><strong>Your password was successfully changed on " + timestamp + "</strong></p>" +
                "<p>If this was not you, please contact our support team immediately.</p>" +
                "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 Auction System. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    // Escape dữ liệu user nhập để không phá cấu trúc HTML email.
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
