import csv
import json
from pathlib import Path


LOGIST_DIR = Path(__file__).resolve().parents[1]
CHATBOT_DIR = LOGIST_DIR.parent
TRAIN_DATA_PATH = LOGIST_DIR / "data" / "train_data.csv"
KNOWLEDGE_PATH = CHATBOT_DIR / "LLM" / "knowledge" / "label_constraints.json"
SPECIAL_LABELS = ["Không Rõ"]


SAMPLE_ROWS = [
    ([0, 1], "Tôi muốn đăng ký tài khoản mới rồi đăng nhập vào hệ thống đấu giá."),
    ([2, 3], "Tôi quên mật khẩu và muốn biết quyền bidder seller admin khác nhau thế nào."),
    ([4, 5], "Số dư tài khoản dùng để làm gì và làm sao xem danh sách phiên đấu giá?"),
    ([6], "Tôi muốn xem chi tiết sản phẩm đang đấu giá."),
    ([7, 8], "Hướng dẫn tôi tham gia phòng đấu giá và rời phòng khi không muốn theo dõi nữa."),
    ([9, 10], "Tôi cần đặt giá trong phiên và muốn biết giá realtime cập nhật ra sao."),
    ([11, 12], "Làm sao xem lịch sử đặt giá và chat trong phòng đấu giá?"),
    ([13, 14], "Tôi muốn xem thời gian còn lại của phiên và hiểu anti-sniping là gì."),
    ([15], "Vì sao hệ thống khóa người tham gia mới ở cuối phiên?"),
    ([16], "Khi hết giờ thì hệ thống tự động chốt phiên đấu giá như thế nào?"),
    ([17], "Xử lý tiền khi phiên kết thúc và có người thắng ra sao?"),
    ([18], "Khi phiên đóng thì người dùng có tự động quay về lobby không?"),
    ([19], "Seller tạo yêu cầu phiên đấu giá mới cần làm những bước nào?"),
    ([20], "Hệ thống kiểm tra dữ liệu tạo phiên đấu giá như giá khởi điểm và bước giá ra sao?"),
    ([21], "Yêu cầu duyệt phiên đấu giá được gửi cho admin thế nào?"),
    ([22], "Seller đóng phiên đấu giá của mình trong trường hợp nào?"),
    ([23], "Thông báo trạng thái phiên được gửi khi tham gia đặt giá hoặc đóng phiên ra sao?"),
    ([24], "Chatbot hỗ trợ người dùng có thể trả lời những nội dung nào?"),
    ([25], "Bệnh viện ở đâu và thời tiết hôm nay thế nào?"),
    ([26], "AI hỗ trợ auto approve dùng để dự đoán khả năng duyệt phiên như thế nào?"),
    ([27], "Tôi cần hỗ trợ nhưng chưa biết phải hỏi thế nào."),
]


def normalize_label(label: str) -> str:
    return " ".join(label.strip().upper().split())


def load_labels() -> list[str]:
    payload = json.loads(KNOWLEDGE_PATH.read_text(encoding="utf-8"))
    labels = [normalize_label(document["title"]) for document in payload["documents"]]
    return [*labels, *SPECIAL_LABELS]


def make_row(text: str, active_labels: list[str], labels: list[str]) -> dict[str, str | int]:
    active = set(active_labels)
    row: dict[str, str | int] = {"text": text}
    for label in labels:
        row[label] = 1 if label in active else 0
    return row


def write_training_data() -> None:
    labels = load_labels()
    rows = []

    for indexes, text in SAMPLE_ROWS:
        active_labels = [labels[index] for index in indexes]
        rows.append(make_row(text, active_labels, labels))

    TRAIN_DATA_PATH.parent.mkdir(parents=True, exist_ok=True)
    with TRAIN_DATA_PATH.open("w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=["text", *labels])
        writer.writeheader()
        writer.writerows(rows)

    print(f"Generated {len(rows)} rows at {TRAIN_DATA_PATH}")


if __name__ == "__main__":
    write_training_data()
