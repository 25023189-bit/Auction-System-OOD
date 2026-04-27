package com.auction.server.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private final String EMAIL_FROM = "your-email@gmail.com"; // TODO: Thay bằng email của bạn
    private final String EMAIL_PASSWORD = "your-app-password"; // TODO: Thay bằng app password từ Gmail

    public boolean sendPasswordResetEmail(String toEmail, String username, String newPassword) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🔑 Auction System - Password Reset");

            String content = "<!DOCTYPE html>" +
                    "<html><body style='font-family: Arial, sans-serif;'>" +
                    "<h2 style='color: #2c3e50;'>Password Reset Request</h2>" +
                    "<p>Hi <strong>" + username + "</strong>,</p>" +
                    "<p>Your new temporary password is:</p>" +
                    "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
                    "<code style='font-size: 16px; font-weight: bold; color: #e74c3c;'><b>" + newPassword + "</b></code>" +
                    "</div>" +
                    "<p><strong>⚠️ Important:</strong> Please change this password immediately after logging in.</p>" +
                    "<p style='color: #7f8c8d; font-size: 12px;'>This is an automated email. Please do not reply.</p>" +
                    "</body></html>";

            message.setContent(content, "text/html; charset=UTF-8");
            Transport.send(message);

            System.out.println("✅ Email sent successfully to: " + toEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
