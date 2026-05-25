import hashlib
import json
import shutil
import sys
from datetime import datetime
from json import JSONDecodeError
from pathlib import Path

import pandas as pd
from joblib import dump
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    f1_score,
    hamming_loss,
    precision_score,
    recall_score,
)
from sklearn.model_selection import train_test_split
from sklearn.multiclass import OneVsRestClassifier
from sklearn.pipeline import Pipeline


AUCTION_AI_DIR = Path(__file__).resolve().parents[3]
if str(AUCTION_AI_DIR) not in sys.path:
    sys.path.insert(0, str(AUCTION_AI_DIR))

from ChatBot.Logist.chatbot.paths import LABELS_PATH, METRICS_PATH, MODEL_PATH
from ChatBot.Logist.chatbot.paths import GENERATED_DATA_DIR, TRAIN_DATA_MANIFEST_PATH


TEXT_COLUMN = "text"
MANIFEST_KEY = "processed_files"
SUCCESS_STATUS = "success"
FAILED_STATUS = "failed"
TRAIN_DATA_PREFIX = "train_data"
IGNORED_FILE_MARKERS = ("backup", ".bak", "tmp", "temp", "~")
PREDICTION_THRESHOLD = 0.5
UNCLEAR_LABEL = "KHÔNG RÕ"


def normalize_label(label: str) -> str:
    return " ".join(str(label).strip().upper().split())


def build_model() -> Pipeline:
    return Pipeline(
        steps=[
            (
                "tfidf",
                TfidfVectorizer(
                    lowercase=True,
                    ngram_range=(1, 2),
                    min_df=1,
                    max_features=5000,
                ),
            ),
            (
                "clf",
                OneVsRestClassifier(
                    LogisticRegression(
                        max_iter=2000,
                        class_weight="balanced",
                        solver="liblinear",
                    )
                ),
            ),
        ]
    )


def timestamp() -> str:
    return datetime.now().isoformat(timespec="seconds")


def manifest_file_path(path: Path) -> str:
    return path.relative_to(TRAIN_DATA_MANIFEST_PATH.parent.parent).as_posix()


def file_checksum(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as file:
        for chunk in iter(lambda: file.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def write_manifest(manifest: dict) -> None:
    TRAIN_DATA_MANIFEST_PATH.parent.mkdir(parents=True, exist_ok=True)
    TRAIN_DATA_MANIFEST_PATH.write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def load_manifest() -> dict:
    if not TRAIN_DATA_MANIFEST_PATH.exists():
        manifest = {MANIFEST_KEY: []}
        write_manifest(manifest)
        return manifest

    try:
        manifest = json.loads(TRAIN_DATA_MANIFEST_PATH.read_text(encoding="utf-8"))
    except JSONDecodeError as exc:
        backup_path = TRAIN_DATA_MANIFEST_PATH.with_name(
            f"{TRAIN_DATA_MANIFEST_PATH.stem}.invalid-{datetime.now():%Y%m%d%H%M%S}.json.bak"
        )
        shutil.copy2(TRAIN_DATA_MANIFEST_PATH, backup_path)
        raise ValueError(
            f"Manifest JSON không hợp lệ: {exc}. Đã tạo backup tại: {backup_path}"
        ) from exc

    if not isinstance(manifest, dict):
        raise ValueError("Manifest phải là JSON object.")
    if MANIFEST_KEY not in manifest:
        manifest[MANIFEST_KEY] = []
    if not isinstance(manifest[MANIFEST_KEY], list):
        raise ValueError(f"Manifest field '{MANIFEST_KEY}' phải là list.")

    return manifest


def manifest_entries_by_path(manifest: dict) -> dict[str, dict]:
    entries = {}
    for entry in manifest.get(MANIFEST_KEY, []):
        if isinstance(entry, dict) and entry.get("file_path"):
            entries[str(entry["file_path"])] = entry
    return entries


def update_manifest_entry(
    manifest: dict,
    path: Path,
    status: str,
    checksum: str,
    sample_count: int | None = None,
    error: str | None = None,
) -> None:
    file_path = manifest_file_path(path)
    entry = manifest_entries_by_path(manifest).get(file_path)
    if entry is None:
        entry = {}
        manifest[MANIFEST_KEY].append(entry)

    entry.update(
        {
            "file_name": path.name,
            "file_path": file_path,
            "processed_at": timestamp(),
            "status": status,
            "checksum": checksum,
        }
    )

    if status == SUCCESS_STATUS:
        entry["sample_count"] = int(sample_count or 0)
        entry.pop("error", None)
    else:
        entry["error"] = error or "Unknown validation error"
        entry.pop("sample_count", None)


def is_train_data_file(path: Path) -> bool:
    name = path.name.lower()
    if not path.is_file() or path.suffix.lower() != ".csv":
        return False
    if path.name.startswith(".") or path.name.startswith("~$"):
        return False
    if not name.startswith(TRAIN_DATA_PREFIX):
        return False
    return not any(marker in name for marker in IGNORED_FILE_MARKERS)


def scan_generated_train_files() -> list[Path]:
    if not GENERATED_DATA_DIR.exists():
        raise FileNotFoundError(f"Không tìm thấy thư mục generated: {GENERATED_DATA_DIR}")

    files = sorted(path for path in GENERATED_DATA_DIR.iterdir() if is_train_data_file(path))
    if not files:
        raise ValueError(f"Không có file train data hợp lệ trong: {GENERATED_DATA_DIR}")
    return files


def validate_training_frame(df: pd.DataFrame) -> tuple[pd.DataFrame, list[str]]:
    if df.empty:
        raise ValueError("File không có sample.")

    if TEXT_COLUMN not in df.columns:
        raise ValueError(f"CSV phải có cột '{TEXT_COLUMN}'")

    label_columns = [column for column in df.columns if column != TEXT_COLUMN]
    if not label_columns:
        raise ValueError("CSV phải có ít nhất 1 cột label")

    text_values = df[TEXT_COLUMN]
    if text_values.isna().any():
        row_number = int(text_values[text_values.isna()].index[0]) + 2
        raise ValueError(f"Record dòng {row_number} thiếu text.")

    normalized_text = text_values.astype(str).str.strip()
    if normalized_text.eq("").any():
        row_number = int(normalized_text[normalized_text.eq("")].index[0]) + 2
        raise ValueError(f"Record dòng {row_number} có text rỗng.")

    normalized_labels = pd.DataFrame(index=df.index)
    for label in label_columns:
        values = pd.to_numeric(df[label], errors="coerce")
        if values.isna().any():
            row_number = int(values[values.isna()].index[0]) + 2
            raise ValueError(f"Label '{label}' ở dòng {row_number} không phải số 0/1.")
        invalid_values = ~values.isin([0, 1])
        if invalid_values.any():
            row_number = int(invalid_values[invalid_values].index[0]) + 2
            raise ValueError(f"Label '{label}' ở dòng {row_number} không phải giá trị 0/1.")
        normalized_labels[label] = values.astype(int)

    empty_label_rows = normalized_labels.sum(axis=1).eq(0)
    if empty_label_rows.any():
        row_number = int(empty_label_rows[empty_label_rows].index[0]) + 2
        raise ValueError(f"Record dòng {row_number} không có label active.")

    normalized_df = pd.DataFrame({TEXT_COLUMN: normalized_text})
    for label in label_columns:
        normalized_df[label] = normalized_labels[label]

    return normalized_df, label_columns


def validate_training_file(path: Path) -> tuple[pd.DataFrame, list[str]]:
    if not path.exists():
        raise FileNotFoundError(f"File không tồn tại: {path}")
    if path.stat().st_size == 0:
        raise ValueError("File rỗng.")

    try:
        df = pd.read_csv(path)
    except Exception as exc:
        raise ValueError(f"Không đọc được CSV: {exc}") from exc

    return validate_training_frame(df)


def needs_validation(entry: dict | None, checksum: str) -> bool:
    if not entry:
        return True
    if entry.get("status") != SUCCESS_STATUS:
        return True
    return entry.get("checksum") != checksum


def resolve_training_files() -> list[Path]:
    manifest = load_manifest()
    train_files = scan_generated_train_files()
    entries = manifest_entries_by_path(manifest)
    checksums = {path: file_checksum(path) for path in train_files}

    success_before = [
        path for path in train_files
        if not needs_validation(entries.get(manifest_file_path(path)), checksums[path])
    ]
    files_to_validate = [
        path for path in train_files
        if needs_validation(entries.get(manifest_file_path(path)), checksums[path])
    ]

    print(f"Manifest loaded from: {TRAIN_DATA_MANIFEST_PATH}")
    print(f"Generated train data directory: {GENERATED_DATA_DIR}")
    print(f"Generated train data files found: {len(train_files)}")
    print(f"Files already success in manifest: {len(success_before)}")
    print(f"Files requiring validation: {len(files_to_validate)}")

    if not files_to_validate:
        print("No new train data files found.")

    for path in files_to_validate:
        try:
            df, _ = validate_training_file(path)
            update_manifest_entry(
                manifest,
                path,
                SUCCESS_STATUS,
                checksum=checksums[path],
                sample_count=len(df),
            )
            print(f"VALIDATE PASS: {path.name} ({len(df)} samples)")
        except Exception as exc:
            update_manifest_entry(
                manifest,
                path,
                FAILED_STATUS,
                checksum=checksums[path],
                error=str(exc),
            )
            print(f"VALIDATE FAIL: {path.name} - {exc}")

    write_manifest(manifest)
    entries = manifest_entries_by_path(manifest)
    success_files = [
        path for path in train_files
        if entries.get(manifest_file_path(path), {}).get("status") == SUCCESS_STATUS
        and entries.get(manifest_file_path(path), {}).get("checksum") == checksums[path]
    ]

    if not success_files:
        raise ValueError("Không có file train data nào status = success để train model.")

    print(f"Success files used for training: {len(success_files)}")
    for path in success_files:
        print(f"TRAIN FILE: {path.name}")

    return success_files


def load_training_data(data_paths: list[Path]) -> tuple[pd.Series, pd.DataFrame, list[str]]:
    frames: list[pd.DataFrame] = []
    label_columns: list[str] = []

    for path in data_paths:
        df, file_label_columns = validate_training_file(path)
        for label in file_label_columns:
            if label not in label_columns:
                label_columns.append(label)
        frames.append(df)

    if not frames or not label_columns:
        raise ValueError("Không có dữ liệu hợp lệ để train model.")

    aligned_frames = [
        frame.reindex(columns=[TEXT_COLUMN, *label_columns], fill_value=0)
        for frame in frames
    ]
    df = pd.concat(aligned_frames, ignore_index=True)
    X = df[TEXT_COLUMN].astype(str)
    y = df[label_columns].astype(int)

    print(f"Training label columns found: {len(label_columns)}")
    print(f"Total samples used for training: {len(X)}")

    return X, y, label_columns


def predict_with_threshold_fallback(
    model: Pipeline,
    X,
    label_columns: list[str],
    threshold: float = PREDICTION_THRESHOLD,
) -> pd.DataFrame:
    """Convert predict_proba output to multi-label predictions.

    Policy used by the ChatBot runtime:
    - every label with probability >= threshold is selected;
    - if no label reaches threshold, the canonical unclear label is selected;
    - no top-1 or top-k truncation is applied.
    """
    probabilities = model.predict_proba(X)
    y_pred = pd.DataFrame(0, index=range(len(probabilities)), columns=label_columns, dtype=int)

    unclear_columns = [
        label for label in label_columns
        if normalize_label(label) == UNCLEAR_LABEL
    ]
    unclear_column = unclear_columns[0] if unclear_columns else None

    for row_index, row_probabilities in enumerate(probabilities):
        selected_any = False
        for label, probability in zip(label_columns, row_probabilities):
            if float(probability) >= threshold:
                y_pred.at[row_index, label] = 1
                selected_any = True

        if not selected_any and unclear_column is not None:
            y_pred.at[row_index, unclear_column] = 1

    return y_pred


def evaluate_model(y_test: pd.DataFrame, y_pred, label_columns: list[str]) -> dict:
    """
    Metrics quan trọng cho multi-label:
    - micro: gom tất cả label lại rồi tính TP/FP/FN.
    - macro: tính từng label rồi lấy trung bình đều.
    - weighted: tính từng label rồi trung bình theo số mẫu thật của label.
    - samples: tính theo từng câu/user message rồi lấy trung bình.
    - exact_match_ratio: phải đoán đúng toàn bộ tập nhãn của một câu.
    - hamming_loss: tỷ lệ label bị đoán sai, càng thấp càng tốt.
    """
    metrics = {
        "precision_micro": precision_score(y_test, y_pred, average="micro", zero_division=0),
        "recall_micro": recall_score(y_test, y_pred, average="micro", zero_division=0),
        "f1_micro": f1_score(y_test, y_pred, average="micro", zero_division=0),
        "precision_macro": precision_score(y_test, y_pred, average="macro", zero_division=0),
        "recall_macro": recall_score(y_test, y_pred, average="macro", zero_division=0),
        "f1_macro": f1_score(y_test, y_pred, average="macro", zero_division=0),
        "precision_weighted": precision_score(y_test, y_pred, average="weighted", zero_division=0),
        "recall_weighted": recall_score(y_test, y_pred, average="weighted", zero_division=0),
        "f1_weighted": f1_score(y_test, y_pred, average="weighted", zero_division=0),
        "precision_samples": precision_score(y_test, y_pred, average="samples", zero_division=0),
        "recall_samples": recall_score(y_test, y_pred, average="samples", zero_division=0),
        "f1_samples": f1_score(y_test, y_pred, average="samples", zero_division=0),
        "exact_match_ratio": accuracy_score(y_test, y_pred),
        "hamming_loss": hamming_loss(y_test, y_pred),
    }

    report = classification_report(
        y_test,
        y_pred,
        target_names=label_columns,
        zero_division=0,
        output_dict=True,
    )

    return {
        "summary": {key: round(float(value), 4) for key, value in metrics.items()},
        "per_label_report": report,
    }


def save_artifacts(model: Pipeline, label_columns: list[str], evaluation: dict) -> None:
    dump(model, MODEL_PATH)
    LABELS_PATH.write_text(
        json.dumps(label_columns, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    METRICS_PATH.write_text(
        json.dumps(evaluation, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def main() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")

    training_files = resolve_training_files()
    X, y, label_columns = load_training_data(training_files)

    model = build_model()
    model.fit(X, y)

    if len(X) >= 80:
        X_train, X_test, y_train, y_test = train_test_split(
            X,
            y,
            test_size=0.25,
            random_state=42,
        )
        model.fit(X_train, y_train)
        y_pred = predict_with_threshold_fallback(model, X_test, label_columns)
        evaluation = evaluate_model(y_test.reset_index(drop=True), y_pred, label_columns)
        report_target = (y_test.reset_index(drop=True), y_pred)
    else:
        y_pred = predict_with_threshold_fallback(model, X, label_columns)
        evaluation = evaluate_model(y.reset_index(drop=True), y_pred, label_columns)
        report_target = (y.reset_index(drop=True), y_pred)

    model.fit(X, y)

    print(f"\n===== MULTI-LABEL EVALUATION SUMMARY threshold={PREDICTION_THRESHOLD} fallback={UNCLEAR_LABEL} =====")
    for metric_name, metric_value in evaluation["summary"].items():
        print(f"{metric_name:24s}: {metric_value}")

    print("\n===== PER-LABEL CLASSIFICATION REPORT =====")
    print(
        classification_report(
            report_target[0],
            report_target[1],
            target_names=label_columns,
            zero_division=0,
        )
    )

    save_artifacts(model, label_columns, evaluation)

    print(f"\nSaved model to: {MODEL_PATH}")
    print(f"Saved labels to: {LABELS_PATH}")
    print(f"Saved metrics to: {METRICS_PATH}")


if __name__ == "__main__":
    main()
