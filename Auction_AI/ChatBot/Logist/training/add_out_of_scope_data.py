import csv
import sys
from pathlib import Path


LOGIST_DIR = Path(__file__).resolve().parents[1]
DATA_DIR = LOGIST_DIR / "data"
GENERATED_DIR = DATA_DIR / "generated"
TRAIN_DATA_PATH = DATA_DIR / "train_data.csv"

OFF_TOPIC_LABEL = "NGOÀI LỀ"
BAD_OUT_TOPIC_LABELS = {"NGO?I L?"}
AI_LABEL = "AI HỖ TRỢ AUTO APPROVE"
UNCLEAR_LABEL = "Không Rõ"
OFF_TOPIC_FILE = GENERATED_DIR / "train_data__NGOAI_LE.csv"


OFF_TOPIC_TEXTS = [
    "Bệnh viện ở đâu?",
    "Bệnh viện gần nhất nằm ở đâu?",
    "Cho tôi địa chỉ bệnh viện gần nhất.",
    "Tìm phòng khám gần đây giúp tôi.",
    "Bố đang bị đau chân.",
    "Bố tôi bị đau chân nên làm gì?",
    "Mẹ tôi đau lưng cần đi đâu khám?",
    "Tôi bị đau bụng nên uống thuốc gì?",
    "Tôi bị sốt có nguy hiểm không?",
    "Đau đầu kéo dài phải làm sao?",
    "Thời tiết nay thế nào?",
    "Thời tiết hôm nay thế nào?",
    "Ngày mai có mưa không?",
    "Nhiệt độ Hà Nội hôm nay bao nhiêu?",
    "Dự báo thời tiết cuối tuần ra sao?",
    "Giá vàng hôm nay bao nhiêu?",
    "Bitcoin hôm nay tăng hay giảm?",
    "Tỷ giá đô la hôm nay là bao nhiêu?",
    "Tin tức mới nhất hôm nay có gì?",
    "Ai là tổng thống Mỹ hiện tại?",
    "Thủ đô của Pháp là gì?",
    "Dân số Việt Nam khoảng bao nhiêu?",
    "Giải phương trình bậc hai này giúp tôi.",
    "Tính đạo hàm của hàm số này.",
    "Viết giúp tôi bài văn về mùa xuân.",
    "Tóm tắt tác phẩm Vợ nhặt.",
    "Dịch câu này sang tiếng Anh.",
    "Dịch tiếng Nhật câu xin chào.",
    "Làm slide thuyết trình về môi trường.",
    "Viết email xin nghỉ học giúp tôi.",
    "Viết code Python đọc file Excel.",
    "Sửa lỗi JavaScript này giúp tôi.",
    "Máy tính của tôi bị chậm phải làm sao?",
    "Cài đặt Windows như thế nào?",
    "Điện thoại không lên nguồn xử lý sao?",
    "Tư vấn mua điện thoại dưới 10 triệu.",
    "So sánh iPhone và Samsung.",
    "Nên mua laptop hãng nào?",
    "Cách nấu phở bò tại nhà.",
    "Tối nay nên ăn món gì?",
    "Công thức làm bánh flan.",
    "Luộc trứng bao lâu thì chín?",
    "Đặt vé máy bay đi Đà Nẵng giúp tôi.",
    "Khách sạn nào tốt ở Đà Lạt?",
    "Đường tới sân bay Nội Bài đi thế nào?",
    "Quán cà phê đẹp gần đây ở đâu?",
    "Lịch chiếu phim hôm nay có gì?",
    "Bộ phim nào đang hot?",
    "Kể tôi một câu chuyện cười.",
    "Gợi ý bài hát để thư giãn.",
    "Tin bóng đá tối nay thế nào?",
    "Đội tuyển nào thắng trận hôm qua?",
    "Lịch thi đấu World Cup ra sao?",
    "Tư vấn tập gym cho người mới.",
    "Tôi nên chạy bộ bao nhiêu phút mỗi ngày?",
    "Cách chăm sóc da mụn.",
    "Tóc rụng nhiều phải làm sao?",
    "Người yêu giận thì nên nhắn gì?",
    "Tôi đang buồn hãy an ủi tôi.",
    "Kể chuyện ma nghe đi.",
    "Bạn có biết hát không?",
    "Vẽ cho tôi một con mèo.",
    "Tạo ảnh phong cảnh núi tuyết.",
    "Đọc lá số tử vi giúp tôi.",
    "Hôm nay cung Bạch Dương thế nào?",
    "Mơ thấy rắn là điềm gì?",
    "Tư vấn phong thủy bàn làm việc.",
    "Cách trồng cây monstera.",
    "Chó bị bỏ ăn phải làm sao?",
    "Mèo bị rụng lông nhiều.",
    "Cách sửa xe máy không đề được.",
    "Xe ô tô báo lỗi động cơ.",
    "Tuyến xe buýt đến bến xe Mỹ Đình.",
    "Mua bảo hiểm y tế ở đâu?",
    "Làm căn cước công dân cần giấy tờ gì?",
    "Đăng ký kết hôn cần chuẩn bị gì?",
    "Nộp thuế thu nhập cá nhân thế nào?",
    "Tìm việc làm part time ở Hà Nội.",
    "Viết CV xin thực tập giúp tôi.",
    "Phỏng vấn nên trả lời thế nào?",
    "Lương ngành IT hiện nay bao nhiêu?",
    "benh vien o dau",
    "benh vien gan nhat o dau",
    "bo toi bi dau chan",
    "bo dang bi dau chan",
    "me toi dau lung",
    "toi bi dau bung nen lam gi",
    "toi bi sot nen uong thuoc gi",
    "thoi tiet nay the nao",
    "thoi tiet hom nay the nao",
    "ngay mai co mua khong",
    "gia vang hom nay bao nhieu",
    "bitcoin hom nay tang hay giam",
    "giai bai tap toan nay giup toi",
    "viet bai van ve mua xuan",
    "dich cau nay sang tieng anh",
    "viet code python giup toi",
    "may tinh bi cham phai lam sao",
    "tu van mua dien thoai",
    "toi nay nen an gi",
    "cach nau pho bo",
    "dat ve may bay di da nang",
    "quan ca phe dep gan day",
    "lich chieu phim hom nay",
    "ke toi mot cau chuyen cuoi",
    "tin bong da hom nay",
    "nguoi yeu gian nen lam gi",
    "toi dang buon hay an ui toi",
    "hom nay cung bach duong the nao",
    "lam can cuoc cong dan can giay to gi",
    "viet cv xin viec giup toi",
]


def read_csv(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    with path.open("r", encoding="utf-8", newline="") as file:
        reader = csv.DictReader(file)
        return list(reader.fieldnames or []), list(reader)


def write_csv(path: Path, fieldnames: list[str], rows: list[dict[str, str | int]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames, lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def normalize_fieldnames(fieldnames: list[str]) -> list[str]:
    labels = [
        field
        for field in fieldnames
        if field != OFF_TOPIC_LABEL and field not in BAD_OUT_TOPIC_LABELS
    ]

    if OFF_TOPIC_LABEL not in labels:
        insert_at = len(labels)
        for marker in (AI_LABEL, UNCLEAR_LABEL):
            if marker in labels:
                insert_at = labels.index(marker)
                break
        labels.insert(insert_at, OFF_TOPIC_LABEL)

    return labels


def normalize_row(row: dict[str, str], fieldnames: list[str]) -> dict[str, str | int]:
    return {field: row.get(field, 0) for field in fieldnames}


def is_existing_out_of_scope_row(row: dict[str, str]) -> bool:
    if row.get(OFF_TOPIC_LABEL) == "1":
        return True
    return any(row.get(label) == "1" for label in BAD_OUT_TOPIC_LABELS)


def make_out_of_scope_row(text: str, fieldnames: list[str]) -> dict[str, str | int]:
    row = {field: 0 for field in fieldnames}
    row["text"] = text
    row[OFF_TOPIC_LABEL] = 1
    return row


def sync_train_data() -> tuple[int, int]:
    fieldnames, rows = read_csv(TRAIN_DATA_PATH)
    fieldnames = normalize_fieldnames(fieldnames)
    rows = [
        normalize_row(row, fieldnames)
        for row in rows
        if not is_existing_out_of_scope_row(row)
    ]
    rows.extend(make_out_of_scope_row(text, fieldnames) for text in OFF_TOPIC_TEXTS)
    write_csv(TRAIN_DATA_PATH, fieldnames, rows)
    return len(rows), len(OFF_TOPIC_TEXTS)


def sync_generated_data() -> None:
    for path in GENERATED_DIR.glob("*.csv"):
        if path == OFF_TOPIC_FILE:
            continue
        fieldnames, rows = read_csv(path)
        fieldnames = normalize_fieldnames(fieldnames)
        rows = [normalize_row(row, fieldnames) for row in rows]
        write_csv(path, fieldnames, rows)

    fieldnames, _ = read_csv(TRAIN_DATA_PATH)
    write_csv(
        OFF_TOPIC_FILE,
        fieldnames,
        [make_out_of_scope_row(text, fieldnames) for text in OFF_TOPIC_TEXTS],
    )


def main() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")

    total_rows, off_topic_rows = sync_train_data()
    sync_generated_data()
    print(f"Updated train rows: {total_rows}")
    print(f"Out-of-scope rows: {off_topic_rows}")
    print(f"Out-of-scope label: {OFF_TOPIC_LABEL}")


if __name__ == "__main__":
    main()
