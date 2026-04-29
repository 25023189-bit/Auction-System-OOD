import json
import sys
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


ANNA_DIR = Path(__file__).resolve().parents[1]
if str(ANNA_DIR) not in sys.path:
    sys.path.insert(0, str(ANNA_DIR))

from chatbot.paths import LABELS_PATH, METRICS_PATH, MODEL_PATH, TRAIN_DATA_PATH


TEXT_COLUMN = "text"


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


def load_training_data(data_path: Path) -> tuple[pd.Series, pd.DataFrame, list[str]]:
    df = pd.read_csv(data_path)

    if TEXT_COLUMN not in df.columns:
        raise ValueError(f"CSV phải có cột '{TEXT_COLUMN}'")

    label_columns = [column for column in df.columns if column != TEXT_COLUMN]
    if not label_columns:
        raise ValueError("CSV phải có ít nhất 1 cột label")

    X = df[TEXT_COLUMN].astype(str)
    y = df[label_columns].astype(int)
    return X, y, label_columns


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

    X, y, label_columns = load_training_data(TRAIN_DATA_PATH)

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.25,
        random_state=42,
    )

    model = build_model()
    model.fit(X_train, y_train)

    y_pred = model.predict(X_test)
    evaluation = evaluate_model(y_test, y_pred, label_columns)

    print("\n===== MULTI-LABEL EVALUATION SUMMARY =====")
    for metric_name, metric_value in evaluation["summary"].items():
        print(f"{metric_name:24s}: {metric_value}")

    print("\n===== PER-LABEL CLASSIFICATION REPORT =====")
    print(
        classification_report(
            y_test,
            y_pred,
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
