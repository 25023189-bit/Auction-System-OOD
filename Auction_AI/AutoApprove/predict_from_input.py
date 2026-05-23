import argparse
import json
import logging
import math
import time
from pathlib import Path

import joblib
import pandas as pd

from diagnostics import DEFAULT_DEBUG_OUTPUT_PATH, DEFAULT_INPUT_PATH as BRIDGE_INPUT_PATH, DEFAULT_MODEL_PATH, DEFAULT_OUTPUT_PATH as BRIDGE_OUTPUT_PATH, get_logger, log_path_diagnostics, setup_logging


BASE_DIR = Path(__file__).resolve().parent
DEFAULT_INPUT_PATH = BASE_DIR / "input_ap.json"
DEFAULT_OUTPUT_PATH = BASE_DIR / "prediction_results.json"
MODEL_PATH = DEFAULT_MODEL_PATH
DECISION_THRESHOLD = 0.75

LOGGER = get_logger()


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

def resolve_ai_directory() -> Path:
    return Path(__file__).resolve().parent


def strip_leading_ai_dir(raw_path: Path, ai_directory: Path) -> Path:
    """
    Nếu user truyền AutoApprove/test_samples.json trong khi base đã là .../AutoApprove,
    bỏ segment AutoApprove đầu tiên để tránh .../AutoApprove/AutoApprove/...
    """
    parts = raw_path.parts

    if parts and parts[0].lower() == ai_directory.name.lower():
        return Path(*parts[1:])

    return raw_path


def resolve_input_path(raw_value: str, ai_directory: Path) -> Path:
    path = Path(raw_value)

    if path.is_absolute():
        return path.resolve()

    # Ưu tiên resolve theo current working directory.
    cwd_candidate = (Path.cwd() / path).resolve()
    if cwd_candidate.exists():
        return cwd_candidate

    # Fallback theo thư mục AutoApprove, chống nhân đôi AutoApprove/AutoApprove.
    stripped = strip_leading_ai_dir(path, ai_directory)
    ai_candidate = (ai_directory / stripped).resolve()
    return ai_candidate


def resolve_output_path(raw_value: str, ai_directory: Path) -> Path:
    path = Path(raw_value)

    if path.is_absolute():
        return path.resolve()

    # Ưu tiên theo current working directory nếu parent tồn tại.
    cwd_candidate = (Path.cwd() / path).resolve()
    if cwd_candidate.parent.exists():
        return cwd_candidate

    # Fallback theo thư mục AutoApprove, chống nhân đôi AutoApprove/AutoApprove.
    stripped = strip_leading_ai_dir(path, ai_directory)
    return (ai_directory / stripped).resolve()

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
    LOGGER.info("AutoApprove loading samples. input_path=%s exists=%s", input_path, input_path.exists())
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
    LOGGER.info("AutoApprove loaded samples. rows=%s columns=%s", len(samples), list(samples.columns))
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
    output_path.parent.mkdir(parents=True, exist_ok=True)
    if output_path.name == "output_ap.json":
        decision = bool(records and int(records[0].get("auto_approve", 0)) == 1)
        with output_path.open("w", encoding="utf-8") as file:
            file.write("true" if decision else "false")
        # Keep a rich diagnostic output without breaking the Java boolean protocol.
        with DEFAULT_DEBUG_OUTPUT_PATH.open("w", encoding="utf-8") as debug_file:
            json.dump(records, debug_file, ensure_ascii=False, indent=2)
        LOGGER.info("AutoApprove wrote boolean bridge output: %s decision=%s", output_path, decision)
        LOGGER.info("AutoApprove wrote rich debug output: %s", DEFAULT_DEBUG_OUTPUT_PATH)
        return

    if output_path.suffix.lower() == ".json":
        with output_path.open("w", encoding="utf-8") as file:
            json.dump(records, file, ensure_ascii=False, indent=2)
        LOGGER.info("AutoApprove wrote JSON output: %s records=%s", output_path, len(records))
    elif output_path.suffix.lower() == ".csv":
        pd.DataFrame(records).to_csv(output_path, index=False, encoding="utf-8-sig")
        LOGGER.info("AutoApprove wrote CSV output: %s records=%s", output_path, len(records))
    else:
        raise ValueError("Output file must be .json or .csv")


def predict_samples(input_path: Path, output_path: Path) -> list[dict]:
    setup_logging()
    log_path_diagnostics(input_path=input_path, output_path=output_path, model_path=MODEL_PATH)
    start_time = time.perf_counter()
    samples = load_samples(input_path)
    LOGGER.info("AutoApprove loading model. model_path=%s exists=%s", MODEL_PATH, MODEL_PATH.exists())
    model = joblib.load(MODEL_PATH)
    results = []

    for index, data in samples.iterrows():
        sample_number = index + 1
        model_row = build_model_row(data.to_dict(), sample_number)
        probability = float(model.predict_proba(model_row)[0][1])
        decision = int(probability >= DECISION_THRESHOLD)

        LOGGER.info("AutoApprove predicted sample=%s title_length=%s probability=%.6f decision=%s", sample_number, len(str(data.get("title", ""))), probability, decision)
        results.append(
            {
                "title": data.get("title", ""),
                "decision": "APPROVE" if decision == 1 else "REJECT",
                "auto_approve": decision,
                "approve_probability": probability,
                "approve_probability_percent": round(probability * 100, 2),
            }
        )

    save_results(results, output_path)
    elapsed_ms = round((time.perf_counter() - start_time) * 1000, 2)
    LOGGER.info("AutoApprove prediction completed. records=%s elapsed_ms=%s output_exists=%s", len(results), elapsed_ms, output_path.exists())
    return results


def main():
    parser = argparse.ArgumentParser(description="Predict all auction samples from a JSON or CSV file.")
    parser.add_argument(
        "--input",
        type=str,
        default=DEFAULT_INPUT_PATH.name,
        help=f"Input .json/.csv path. Default: {DEFAULT_INPUT_PATH.name}",
    )
    parser.add_argument(
        "--output",
        type=str,
        default=DEFAULT_OUTPUT_PATH.name,
        help=f"Output .json/.csv path. Default: {DEFAULT_OUTPUT_PATH.name}",
    )

    args = parser.parse_args()

    ai_directory = resolve_ai_directory()

    input_path = resolve_input_path(args.input, ai_directory)
    output_path = resolve_output_path(args.output, ai_directory)

    setup_logging()
    LOGGER.info("AutoApprove CLI started. input=%s output=%s", input_path, output_path)

    try:
        results = predict_samples(input_path, output_path)
    except Exception:
        LOGGER.exception("AutoApprove CLI failed. input=%s output=%s", input_path, output_path)
        raise

    print(json.dumps(results, ensure_ascii=False, indent=2))
    print(f"\nSaved prediction results to: {output_path}")


if __name__ == "__main__":
    main()
