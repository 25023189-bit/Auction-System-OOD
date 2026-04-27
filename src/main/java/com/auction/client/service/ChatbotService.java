package com.auction.client.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class ChatbotService {

    public enum InputLanguageState {
        ENGLISH,
        VIETNAMESE_UNSIGNED,
        VIETNAMESE_SIGNED
    }

    private static final String DEFAULT_RESPONSE = """
            Xin lỗi, tôi chưa có câu trả lời cho nội dung này. Bạn có thể hỏi về anti-sniping, cách đặt giá, tạo phiên đấu giá, số dư, quy định tham gia hoặc truy vấn thông tin đấu giá.
            """.strip();

    private static final List<String> ENGLISH_INDICATORS = List.of(
            "what", "how", "auction", "bid", "balance", "account", "login", "password",
            "room", "product", "price", "winner", "query", "lookup", "search", "seller"
    );

    private static final List<String> VIETNAMESE_UNSIGNED_INDICATORS = List.of(
            "la gi", "nhu the nao", "dau gia", "dat gia", "so du", "tai khoan", "mat khau",
            "phien", "san pham", "gia", "nguoi thang", "truy van", "tra cuu", "tim kiem"
    );

    private final List<ChatbotRule> rules = List.of(
            new ChatbotRule(
                    List.of("truy van gia", "tra cuu gia", "gia hien tai", "gia cua phien", "gia san pham",
                            "current price", "product price", "auction price"),
                    "Để truy vấn giá hiện tại, bạn mở phiên đấu giá cần xem. Giá hiện tại sẽ hiển thị trong phòng đấu giá và được cập nhật khi có người đặt giá mới."
            ),
            new ChatbotRule(
                    List.of("truy van phien", "tra cuu phien", "tim phien", "tim kiem phien", "danh sach phien",
                            "auction list", "find auction", "search auction", "active auctions"),
                    "Để truy vấn phiên đấu giá, bạn xem danh sách trong Main Lobby. Bạn có thể tìm theo tên sản phẩm, trạng thái phiên hoặc người bán nếu màn hình có bộ lọc tương ứng."
            ),
            new ChatbotRule(
                    List.of("truy van nguoi thang", "ai thang", "nguoi thang", "ket qua dau gia", "winner", "auction result"),
                    "Kết quả đấu giá được xác định khi phiên kết thúc. Người có lượt đặt giá hợp lệ cao nhất sẽ là người thắng."
            ),
            new ChatbotRule(
                    List.of("truy van", "tra cuu", "tim kiem", "kiem tra thong tin", "lookup", "query", "search"),
                    "Bạn muốn truy vấn thông tin nào? Bạn có thể hỏi theo mẫu: 'truy vấn phiên đấu giá', 'truy vấn giá hiện tại', 'truy vấn người thắng', hoặc 'truy vấn số dư'."
            ),
            new ChatbotRule(
                    List.of("anti sniping", "antisniping", "chong sniping", "gia han phien", "mo rong phien", "anti-sniping"),
                    "Anti-sniping là cơ chế tự động gia hạn thời gian đấu giá khi có người đặt giá vào những giây cuối. Cách này giúp phiên đấu giá công bằng hơn vì người khác vẫn có thời gian phản hồi."
            ),
            new ChatbotRule(
                    List.of("dat gia", "bid", "place bid", "dau gia nhu the nao", "tham gia dau gia", "join auction"),
                    "Để đặt giá, bạn vào một phiên đấu giá đang mở, nhập số tiền cao hơn giá hiện tại và nhấn nút đặt giá. Hệ thống sẽ kiểm tra số dư và điều kiện phiên trước khi ghi nhận lượt đặt giá."
            ),
            new ChatbotRule(
                    List.of("tao phien", "tao dau gia", "create auction", "seller", "create auction room"),
                    "Người bán có thể tạo phiên đấu giá từ màn hình Seller Lobby bằng nút Create Auction. Sau khi gửi thông tin sản phẩm, phiên cần được duyệt trước khi hiển thị cho người tham gia."
            ),
            new ChatbotRule(
                    List.of("so du", "balance", "account balance", "nap tien", "tien", "truy van so du", "tra cuu so du"),
                    "Số dư dùng để kiểm tra khả năng tham gia và đặt giá trong phiên đấu giá. Nếu số dư không đủ, bạn cần bổ sung tiền trước khi tiếp tục đặt giá."
            ),
            new ChatbotRule(
                    List.of("dang nhap", "login", "tai khoan", "account", "mat khau", "password"),
                    "Nếu không đăng nhập được, hãy kiểm tra lại tên tài khoản và mật khẩu. Nếu quên mật khẩu, dùng chức năng Forgot Password ở màn hình đăng nhập."
            ),
            new ChatbotRule(
                    List.of("quy dinh", "luat", "dieu kien", "rule", "policy", "condition"),
                    "Người tham gia cần đặt giá hợp lệ, không thấp hơn giá hiện tại và tuân thủ thời gian của phiên. Khi phiên kết thúc, lượt đặt giá hợp lệ cao nhất sẽ được dùng để xác định người thắng."
            ),
            new ChatbotRule(
                    List.of("xin chao", "hello", "hi", "chao"),
                    "Xin chào, tôi là trợ lý của hệ thống đấu giá. Bạn cần hỗ trợ về đặt giá, anti-sniping, tạo phiên đấu giá, truy vấn thông tin hay số dư?"
            )
    );

    public String reply(String userInput) {
        String normalizedInput = normalizeByInputState(userInput);
        if (normalizedInput.isBlank()) {
            return "Bạn hãy nhập câu hỏi cần hỗ trợ.";
        }

        return rules.stream()
                .filter(rule -> rule.matches(normalizedInput))
                .findFirst()
                .map(ChatbotRule::response)
                .orElse(DEFAULT_RESPONSE);
    }

    public InputLanguageState analyzeInputState(String value) {
        if (value == null || value.isBlank()) {
            return InputLanguageState.VIETNAMESE_UNSIGNED;
        }

        if (hasVietnameseDiacritics(value)) {
            return InputLanguageState.VIETNAMESE_SIGNED;
        }

        String normalized = normalizeForSearch(value);
        boolean hasEnglishSignal = ENGLISH_INDICATORS.stream().anyMatch(normalized::contains);
        boolean hasVietnameseSignal = VIETNAMESE_UNSIGNED_INDICATORS.stream().anyMatch(normalized::contains);

        if (hasEnglishSignal && !hasVietnameseSignal) {
            return InputLanguageState.ENGLISH;
        }
        return InputLanguageState.VIETNAMESE_UNSIGNED;
    }

    public String normalizeByInputState(String value) {
        return switch (analyzeInputState(value)) {
            case ENGLISH, VIETNAMESE_UNSIGNED, VIETNAMESE_SIGNED -> normalizeForSearch(value);
        };
    }

    public static String normalizeForSearch(String value) {
        if (value == null) {
            return "";
        }

        String standardized = value
                .replace('đ', 'd')
                .replace('Đ', 'D');

        String withoutDiacritics = Normalizer.normalize(standardized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return withoutDiacritics
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean hasVietnameseDiacritics(String value) {
        return value.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ].*");
    }

    private record ChatbotRule(List<String> keywords, String response) {
        private boolean matches(String input) {
            return keywords.stream()
                    .map(ChatbotService::normalizeForSearch)
                    .anyMatch(input::contains);
        }
    }
}
