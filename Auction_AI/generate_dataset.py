import csv
import math
import random
import re
from pathlib import Path
import time

random.seed(42)

positive_keywords = [
    "chinh hang", "fullbox", "bao hanh", "like new", "moi", "nguyen seal",
    "day du phu kien", "zin", "dep", "tot", "cao cap", "it dung"
]
negative_keywords = [
    "hong", "loi", "xuoc", "vo", "mat phu kien", "khong bao hanh",
    "can sua", "cu", "test", "gap", "mo ta so sai", "khong ro"
]

product_templates = [
    ("dien thoai", ["iphone 13", "iphone 14 pro", "samsung s23", "xiaomi 13", "oppo find x5"]),
    ("laptop", ["macbook air m1", "thinkpad x1", "dell xps 13", "asus tuf", "hp spectre"]),
    ("tai nghe", ["airpods pro", "sony wh-1000xm5", "galaxy buds", "beats studio buds"]),
    ("dong ho", ["apple watch", "galaxy watch", "casio gshock", "seiko 5"]),
    ("camera", ["canon eos", "sony a6400", "nikon d750", "fujifilm xt30"]),
]

def sample_title_and_desc():
    category, products = random.choice(product_templates)
    product = random.choice(products)
    pos_count = random.choices([0, 1, 2, 3], weights=[0.1, 0.35, 0.35, 0.2])[0]
    neg_count = random.choices([0, 1, 2], weights=[0.7, 0.22, 0.08])[0]

    chosen_pos = random.sample(positive_keywords, k=min(pos_count, len(positive_keywords)))
    chosen_neg = random.sample(negative_keywords, k=min(neg_count, len(negative_keywords)))

    title_parts = [product]
    if chosen_pos:
        title_parts.append(random.choice(chosen_pos))
    if chosen_neg and random.random() < 0.45:
        title_parts.append(random.choice(chosen_neg))
    title = " ".join(title_parts)

    desc_parts = [
        f"san pham thuoc nhom {category}",
        f"ten san pham {product}",
    ]
    for kw in chosen_pos:
        desc_parts.append(f"co dac diem {kw}")
    for kw in chosen_neg:
        desc_parts.append(f"luu y {kw}")
    filler = [
        "hinh anh that", "co the giao dich truc tiep", "nguon goc ro rang",
        "mo ta chi tiet", "co hoa don", "ho tro kiem tra", "da su dung on dinh",
        "chat luong tot", "ngoai hinh dep", "thong tin trung thuc"
    ]
    desc_parts.extend(random.sample(filler, k=random.randint(2, 5)))
    description = ". ".join(desc_parts)
    return title, description

def clean_text(text: str) -> str:
    text = text.lower().strip()
    text = re.sub(r"[^a-z0-9\s]", " ", text)
    text = re.sub(r"\s+", " ", text)
    return text

def count_keywords(text: str, keywords):
    text = clean_text(text)
    return sum(1 for kw in keywords if kw in text)

def generate_row():
    title, description = sample_title_and_desc()

    seller_rating = round(random.uniform(1.0, 5.0), 2)
    seller_completed_auctions = random.randint(0, 250)
    seller_cancel_rate = round(random.uniform(0.0, 0.45), 3)

    start_price = random.randint(50_000, 50_000_000)
    start_price_log = round(math.log(start_price + 1), 5)

    duration_minutes = random.choice([30, 45, 60, 90, 120, 180, 240])
    extension_seconds = random.choice([15, 30, 45, 60, 90, 120])

    start_hour = random.randint(0, 23)
    day_of_week = random.randint(0, 6)
    is_weekend = 1 if day_of_week in (5, 6) else 0

    title_length = len(title)
    desc_length = len(description)
    num_positive_keywords = count_keywords(title + " " + description, positive_keywords)
    num_negative_keywords = count_keywords(title + " " + description, negative_keywords)

    score = 0.0
    score += (seller_rating - 3.0) * 0.9
    score += min(seller_completed_auctions / 120.0, 1.5) * 0.7
    score -= seller_cancel_rate * 2.8
    score += 0.6 if desc_length >= 100 else -0.5
    score += 0.35 if title_length >= 12 else -0.4
    score += num_positive_keywords * 0.35
    score -= num_negative_keywords * 0.9
    score += 0.35 if 19 <= start_hour <= 22 else 0.0
    score += 0.15 if is_weekend else 0.0
    score += 0.2 if duration_minutes in (60, 90, 120) else -0.1
    score += 0.1 if extension_seconds in (30, 45, 60) else -0.05
    score += 0.15 if 11.5 <= start_price_log <= 16.8 else -0.2
    score += random.uniform(-0.8, 0.8)

    auto_approve = 1 if score >= 0.9 else 0

    return {
        "title": title,
        "description": description,
        "seller_rating": seller_rating,
        "seller_completed_auctions": seller_completed_auctions,
        "seller_cancel_rate": seller_cancel_rate,
        "title_length": title_length,
        "desc_length": desc_length,
        "num_positive_keywords": num_positive_keywords,
        "num_negative_keywords": num_negative_keywords,
        "start_price": start_price,
        "start_price_log": start_price_log,
        "duration_minutes": duration_minutes,
        "extension_seconds": extension_seconds,
        "start_hour": start_hour,
        "day_of_week": day_of_week,
        "is_weekend": is_weekend,
        "auto_approve": auto_approve,
    }

def generate_dataset(n_rows=10**9, output_path="auction_dataset.csv"):
    st = time.time()
    rows = [generate_row() for _ in range(n_rows)]
    with open(output_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)
    print(f"Saved {n_rows} rows to {output_path}")
    print("Time run:",time.time() - st,"s")

if __name__ == "__main__":
    generate_dataset()
