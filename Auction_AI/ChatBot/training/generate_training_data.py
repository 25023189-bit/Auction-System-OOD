import csv
import random
import unicodedata
from pathlib import Path


RANDOM_SEED = 20260429
TARGET_SINGLE_ROWS_PER_LABEL = 90
TARGET_MULTI_ROWS = 260

CHATBOT_DIR = Path(__file__).resolve().parents[1]
TRAIN_DATA_PATH = CHATBOT_DIR / "data" / "train_data.csv"

LABELS = [
    "HELLO",
    "OK",
    "COMPLIMENT",
    "CONNECT_TO_ADMIN",
    "EXPLAIN_BALANCE",
    "GUIDE_SEARCH_AUCTION",
    "GUIDE_BIDDING",
    "GUIDE_CREATE_AUCTION",
    "GUIDE_CHECK_TIME_LEFT",
    "GUIDE_ANTI_SNIPING",
    "UNKNOWN",
]

PHRASES = {
    "HELLO": [
        "xin chào",
        "chào bạn",
        "hello",
        "hi trợ lý",
        "alo chatbot",
        "chào hệ thống",
        "chào buổi sáng",
        "chào buổi chiều",
        "hey assistant",
        "mình chào bạn",
        "có ai hỗ trợ không",
        "bắt đầu tư vấn giúp tôi",
        "tôi mới vào hệ thống",
        "bạn đang online không",
        "cho tôi hỏi một chút",
    ],
    "OK": [
        "ok",
        "được rồi",
        "hiểu rồi",
        "tôi đã rõ",
        "ổn rồi",
        "vậy là được",
        "đồng ý",
        "rõ rồi",
        "đã hiểu",
        "tạm ổn",
        "không cần giải thích thêm",
        "để tôi thử",
        "tôi ghi nhận",
        "phần này đã rõ",
        "câu trả lời đủ rồi",
    ],
    "COMPLIMENT": [
        "bạn trả lời tốt",
        "trợ lý hữu ích quá",
        "cảm ơn bạn nhiều",
        "câu trả lời rất dễ hiểu",
        "chatbot thông minh",
        "hỗ trợ nhanh thật",
        "bạn hướng dẫn rõ ràng",
        "phản hồi rất ổn",
        "giải thích dễ hiểu",
        "dịch vụ hỗ trợ tốt",
        "thông tin này giúp ích nhiều",
        "tôi hài lòng",
        "bạn làm tốt lắm",
        "câu trả lời đúng ý tôi",
        "trải nghiệm khá tốt",
    ],
    "CONNECT_TO_ADMIN": [
        "tôi muốn gặp tư vấn viên",
        "kết nối admin giúp tôi",
        "gọi nhân viên hỗ trợ",
        "tôi cần nói chuyện với người thật",
        "chuyển tôi đến tư vấn viên",
        "cho tôi số điện thoại hỗ trợ",
        "cần liên hệ admin",
        "gặp bộ phận chăm sóc khách hàng",
        "tôi muốn gọi tổng đài",
        "liên hệ người phụ trách giúp tôi",
        "chatbot chưa giải quyết được",
        "cần hỗ trợ trực tiếp",
        "tôi cần người xử lý",
        "cho tôi gặp bộ phận hỗ trợ",
        "admin có thể hỗ trợ không",
    ],
    "EXPLAIN_BALANCE": [
        "số dư là gì",
        "giải thích số dư tài khoản",
        "số dư dùng để làm gì",
        "ý nghĩa của số dư trong đấu giá",
        "số dư ảnh hưởng gì khi đặt giá",
        "tại sao cần có số dư",
        "số dư trong hệ thống được hiểu như thế nào",
        "số dư có vai trò gì khi thắng phiên",
        "cho tôi biết khái niệm số dư",
        "balance trong tài khoản nghĩa là gì",
        "số tiền còn lại trong ví là gì",
        "vì sao phải kiểm tra số dư",
        "số dư liên quan gì tới thanh toán",
        "số dư có phải tiền khả dụng không",
        "khi nào số dư bị trừ",
    ],
    "GUIDE_SEARCH_AUCTION": [
        "tìm phiên đấu giá",
        "hướng dẫn tìm phiên đang mở",
        "làm sao xem danh sách đấu giá",
        "tôi muốn tìm sản phẩm đang đấu giá",
        "cách lọc phiên đấu giá theo danh mục",
        "xem các phiên còn hiệu lực",
        "hướng dẫn tìm phòng đấu giá",
        "làm sao kiểm tra phiên đang hoạt động",
        "tìm auction room",
        "search phiên đấu giá",
        "tìm phiên bán laptop",
        "xem phiên sắp kết thúc",
        "tìm phiên theo tên sản phẩm",
        "lobby có những phiên nào",
        "lọc sản phẩm đang đấu giá",
    ],
    "GUIDE_BIDDING": [
        "làm sao để đặt giá",
        "hướng dẫn tôi bid",
        "cách tham gia đặt giá",
        "đặt giá như thế nào",
        "tôi muốn đấu giá sản phẩm",
        "cần nhập gì khi đặt giá",
        "giá hợp lệ là gì khi bid",
        "làm sao biết đặt giá thành công",
        "hướng dẫn bidder tham gia phiên",
        "place bid như thế nào",
        "muốn giữ giá cao nhất thì làm sao",
        "tôi cần tăng giá hiện tại",
        "bid bị từ chối thì kiểm tra gì",
        "quy trình đặt giá trong phòng",
        "bấm nút nào để gửi giá",
    ],
    "GUIDE_CREATE_AUCTION": [
        "làm sao tạo phiên đấu giá",
        "seller muốn đăng sản phẩm",
        "hướng dẫn tạo phiên bán hàng",
        "cách mở phòng đấu giá mới",
        "cần thông tin gì để tạo phiên",
        "seller tạo đấu giá như thế nào",
        "làm sao gửi yêu cầu tạo phiên",
        "tạo phiên cho sản phẩm mới",
        "hướng dẫn đăng sản phẩm đấu giá",
        "create auction room thế nào",
        "nhập giá khởi điểm ở đâu",
        "cần mô tả sản phẩm ra sao",
        "thiết lập bước giá như thế nào",
        "seller lobby tạo phiên ở đâu",
        "gửi phiên để admin duyệt",
    ],
    "GUIDE_CHECK_TIME_LEFT": [
        "xem thời gian còn lại",
        "phiên này còn bao lâu",
        "khi nào phiên kết thúc",
        "hướng dẫn xem countdown",
        "kiểm tra giờ kết thúc phiên",
        "thời gian đấu giá còn bao nhiêu",
        "xem đồng hồ đếm ngược ở đâu",
        "làm sao biết phiên sắp hết giờ",
        "kiểm tra time left",
        "xem thời hạn phiên đấu giá",
        "còn mấy phút nữa kết thúc",
        "đếm ngược nằm ở chỗ nào",
        "phiên gần hết giờ chưa",
        "xem deadline của phiên",
        "auction còn bao lâu",
    ],
    "GUIDE_ANTI_SNIPING": [
        "anti sniping là gì",
        "giải thích chống sniping",
        "cơ chế gia hạn phiên cuối giờ",
        "tại sao phiên tự kéo dài",
        "đặt giá phút cuối có gia hạn không",
        "anti sniping hoạt động thế nào",
        "hướng dẫn về chống đặt giá phút cuối",
        "vì sao thời gian đấu giá tăng thêm",
        "auto extend trong đấu giá là gì",
        "cơ chế chống cướp giá cuối giờ",
        "bid sát giờ có kéo dài phiên không",
        "gia hạn tự động khi nào xảy ra",
        "sniping trong đấu giá nghĩa là gì",
        "luật chống đặt giá phút cuối",
        "vì sao countdown nhảy thêm thời gian",
    ],
    "UNKNOWN": [
        "hôm nay trời đẹp",
        "kể chuyện cười đi",
        "bạn thích màu gì",
        "thời tiết ngày mai sao",
        "tôi muốn nghe nhạc",
        "abc xyz không hiểu",
        "nấu phở như thế nào",
        "phim nào hay",
        "dịch câu này sang tiếng Anh",
        "mở bản đồ giúp tôi",
        "mua vé xem phim ở đâu",
        "tính giúp tôi căn bậc hai",
        "đặt báo thức lúc 7 giờ",
        "viết bài thơ về biển",
        "đổi mật khẩu wifi thế nào",
    ],
}

CONTEXTS = [
    "",
    "giúp tôi",
    "nhé",
    "với ạ",
    "trong hệ thống",
    "trên màn hình hiện tại",
    "khi tôi đang dùng app",
    "cho người mới",
    "nói ngắn gọn thôi",
    "tôi hơi chưa rõ",
]

TYPO_REPLACEMENTS = [
    ("hướng dẫn", "hd"),
    ("đấu giá", "dgia"),
    ("đặt giá", "dat gia"),
    ("phiên", "phien"),
    ("sản phẩm", "sp"),
    ("tài khoản", "tk"),
    ("tư vấn viên", "tvv"),
    ("nhân viên hỗ trợ", "nv ho tro"),
    ("thời gian", "tg"),
    ("kết thúc", "ket thuc"),
    ("chống", "chong"),
    ("giải thích", "gthich"),
    ("admin", "ad"),
]

NOISY_PREFIXES = [
    "ờ",
    "ê",
    "cho hỏi",
    "mình hỏi ngu tí",
    "không rõ lắm",
    "hơi gấp",
    "pls",
    "help",
    "này",
    "ad ơi",
]

NOISY_SUFFIXES = [
    "???",
    "ạ",
    "nha",
    "plz",
    "đc ko",
    "với",
    "gấp",
    "...",
    "huhu",
    "thanks",
]

MULTI_LABEL_PAIRS = [
    ("HELLO", "EXPLAIN_BALANCE"),
    ("HELLO", "GUIDE_SEARCH_AUCTION"),
    ("HELLO", "GUIDE_BIDDING"),
    ("HELLO", "CONNECT_TO_ADMIN"),
    ("OK", "GUIDE_BIDDING"),
    ("OK", "GUIDE_CREATE_AUCTION"),
    ("COMPLIMENT", "GUIDE_ANTI_SNIPING"),
    ("COMPLIMENT", "CONNECT_TO_ADMIN"),
    ("EXPLAIN_BALANCE", "GUIDE_BIDDING"),
    ("EXPLAIN_BALANCE", "GUIDE_SEARCH_AUCTION"),
    ("GUIDE_SEARCH_AUCTION", "GUIDE_BIDDING"),
    ("GUIDE_SEARCH_AUCTION", "GUIDE_CHECK_TIME_LEFT"),
    ("GUIDE_BIDDING", "GUIDE_CHECK_TIME_LEFT"),
    ("GUIDE_CREATE_AUCTION", "GUIDE_ANTI_SNIPING"),
    ("GUIDE_CREATE_AUCTION", "GUIDE_CHECK_TIME_LEFT"),
    ("GUIDE_ANTI_SNIPING", "GUIDE_CHECK_TIME_LEFT"),
    ("CONNECT_TO_ADMIN", "GUIDE_BIDDING"),
    ("CONNECT_TO_ADMIN", "GUIDE_CREATE_AUCTION"),
    ("COMPLIMENT", "GUIDE_BIDDING"),
    ("HELLO", "GUIDE_ANTI_SNIPING"),
]

AMBIGUOUS_NOISE_ROWS = [
    ("ok ad ơi chỉ tôi cái này", {"OK"}),
    ("ad ơi hệ thống hơi khó dùng", {"CONNECT_TO_ADMIN"}),
    ("cái số tiền trong tk là sao", {"EXPLAIN_BALANCE"}),
    ("phien nào còn chạy vậy", {"GUIDE_SEARCH_AUCTION"}),
    ("muốn vào trả giá thì bấm đâu", {"GUIDE_BIDDING"}),
    ("seller muốn up sp lên bán", {"GUIDE_CREATE_AUCTION"}),
    ("còn mấy phút nữa vậy", {"GUIDE_CHECK_TIME_LEFT"}),
    ("sao tự nhiên cộng thêm giờ", {"GUIDE_ANTI_SNIPING"}),
    ("cảm ơn nha mà anti sniping là sao", {"COMPLIMENT", "GUIDE_ANTI_SNIPING"}),
    ("xin chao tim phien giup minh", {"HELLO", "GUIDE_SEARCH_AUCTION"}),
    ("toi muon gap nguoi that de hoi dat gia", {"CONNECT_TO_ADMIN", "GUIDE_BIDDING"}),
    ("hướng dn tao phien voi", {"GUIDE_CREATE_AUCTION"}),
    ("bid sao z", {"GUIDE_BIDDING"}),
    ("so du la j", {"EXPLAIN_BALANCE"}),
    ("anti snip la j vay", {"GUIDE_ANTI_SNIPING"}),
    ("phien con bn lau", {"GUIDE_CHECK_TIME_LEFT"}),
    ("tim sp dang dgia", {"GUIDE_SEARCH_AUCTION"}),
    ("cam on bot nha", {"COMPLIMENT"}),
    ("ừ ok hiểu sơ sơ", {"OK"}),
    ("blabla test test", {"UNKNOWN"}),
]


def strip_diacritics(text: str) -> str:
    standardized = text.replace("đ", "d").replace("Đ", "D")
    decomposed = unicodedata.normalize("NFD", standardized)
    return "".join(character for character in decomposed if unicodedata.category(character) != "Mn")


def drop_random_character(text: str, rng: random.Random) -> str:
    candidates = [index for index, character in enumerate(text) if character.isalpha()]
    if not candidates:
        return text
    index = rng.choice(candidates)
    return text[:index] + text[index + 1:]


def swap_adjacent_characters(text: str, rng: random.Random) -> str:
    candidates = [
        index for index in range(len(text) - 1)
        if text[index].isalpha() and text[index + 1].isalpha()
    ]
    if not candidates:
        return text
    index = rng.choice(candidates)
    chars = list(text)
    chars[index], chars[index + 1] = chars[index + 1], chars[index]
    return "".join(chars)


def duplicate_random_character(text: str, rng: random.Random) -> str:
    candidates = [index for index, character in enumerate(text) if character.isalpha()]
    if not candidates:
        return text
    index = rng.choice(candidates)
    return text[:index] + text[index] + text[index:]


def apply_abbreviations(text: str, rng: random.Random) -> str:
    result = text
    replacements = TYPO_REPLACEMENTS[:]
    rng.shuffle(replacements)
    for source, replacement in replacements[:3]:
        result = result.replace(source, replacement)
    return result


def add_noise(text: str, rng: random.Random, level: int) -> str:
    result = text

    if level >= 1 and rng.random() < 0.75:
        result = apply_abbreviations(result, rng)
    if level >= 2 and rng.random() < 0.65:
        result = strip_diacritics(result)
    if level >= 2 and rng.random() < 0.45:
        result = f"{rng.choice(NOISY_PREFIXES)} {result}"
    if level >= 2 and rng.random() < 0.45:
        result = f"{result} {rng.choice(NOISY_SUFFIXES)}"
    if level >= 3:
        operations = [drop_random_character, swap_adjacent_characters, duplicate_random_character]
        for operation in rng.sample(operations, k=2):
            result = operation(result, rng)

    return " ".join(result.split())


def make_row(text: str, active_labels: set[str]) -> dict[str, str | int]:
    row: dict[str, str | int] = {"text": text}
    for label in LABELS:
        row[label] = 1 if label in active_labels else 0
    return row


def add_row(rows: list[dict[str, str | int]], seen: set[str], text: str, labels: set[str]) -> None:
    normalized = " ".join(text.split()).strip()
    if not normalized or normalized in seen:
        return
    seen.add(normalized)
    rows.append(make_row(normalized, labels))


def build_single_label_rows(rows: list[dict[str, str | int]], seen: set[str], rng: random.Random) -> None:
    for label in LABELS:
        while sum(1 for row in rows if row[label] == 1 and sum(row[item] == 1 for item in LABELS) == 1) < TARGET_SINGLE_ROWS_PER_LABEL:
            phrase = rng.choice(PHRASES[label])
            context = rng.choice(CONTEXTS)
            text = f"{phrase} {context}".strip()

            sample_index = len(rows)
            if sample_index % 10 < 4:
                level = 0
            elif sample_index % 10 < 7:
                level = 1
            elif sample_index % 10 < 9:
                level = 2
            else:
                level = 3

            add_row(rows, seen, add_noise(text, rng, level), {label})


def build_multi_label_rows(rows: list[dict[str, str | int]], seen: set[str], rng: random.Random) -> None:
    connectors = ["và", "rồi", "sau đó", "đồng thời", "xong rồi", "kèm theo"]

    while sum(1 for row in rows if sum(row[item] == 1 for item in LABELS) > 1) < TARGET_MULTI_ROWS:
        left_label, right_label = rng.choice(MULTI_LABEL_PAIRS)
        left = rng.choice(PHRASES[left_label])
        right = rng.choice(PHRASES[right_label])
        connector = rng.choice(connectors)
        text = f"{left} {connector} {right}"
        level = rng.choices([0, 1, 2, 3], weights=[30, 35, 25, 10], k=1)[0]
        add_row(rows, seen, add_noise(text, rng, level), {left_label, right_label})


def add_ambiguous_noise(rows: list[dict[str, str | int]], seen: set[str]) -> None:
    for text, labels in AMBIGUOUS_NOISE_ROWS:
        add_row(rows, seen, text, labels)


def write_training_data() -> None:
    rng = random.Random(RANDOM_SEED)
    rows: list[dict[str, str | int]] = []
    seen: set[str] = set()

    build_single_label_rows(rows, seen, rng)
    build_multi_label_rows(rows, seen, rng)
    add_ambiguous_noise(rows, seen)

    rng.shuffle(rows)

    TRAIN_DATA_PATH.parent.mkdir(parents=True, exist_ok=True)
    with TRAIN_DATA_PATH.open("w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=["text", *LABELS])
        writer.writeheader()
        writer.writerows(rows)

    print(f"Generated {len(rows)} rows at {TRAIN_DATA_PATH}")


if __name__ == "__main__":
    write_training_data()
