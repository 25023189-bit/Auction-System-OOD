import json
import re
import sys
from pathlib import Path
from typing import Any

from joblib import load


BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "chatbot_intent_model.joblib"
LABELS_PATH = BASE_DIR / "labels.json"
INPUT_PATH = BASE_DIR / "input.json"
OUTPUT_PATH = BASE_DIR / "output.json"

DEFAULT_THRESHOLD = 0.50

RESPONSE_TEMPLATES = {
    "CHECK_BALANCE": "Số dư hiện tại của bạn là {balance}đ.",
    "ASK_BALANCE_DEFINITION": (
        "Số dư là số tiền hiện có trong tài khoản của bạn, có thể dùng để đặt giá "
        "hoặc thanh toán khi thắng phiên đấu giá."
    ),
    "SEARCH_AUCTION": "Tôi sẽ tìm các phiên đấu giá phù hợp với yêu cầu của bạn.",
    "ASK_BID_GUIDE": (
        "Để đặt giá, bạn vào phiên đấu giá, nhập mức giá cao hơn giá hiện tại "
        "và xác nhận đặt giá."
    ),
    "CREATE_AUCTION_HELP": (
        "Để tạo phiên đấu giá, seller cần nhập thông tin sản phẩm, giá khởi điểm, "
        "thời gian diễn ra và gửi yêu cầu tạo phiên."
    ),
    "ASK_TIME_LEFT": "Tôi sẽ kiểm tra thời gian còn lại của phiên đấu giá hiện tại.",
    "UNKNOWN": "Tôi chưa hiểu rõ yêu cầu của bạn. Bạn có thể nói cụ thể hơn không?",
}

# Demo dữ liệu người dùng. Khi tích hợp Java/MySQL, thay bằng truy vấn thật.
FAKE_USER_BALANCES = {
    "U001": 100000,
    "U002": 250000,
}

RESPONSE_ORDER = [
    "ASK_BALANCE_DEFINITION",
    "CHECK_BALANCE",
    "SEARCH_AUCTION",
    "ASK_BID_GUIDE",
    "CREATE_AUCTION_HELP",
    "ASK_TIME_LEFT",
    "UNKNOWN",
]


def split_clauses(message: str) -> list[str]:
    """Tách câu dài thành các mệnh đề đơn giản bằng rule nhẹ."""
    parts = re.split(r"\s+(?:và|rồi|sau đó)\s+|[,;]", message, flags=re.IGNORECASE)
    return [part.strip() for part in parts if part.strip()]


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


def build_response(intents: list[str], user_id: str) -> str:
    balance = FAKE_USER_BALANCES.get(user_id, 0)
    lines = []

    # Ưu tiên giải thích khái niệm trước, rồi mới trả dữ liệu thật.
    for intent in RESPONSE_ORDER:
        if intent in intents:
            template = RESPONSE_TEMPLATES[intent]
            lines.append(template.format(balance=f"{balance:,}".replace(",", ".")))

    return "\n".join(lines)


def predict_message(model: Any, labels: list[str], user_id: str, message: str) -> dict:
    clauses = split_clauses(message)
    clause_predictions = [predict_clause(model, labels, clause) for clause in clauses]
    aggregated = aggregate_predictions(clause_predictions)
    selected_intents = select_intents(aggregated, DEFAULT_THRESHOLD)

    return {
        "user_id": user_id,
        "message": message,
        "clauses": clauses,
        "threshold": DEFAULT_THRESHOLD,
        "intent_probabilities": {
            label: round(probability, 4)
            for label, probability in sorted(aggregated.items())
        },
        "selected_intents": selected_intents,
        "response": build_response(selected_intents, user_id),
    }


def load_input(input_path: Path) -> tuple[str, str]:
    input_data = json.loads(input_path.read_text(encoding="utf-8"))
    user_id = input_data.get("user_id", "UNKNOWN_USER")
    message = input_data.get("message", "")
    return user_id, message


def main() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")

    if not MODEL_PATH.exists():
        raise FileNotFoundError(
            "Chưa có chatbot_intent_model.joblib. Hãy chạy: python train_model.py trước."
        )

    model = load(MODEL_PATH)
    labels = json.loads(LABELS_PATH.read_text(encoding="utf-8"))
    user_id, message = load_input(INPUT_PATH)

    output = predict_message(model, labels, user_id, message)

    OUTPUT_PATH.write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(output, ensure_ascii=False, indent=2))
    print(f"\nSaved output to: {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
