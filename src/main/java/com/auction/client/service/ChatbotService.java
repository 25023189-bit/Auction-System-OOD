package com.auction.client.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class ChatbotService {

    private static final String DEFAULT_RESPONSE = """
            Xin loi, toi chua co cau tra loi cho noi dung nay. Ban co the hoi ve anti-sniping, cach dat gia, tao phien dau gia, so du, quy dinh tham gia hoac truy van thong tin dau gia.
            """.strip();

    private final List<ChatbotRule> rules = List.of(
            new ChatbotRule(
                    List.of("truy van gia", "tra cuu gia", "gia hien tai", "gia cua phien", "gia san pham"),
                    "De truy van gia hien tai, ban mo phien dau gia can xem. Gia hien tai se hien thi trong phong dau gia va duoc cap nhat khi co nguoi dat gia moi."
            ),
            new ChatbotRule(
                    List.of("truy van phien", "tra cuu phien", "tim phien", "tim kiem phien", "danh sach phien"),
                    "De truy van phien dau gia, ban xem danh sach trong Main Lobby. Ban co the tim theo ten san pham, trang thai phien hoac nguoi ban neu man hinh co bo loc tuong ung."
            ),
            new ChatbotRule(
                    List.of("truy van nguoi thang", "ai thang", "nguoi thang", "ket qua dau gia", "winner"),
                    "Ket qua dau gia duoc xac dinh khi phien ket thuc. Nguoi co luot dat gia hop le cao nhat se la nguoi thang."
            ),
            new ChatbotRule(
                    List.of("truy van", "tra cuu", "tim kiem", "kiem tra thong tin", "lookup", "query"),
                    "Ban muon truy van thong tin nao? Ban co the hoi theo mau: 'truy van phien dau gia', 'truy van gia hien tai', 'truy van nguoi thang', hoac 'truy van so du'."
            ),
            new ChatbotRule(
                    List.of("anti sniping", "antisniping", "chong sniping", "gia han phien", "mo rong phien", "anti-sniping"),
                    "Anti-sniping la co che tu dong gia han thoi gian dau gia khi co nguoi dat gia vao nhung giay cuoi. Cach nay giup phien dau gia cong bang hon vi nguoi khac van co thoi gian phan hoi."
            ),
            new ChatbotRule(
                    List.of("dat gia", "bid", "dau gia nhu the nao", "tham gia dau gia"),
                    "De dat gia, ban vao mot phien dau gia dang mo, nhap so tien cao hon gia hien tai va nhan nut dat gia. He thong se kiem tra so du va dieu kien phien truoc khi ghi nhan luot dat gia."
            ),
            new ChatbotRule(
                    List.of("tao phien", "tao dau gia", "create auction", "seller"),
                    "Nguoi ban co the tao phien dau gia tu man hinh Seller Lobby bang nut Create Auction. Sau khi gui thong tin san pham, phien can duoc duyet truoc khi hien thi cho nguoi tham gia."
            ),
            new ChatbotRule(
                    List.of("so du", "balance", "nap tien", "tien", "truy van so du", "tra cuu so du"),
                    "So du dung de kiem tra kha nang tham gia va dat gia trong phien dau gia. Neu so du khong du, ban can bo sung tien truoc khi tiep tuc dat gia."
            ),
            new ChatbotRule(
                    List.of("dang nhap", "login", "tai khoan", "mat khau"),
                    "Neu khong dang nhap duoc, hay kiem tra lai ten tai khoan va mat khau. Neu quen mat khau, dung chuc nang Forgot Password o man hinh dang nhap."
            ),
            new ChatbotRule(
                    List.of("quy dinh", "luat", "dieu kien", "rule"),
                    "Nguoi tham gia can dat gia hop le, khong thap hon gia hien tai va tuan thu thoi gian cua phien. Khi phien ket thuc, luot dat gia hop le cao nhat se duoc dung de xac dinh nguoi thang."
            ),
            new ChatbotRule(
                    List.of("xin chao", "hello", "hi", "chao"),
                    "Xin chao, toi la tro ly cua he thong dau gia. Ban can ho tro ve dat gia, anti-sniping, tao phien dau gia, truy van thong tin hay so du?"
            )
    );

    public String reply(String userInput) {
        String normalizedInput = normalize(userInput);
        if (normalizedInput.isBlank()) {
            return "Ban hay nhap cau hoi can ho tro.";
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
