import csv
import json
from pathlib import Path


LOGIST_DIR = Path(__file__).resolve().parents[1]
CHATBOT_DIR = LOGIST_DIR.parent
TRAIN_DATA_PATH = LOGIST_DIR / "data" / "train_data.csv"
KNOWLEDGE_PATH = CHATBOT_DIR / "LLM" / "knowledge" / "label_constraints.json"


SAMPLE_ROWS = [
    ([0, 1], "Tôi muốn đăng ký tài khoản mới rồi đăng nhập vào hệ thống đấu giá."),
    ([2, 3], "Tôi quên mật khẩu và muốn biết quyền bidder seller admin khác nhau thế nào."),
    ([4, 5], "Số dư tài khoản dùng để làm gì và làm sao xem danh sách phiên đấu giá?"),
    ([6, 7], "Tôi muốn tìm kiếm lọc phiên rồi xem chi tiết sản phẩm đang đấu giá."),
    ([8, 9], "Hướng dẫn tôi tham gia phòng đấu giá và rời phòng khi không muốn theo dõi nữa."),
    ([10, 11], "Tôi cần đặt giá trong phiên và muốn biết giá realtime cập nhật ra sao."),
    ([12, 13], "Làm sao xem lịch sử đặt giá và chat trong phòng đấu giá?"),
    ([14, 15], "Tôi muốn xem thời gian còn lại của phiên và hiểu anti-sniping là gì."),
    ([16], "Vì sao hệ thống khóa người tham gia mới ở cuối phiên?"),
    ([17], "Khi hết giờ thì hệ thống tự động chốt phiên đấu giá như thế nào?"),
    ([18], "Xử lý tiền khi phiên kết thúc và có người thắng ra sao?"),
    ([19], "Khi phiên đóng thì người dùng có tự động quay về lobby không?"),
    ([20], "Seller tạo yêu cầu phiên đấu giá mới cần làm những bước nào?"),
    ([21], "Hệ thống kiểm tra dữ liệu tạo phiên đấu giá như giá khởi điểm và bước giá ra sao?"),
    ([22], "Yêu cầu duyệt phiên đấu giá được gửi cho admin thế nào?"),
    ([23], "Admin duyệt phiên đấu giá thì phiên được mở ra sao?"),
    ([24], "Admin từ chối phiên đấu giá thì seller thấy kết quả thế nào?"),
    ([25], "Admin xem danh sách người dùng ở đâu?"),
    ([26], "Admin xóa tài khoản người dùng thì dữ liệu cập nhật thế nào?"),
    ([27], "Admin xem danh sách phiên đấu giá để quản lý trạng thái như thế nào?"),
    ([28], "Admin hủy hoặc xóa phiên đấu giá thì có chuyển tiền không?"),
    ([29], "Seller đóng phiên đấu giá của mình trong trường hợp nào?"),
    ([30], "Thông báo trạng thái phiên được gửi khi tham gia đặt giá hoặc đóng phiên ra sao?"),
    ([31], "Dữ liệu đấu giá được lưu vào database gồm những thông tin nào?"),
    ([32], "Chatbot hỗ trợ người dùng có thể trả lời những nội dung nào?"),
    ([33], "Kết nối chatbot Python từ client Java hoạt động như thế nào?"),
    ([34], "AI hỗ trợ auto approve dùng để dự đoán khả năng duyệt phiên như thế nào?"),
    ([35], "Giám sát dữ liệu AI kiểm tra phân phối dữ liệu huấn luyện ra sao?"),
    ([36], "Ghi log và audit giúp debug và theo dõi nghiệp vụ như thế nào?"),
    ([37], "Rate limit bảo vệ thao tác nhạy cảm khỏi spam như thế nào?"),
]


def normalize_label(label: str) -> str:
    return " ".join(label.strip().upper().split())


def load_labels() -> list[str]:
    payload = json.loads(KNOWLEDGE_PATH.read_text(encoding="utf-8"))
    return [normalize_label(document["title"]) for document in payload["documents"]]


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
