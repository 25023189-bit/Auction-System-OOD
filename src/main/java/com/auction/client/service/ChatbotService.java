package com.auction.client.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class ChatbotService {

    private static final String DEFAULT_RESPONSE = """
            Xin lỗi, tôi chưa có câu trả lời cho nội dung này. Bạn có thể hỏi về anti-sniping, cách đặt giá, tạo phiên đấu giá, số dư hoặc quy định tham gia đấu giá.
            """.strip();

    private final List<ChatbotRule> rules = List.of(
            new ChatbotRule(
                    List.of("anti sniping", "antisniping", "chong sniping", "gia han phien", "mo rong phien", "anti-sniping"),
                    "Anti-sniping là cơ chế tự động gia hạn thời gian đấu giá khi có người đặt giá vào những giây cuối. Cách này giúp phiên đấu giá công bằng hơn vì người khác vẫn có thời gian phản hồi."
            ),
            new ChatbotRule(
                    List.of("dat gia", "bid", "dau gia nhu the nao", "tham gia dau gia"),
                    "Để đặt giá, bạn vào một phiên đấu giá đang mở, nhập số tiền cao hơn giá hiện tại và nhấn nút đặt giá. Hệ thống sẽ kiểm tra số dư và điều kiện phiên trước khi ghi nhận lượt đặt giá."
            ),
            new ChatbotRule(
                    List.of("tao phien", "tao dau gia", "create auction", "seller"),
                    "Người bán có thể tạo phiên đấu giá từ màn hình Seller Lobby bằng nút Create Auction. Sau khi gửi thông tin sản phẩm, phiên cần được duyệt trước khi hiển thị cho người tham gia."
            ),
            new ChatbotRule(
                    List.of("so du", "balance", "nap tien", "tien"),
                    "Số dư dùng để kiểm tra khả năng tham gia và đặt giá trong phiên đấu giá. Nếu số dư không đủ, bạn cần bổ sung tiền trước khi tiếp tục đặt giá."
            ),
            new ChatbotRule(
                    List.of("dang nhap", "login", "tai khoan", "mat khau"),
                    "Nếu không đăng nhập được, hãy kiểm tra lại tên tài khoản và mật khẩu. Nếu quên mật khẩu, dùng chức năng Forgot Password ở màn hình đăng nhập."
            ),
            new ChatbotRule(
                    List.of("quy dinh", "luat", "dieu kien", "rule"),
                    "Người tham gia cần đặt giá hợp lệ, không thấp hơn giá hiện tại và tuân thủ thời gian của phiên. Khi phiên kết thúc, lượt đặt giá hợp lệ cao nhất sẽ được dùng để xác định người thắng."
            ),
            new ChatbotRule(
                    List.of("xin chao", "hello", "hi", "chao"),
                    "Xin chào, tôi là trợ lý của hệ thống đấu giá. Bạn cần hỗ trợ về đặt giá, anti-sniping, tạo phiên đấu giá hay số dư?"
            )
    );

    public String reply(String userInput) {
        String normalizedInput = normalize(userInput);
        if (normalizedInput.isBlank()) {
            return "Bạn hãy nhập câu hỏi cần hỗ trợ.";
        }

        return rules.stream()
                .filter(rule -> rule.matches(normalizedInput))
                .findFirst()
                .map(ChatbotRule::response)
                .orElse(DEFAULT_RESPONSE);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String withoutDiacritics = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');

        return withoutDiacritics
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record ChatbotRule(List<String> keywords, String response) {
        private boolean matches(String input) {
            return keywords.stream().anyMatch(input::contains);
        }
    }
}
