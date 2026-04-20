# Auction ML Project

## Files
- `generate_dataset.py`: sinh dữ liệu giả lập cho bài toán auto-approve phiên đấu giá
- `auction_dataset.csv`: dataset mẫu 10000 dòng
- `train_logistic_regression.py`: train Logistic Regression với BoW + feature số
- `predict_one.py`: dự đoán một mẫu đầu vào bằng model đã train

## Cài thư viện
```bash
pip install pandas scikit-learn joblib
```

## Chạy theo thứ tự
```bash
python generate_dataset.py
python train_logistic_regression.py
```

## Dự đoán thử
```bash
python predict_one.py \
  --title "iphone 13 fullbox chinh hang" \
  --description "san pham dep bao hanh day du phu kien thong tin trung thuc" \
  --seller_rating 4.5 \
  --seller_completed_auctions 120 \
  --seller_cancel_rate 0.05 \
  --title_length 28 \
  --desc_length 75 \
  --num_positive_keywords 3 \
  --num_negative_keywords 0 \
  --start_price_log 15.2 \
  --duration_minutes 90 \
  --extension_seconds 30 \
  --start_hour 20 \
  --day_of_week 6 \
  --is_weekend 1
```
