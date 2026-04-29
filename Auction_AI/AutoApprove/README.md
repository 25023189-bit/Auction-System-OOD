# Auction AI

## Files

- `Training_Data.csv`: dataset huấn luyện hiện tại, 1.000 mẫu, đúng schema dữ liệu đầu vào hiện tại.
- `train_logistic_regression.py`: train Logistic Regression với title, description, organization và các feature số.
- `auction_model.pkl`: model đã train.
- `model_tester.py`: giao diện Python để nhập dữ liệu và xem quyết định của model.
- `input_data.json`: nơi nhập dữ liệu test bằng file.
- `predict_from_input.py`: đọc `input_data.json` và in kết quả dự đoán.

## Cài thư viện

```bash
pip install pandas scikit-learn joblib
```

`model_tester.py` dùng `tkinter`, thường có sẵn trong Python trên Windows.

## Train lại model

```bash
python train_logistic_regression.py
```

Dataset được chỉnh trực tiếp trong `Training_Data.csv`; hiện không dùng script sinh dữ liệu.

Dataset chỉ giữ các field: `title`, `category`, `organization`, `description`, `seller_rating`, `seller_completed_rating`, `seller_cancel_rate`, `minimum_join_amount`, `bid_step`, `start_price`, `start_price_log`, `duration_minutes`, `extension_seconds`, `start_hour`, `day_of_week`, `is_weekend`, `targeted_risk_type`, `description_style`, `review_status`, `auto_approve`.

Model binary vẫn train theo `auto_approve`; các dòng `manual_review` được xem là `not_auto_approve`.

`targeted_risk_type` chỉ dùng để mô tả lý do cần review, không đưa vào feature huấn luyện.

`title_length` và `desc_length` không nằm trong CSV; train script tự tính trong bộ nhớ. `num_positive_keywords` không còn dùng trong dataset để tránh model học kiểu "nhiều từ tốt = approve".

Nhóm nhà đất dùng giá khởi điểm và bước giá lớn hơn, có nhiễu về pháp lý, quy hoạch, đặt cọc, quyền thuê, giấy chứng nhận và giá thầu lệch thị trường.

## Test bằng giao diện

```bash
python model_tester.py
```

Giao diện sẽ tự tính các feature phụ:

- `title_length`
- `desc_length`
- `num_positive_keywords`
- `start_price_log`
- `is_weekend`

Kết quả hiển thị gồm quyết định `APPROVE`/`REJECT` và xác suất approve.

## Test bằng file nhập

Sửa dữ liệu trong:

```bash
input_data.json
```

Sau đó chạy:

```bash
python predict_from_input.py
```
