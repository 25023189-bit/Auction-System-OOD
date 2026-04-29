import json
import re
import unicodedata
from pathlib import Path
from typing import Any

from joblib import load

from .paths import INPUT_PATH, LABELS_PATH, MODEL_PATH, OUTPUT_PATH


DEFAULT_THRESHOLD = 0.50

RESPONSE_TEMPLATES = {
    "HELLO": "Xin chào, tôi có thể giải thích và hướng dẫn bạn sử dụng hệ thống đấu giá.",
    "OK": "Tôi đã ghi nhận. Bạn có thể tiếp tục hỏi về thao tác cần được hướng dẫn.",
    "COMPLIMENT": "Cảm ơn bạn. Tôi sẽ tiếp tục hỗ trợ các câu hỏi về hệ thống đấu giá.",
    "CONNECT_TO_ADMIN": (
        "Nếu bạn cần tư vấn viên hỗ trợ trực tiếp, bạn có thể gọi đến số 038xxxxxxx."
    ),
    "EXPLAIN_BALANCE": (
        "Số dư là số tiền hiện có trong tài khoản của bạn. Trong hệ thống đấu giá, "
        "số dư giúp bạn biết mình có đủ điều kiện tài chính để tham gia đặt giá "
        "hoặc thanh toán khi thắng phiên."
    ),
    "GUIDE_SEARCH_AUCTION": (
        "Để tìm phiên đấu giá, bạn mở danh sách phiên đang hoạt động, nhập từ khóa "
        "sản phẩm hoặc chọn danh mục phù hợp, sau đó xem thông tin chi tiết của "
        "phiên còn hiệu lực."
    ),
    "GUIDE_BIDDING": (
        "Để đặt giá, bạn vào phiên đấu giá đang chạy, nhập mức giá cao hơn giá "
        "hiện tại và xác nhận đặt giá. Nếu giá hợp lệ, hệ thống sẽ ghi nhận bạn "
        "là người đang giữ mức giá cao nhất."
    ),
    "GUIDE_CREATE_AUCTION": (
        "Để tạo phiên đấu giá, seller nhập thông tin sản phẩm, mô tả, giá khởi điểm, "
        "bước giá, thời gian diễn ra và gửi yêu cầu tạo phiên để hệ thống xử lý."
    ),
    "GUIDE_CHECK_TIME_LEFT": (
        "Để xem thời gian còn lại, bạn mở phiên đấu giá đang chạy và kiểm tra phần "
        "đếm ngược hoặc thời gian kết thúc hiển thị trong chi tiết phiên."
    ),
    "GUIDE_ANTI_SNIPING": (
        "Anti-sniping là cơ chế tự động gia hạn phiên khi có lượt đặt giá ở những "
        "giây cuối. Cơ chế này giúp người tham gia khác có thêm thời gian phản hồi "
        "và làm phiên đấu giá công bằng hơn."
    ),
    "UNKNOWN": (
        "Tôi chưa hiểu rõ yêu cầu của bạn. Bạn có thể hỏi về cách tìm phiên, đặt giá, "
        "tạo phiên, xem thời gian còn lại, anti-sniping hoặc khái niệm số dư."
    ),
}

RESPONSE_ORDER = [
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

KEYWORD_INTENT_HINTS = {
    "HELLO": [
        "xin chao",
        "chao ban",
        "hello",
        "hi",
        "alo",
        "bat dau tro chuyen",
    ],
    "OK": [
        "ok",
        "duoc roi",
        "hieu roi",
        "toi da ro",
        "dong y",
        "on roi",
        "vay la duoc",
    ],
    "COMPLIMENT": [
        "cam on",
        "tra loi tot",
        "huu ich",
        "de hieu",
        "thong minh",
        "huong dan ro",
        "ho tro nhanh",
    ],
    "CONNECT_TO_ADMIN": [
        "tu van vien",
        "ket noi admin",
        "nhan vien ho tro",
        "nguoi that",
        "so dien thoai ho tro",
        "lien he admin",
        "cham soc khach hang",
    ],
    "EXPLAIN_BALANCE": [
        "so du la gi",
        "giai thich so du",
        "khai niem so du",
        "y nghia cua so du",
        "so du dung de lam gi",
        "vai tro cua so du",
    ],
    "GUIDE_SEARCH_AUCTION": [
        "tim phien",
        "danh sach dau gia",
        "tim san pham",
        "loc phien",
        "phong dau gia",
        "phien dang hoat dong",
    ],
    "GUIDE_BIDDING": [
        "dat gia",
        "bid",
        "tham gia dau gia",
        "dau gia san pham",
        "gia hop le",
    ],
    "GUIDE_CREATE_AUCTION": [
        "tao phien",
        "dang san pham",
        "mo phong dau gia",
        "seller",
        "tao dau gia",
        "gui yeu cau tao phien",
    ],
    "GUIDE_CHECK_TIME_LEFT": [
        "thoi gian con lai",
        "con bao lau",
        "phien ket thuc",
        "countdown",
        "dem nguoc",
        "sap het gio",
    ],
    "GUIDE_ANTI_SNIPING": [
        "anti sniping",
        "chong sniping",
        "gia han phien",
        "tu keo dai",
        "phut cuoi",
        "thoi gian dau gia tang",
    ],
}


def split_clauses(message: str) -> list[str]:
    """Tách câu dài thành các mệnh đề đơn giản bằng rule nhẹ."""
    parts = re.split(
        r"\s+(?:và|rồi|sau đó|đồng thời)\s+|[,;]",
        message,
        flags=re.IGNORECASE,
    )
    return [part.strip() for part in parts if part.strip()]


def normalize_text(message: str) -> str:
    standardized = message.replace("đ", "d").replace("Đ", "D")
    without_diacritics = unicodedata.normalize("NFD", standardized)
    without_diacritics = "".join(
        character for character in without_diacritics
        if unicodedata.category(character) != "Mn"
    )
    return re.sub(r"\s+", " ", without_diacritics.lower()).strip()


def detect_keyword_intents(message: str) -> list[str]:
    normalized_message = normalize_text(message)
    detected = []

    for intent in RESPONSE_ORDER:
        keywords = KEYWORD_INTENT_HINTS.get(intent, [])
        if any(has_keyword(normalized_message, keyword) for keyword in keywords):
            detected.append(intent)

    return detected


def has_keyword(normalized_message: str, keyword: str) -> bool:
    pattern = rf"(?<![a-z0-9]){re.escape(keyword)}(?![a-z0-9])"
    return re.search(pattern, normalized_message) is not None


def load_chatbot_model(model_path: Path = MODEL_PATH):
    if not model_path.exists():
        raise FileNotFoundError(
            f"Chưa có model tại {model_path}. Hãy chạy: python Auction_AI\\ChatBot\\training\\train_model.py trước."
        )
    return load(model_path)


def load_labels(labels_path: Path = LABELS_PATH) -> list[str]:
    return json.loads(labels_path.read_text(encoding="utf-8"))


def predict_clause(model: Any, labels: list[str], clause: str) -> dict[str, float]:
    probabilities = model.predict_proba([clause])[0]
    return {label: float(probability) for label, probability in zip(labels, probabilities)}


def aggregate_predictions(predictions: list[dict[str, float]]) -> dict[str, float]:
    """Nếu một intent xuất hiện ở nhiều mệnh đề, lấy xác suất cao nhất."""
    result: dict[str, float] = {}
    for prediction in predictions:
        for label, probability in prediction.items():
            result[label] = max(result.get(label, 0.0), probability)
    return result


def select_intents(probabilities: dict[str, float], threshold: float) -> list[str]:
    selected = [
        label
        for label, probability in probabilities.items()
        if probability >= threshold and label != "UNKNOWN"
    ]
    return selected or ["UNKNOWN"]


def build_response(intents: list[str]) -> str:
    lines = []

    for intent in RESPONSE_ORDER:
        if intent in intents:
            lines.append(RESPONSE_TEMPLATES[intent])

    return "\n".join(lines)


def predict_message(
    message: str,
    model: Any | None = None,
    labels: list[str] | None = None,
    threshold: float = DEFAULT_THRESHOLD,
) -> str:
    model = model or load_chatbot_model()
    labels = labels or load_labels()

    clauses = split_clauses(message)
    clause_predictions = [predict_clause(model, labels, clause) for clause in clauses]
    aggregated = aggregate_predictions(clause_predictions)
    selected_intents = select_intents(aggregated, threshold)
    keyword_intents = detect_keyword_intents(message)

    if keyword_intents:
        selected_intents = [
            intent for intent in selected_intents
            if intent != "UNKNOWN"
        ]
        selected_intents.extend(
            intent for intent in keyword_intents
            if intent not in selected_intents
        )
        selected_intents = selected_intents or ["UNKNOWN"]

    return build_response(selected_intents)


def load_message(input_path: Path = INPUT_PATH) -> str:
    input_data = json.loads(input_path.read_text(encoding="utf-8"))
    return input_data.get("message", "")


def predict_to_file(
    input_path: Path = INPUT_PATH,
    output_path: Path = OUTPUT_PATH,
    model_path: Path = MODEL_PATH,
    labels_path: Path = LABELS_PATH,
) -> str:
    model = load_chatbot_model(model_path)
    labels = load_labels(labels_path)
    message = load_message(input_path)
    answer = predict_message(message, model=model, labels=labels)

    output_path.write_text(
        json.dumps({"response": answer}, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    return answer
