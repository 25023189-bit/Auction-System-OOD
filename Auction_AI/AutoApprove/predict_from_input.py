import argparse
import json
import math
from pathlib import Path

import joblib
import pandas as pd


BASE_DIR = Path(__file__).resolve().parent
DEFAULT_INPUT_PATH = BASE_DIR / "test_samples.json"
DEFAULT_OUTPUT_PATH = BASE_DIR / "prediction_results.json"
MODEL_PATH = BASE_DIR / "auction_model.pkl"
DECISION_THRESHOLD = 0.75


POSITIVE_KEYWORDS = [
    "hàng chính hãng",
    "chinh hang",
    "còn bảo hành",
    "con bao hanh",
    "đầy đủ phụ kiện",
    "day du phu kien",
    "full phụ kiện",
    "full phu kien",
    "ngoại hình đẹp",
    "ngoai hinh dep",
    "hoạt động ổn định",
    "hoat dong on dinh",
    "có hóa đơn",
    "co hoa don",
    "pin còn tốt",
    "pin con tot",
    "nguyên hộp",
    "nguyen hop",
    "sẵn sàng kiểm tra",
    "san sang kiem tra",
    "like new",
    "ít dùng",
    "it dung",
]


REQUIRED_FIELDS = [
    "title",
    "organization",
    "description",
    "seller_rating",
    "seller_completed_rating",
    "seller_cancel_rate",
    "start_price",
    "minimum_join_amount",
    "bid_step",
    "duration_minutes",
    "extension_seconds",
    "start_hour",
    "day_of_week",
]


def count_positive_keywords(text: str) -> int:
    normalized = text.lower()
    return sum(1 for keyword in POSITIVE_KEYWORDS if keyword in normalized)


def is_missing(value) -> bool:
    return value is None or pd.isna(value)


def required(data: dict, key: str, row_number: int):
    value = data.get(key)
    if is_missing(value):
        raise ValueError(f"Sample {row_number}: missing required field: {key}")
    return value


def load_samples(input_path: Path) -> pd.DataFrame:
    suffix = input_path.suffix.lower()
    if suffix == ".json":
        with input_path.open("r", encoding="utf-8-sig") as file:
            payload = json.load(file)
        if isinstance(payload, dict):
            payload = payload.get("samples")
        if not isinstance(payload, list):
            raise ValueError("JSON input must be a list, or an object with a 'samples' list.")
        samples = pd.DataFrame(payload)
    elif suffix == ".csv":
        samples = pd.read_csv(input_path)
    else:
        raise ValueError("Input file must be .json or .csv")

    validate_input_fields(samples)
    return samples


def validate_input_fields(samples: pd.DataFrame):
    missing = [field for field in REQUIRED_FIELDS if field not in samples.columns]
    if missing:
        raise ValueError(f"Missing required fields in input file: {', '.join(missing)}")


def build_model_row(data: dict, sample_number: int) -> pd.DataFrame:
    title = str(required(data, "title", sample_number)).strip()
    organization = str(required(data, "organization", sample_number)).strip()
    description = str(required(data, "description", sample_number)).strip()
    start_price = float(required(data, "start_price", sample_number))
    day_of_week = int(required(data, "day_of_week", sample_number))
    start_hour = int(required(data, "start_hour", sample_number))

    if not title or not organization or not description:
        raise ValueError(f"Sample {sample_number}: title, organization, and description cannot be empty.")
    if start_price <= 0:
        raise ValueError(f"Sample {sample_number}: start_price must be greater than 0.")
    if not 0 <= day_of_week <= 6:
        raise ValueError(f"Sample {sample_number}: day_of_week must be from 0 to 6.")
    if not 0 <= start_hour <= 23:
        raise ValueError(f"Sample {sample_number}: start_hour must be from 0 to 23.")

    return pd.DataFrame(
        [
            {
                "title": title,
                "organization": organization,
                "description": description,
                "seller_rating": float(required(data, "seller_rating", sample_number)),
                "seller_completed_rating": float(required(data, "seller_completed_rating", sample_number)),
                "seller_cancel_rate": float(required(data, "seller_cancel_rate", sample_number)),
                "title_length": len(title),
                "desc_length": len(description),
                # Kept for compatibility with older trained pipelines if present.
                "num_positive_keywords": count_positive_keywords(f"{title} {description}"),
                "minimum_join_amount": float(required(data, "minimum_join_amount", sample_number)),
                "minimum_join_ratio": float(required(data, "minimum_join_amount", sample_number)) / start_price,
                "bid_step": float(required(data, "bid_step", sample_number)),
                "bid_step_ratio": float(required(data, "bid_step", sample_number)) / start_price,
                "start_price": start_price,
                "start_price_log": math.log(start_price),
                "duration_minutes": int(required(data, "duration_minutes", sample_number)),
                "extension_seconds": int(required(data, "extension_seconds", sample_number)),
                "start_hour": start_hour,
                "day_of_week": day_of_week,
                "is_weekend": 1 if day_of_week in (5, 6) else 0,
            }
        ]
    )


def save_results(records: list[dict], output_path: Path):
    output_path.parent.mkdir(exist_ok=True)
    if output_path.name == "output_ap.json":
        decision = bool(records and int(records[0].get("auto_approve", 0)) == 1)
        with output_path.open("w", encoding="utf-8") as file:
            file.write("true" if decision else "false")
        return

    if output_path.suffix.lower() == ".json":
        with output_path.open("w", encoding="utf-8") as file:
            json.dump(records, file, ensure_ascii=False, indent=2)
    elif output_path.suffix.lower() == ".csv":
        pd.DataFrame(records).to_csv(output_path, index=False, encoding="utf-8-sig")
    else:
        raise ValueError("Output file must be .json or .csv")


def predict_samples(input_path: Path, output_path: Path) -> list[dict]:
    samples = load_samples(input_path)
    model = joblib.load(MODEL_PATH)
    results = []

    for index, data in samples.iterrows():
        sample_number = index + 1
        model_row = build_model_row(data.to_dict(), sample_number)
        probability = float(model.predict_proba(model_row)[0][1])
        decision = int(probability >= DECISION_THRESHOLD)

        results.append(
            {
                "title": data.get("title", ""),
                "category": data.get("category", ""),
                "decision": "APPROVE" if decision == 1 else "REJECT",
                "auto_approve": decision,
                "approve_probability": probability,
                "approve_probability_percent": round(probability * 100, 2),
            }
        )

    save_results(results, output_path)
    return results


def main():
    parser = argparse.ArgumentParser(description="Predict all auction samples from a JSON or CSV file.")
    parser.add_argument(
        "--input",
        type=Path,
        default=DEFAULT_INPUT_PATH,
        help=f"Input .json/.csv path. Default: {DEFAULT_INPUT_PATH.name}",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=DEFAULT_OUTPUT_PATH,
        help=f"Output .json/.csv path. Default: {DEFAULT_OUTPUT_PATH.name}",
    )
    args = parser.parse_args()

    input_path = args.input if args.input.is_absolute() else BASE_DIR / args.input
    output_path = args.output if args.output.is_absolute() else BASE_DIR / args.output

    results = predict_samples(input_path, output_path)
    print(json.dumps(results, ensure_ascii=False, indent=2))
    print(f"\nSaved prediction results to: {output_path}")


if __name__ == "__main__":
    main()
