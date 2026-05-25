import json
from pathlib import Path
import time

import joblib
import pandas as pd
import matplotlib.pyplot as plt

from sklearn.compose import ColumnTransformer
from sklearn.feature_extraction.text import CountVectorizer,TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    roc_auc_score,
    confusion_matrix,
    classification_report,
    ConfusionMatrixDisplay,
    roc_curve,
    auc
)
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler


# =========================
# PATH
# =========================
BASE_DIR = Path(__file__).resolve().parent
DATA_PATH = BASE_DIR / "Training_Data.csv"
MODEL_PATH = BASE_DIR / "auction_model.pkl"
METRICS_PATH = BASE_DIR / "metrics.json"
DECISION_THRESHOLD = 0.75


def main():
    # =========================
    # 1. LOAD DATA
    # =========================
    df = pd.read_csv(DATA_PATH)
    df["title_length"] = df["title"].astype(str).str.len()
    df["desc_length"] = df["description"].astype(str).str.len()
    df["minimum_join_ratio"] = df["minimum_join_amount"] / df["start_price"]
    df["bid_step_ratio"] = df["bid_step"] / df["start_price"]

    X = df.drop(columns=["auto_approve", "review_status", "description_style", "targeted_risk_type"], errors="ignore")
    y = df["auto_approve"]

    # =========================
    # 2. FEATURE GROUP
    # =========================
    numeric_features = [
        "seller_rating",
        "seller_completed_rating",
        "seller_cancel_rate",
        "title_length",
        "desc_length",
        "minimum_join_amount",
        "minimum_join_ratio",
        "bid_step",
        "bid_step_ratio",
        "start_price_log",
        "duration_minutes",
        "extension_seconds",
        "start_hour",
        "day_of_week",
        "is_weekend",
    ]

    st = time.time()

    title_feature = "title"
    desc_feature = "description"
    organization_feature = ["organization"]

    # =========================
    # 3. PREPROCESSOR
    # =========================
    preprocessor = ColumnTransformer(
        transformers=[
            ("title_bow", TfidfVectorizer(), title_feature),
            ("desc_bow", TfidfVectorizer(ngram_range=(1, 2),max_features=5000,min_df=2,sublinear_tf=True), desc_feature),
            ("organization_onehot", OneHotEncoder(handle_unknown="ignore"), organization_feature),
            ("num", StandardScaler(), numeric_features),
        ],
        transformer_weights={
            "title_bow": 0.7,
            "desc_bow": 1.0,
            "organization_onehot": 1.2,
        },
        remainder="drop"
    )

    # =========================
    # 4. MODEL PIPELINE
    # =========================
    model = Pipeline(
        steps=[
            ("preprocessor", preprocessor),
            ("classifier", LogisticRegression(
                max_iter=2000,
                class_weight="balanced"
            )),
        ]
    )

    # =========================
    # 5. TRAIN / TEST SPLIT
    # =========================
    X_train, X_test, y_train, y_test = train_test_split(
        X, y,
        test_size=0.2,
        random_state=42,
        stratify=y
    )

    # =========================
    # 6. TRAIN
    # =========================
    model.fit(X_train, y_train)

    # =========================
    # 7. PREDICT
    # =========================
    y_prob = model.predict_proba(X_test)[:, 1]
    y_pred = (y_prob >= DECISION_THRESHOLD).astype(int)

    ed = time.time()

    # =========================
    # 8. METRICS
    # =========================
    metrics = {
        "accuracy": float(accuracy_score(y_test, y_pred)),
        "precision": float(precision_score(y_test, y_pred, zero_division=0)),
        "recall": float(recall_score(y_test, y_pred, zero_division=0)),
        "f1": float(f1_score(y_test, y_pred, zero_division=0)),
        "roc_auc": float(roc_auc_score(y_test, y_prob)),
        "decision_threshold": DECISION_THRESHOLD,
        "confusion_matrix": confusion_matrix(y_test, y_pred).tolist(),
        "classification_report": classification_report(y_test, y_pred, zero_division=0),
    }

    print("\n=== METRICS ===")
    print(json.dumps(metrics, indent=2))

    # =========================
    # 9. SAVE MODEL
    # =========================
    joblib.dump(model, MODEL_PATH)

    with open(METRICS_PATH, "w", encoding="utf-8") as f:
        json.dump(metrics, f, indent=2, ensure_ascii=False)

    print(f"\nModel saved: {MODEL_PATH}")
    print(f"Metrics saved: {METRICS_PATH}")
    print("Time run:"+str(round(ed-st,2))+"s")

    # =========================
    # 10. VISUALIZATION
    # =========================

    # --- 10.1 So sánh y_test vs y_pred ---
    """plt.figure(figsize=(10, 4))
    plt.plot(y_test.values, label="Thực tế", marker='o')
    plt.plot(y_pred, label="Dự đoán", marker='x')
    plt.title("Thực tế vs Dự đoán")
    plt.legend()
    plt.grid()
    plt.show()"""

    """# --- 10.2 Scatter probability ---
    plt.figure(figsize=(10, 4))
    plt.scatter(range(len(y_prob)), y_prob, c=y_test, cmap="bwr")
    plt.axhline(y=0.5, linestyle='--', color='black')
    plt.title("Xác suất dự đoán")
    plt.ylabel("Probability")
    plt.xlabel("Sample index")
    plt.colorbar(label="True label")
    plt.grid()
    plt.show()"""

    # --- 10.3 Histogram ---
    """plt.figure(figsize=(6, 4))
    plt.hist(y_prob, bins=20)
    plt.title("Phân bố xác suất")
    plt.xlabel("Probability")
    plt.ylabel("Count")
    plt.grid()
    plt.show()"""

    """# --- 10.4 Confusion Matrix ---
    ConfusionMatrixDisplay.from_predictions(y_test, y_pred)
    plt.title("Confusion Matrix")
    plt.show()

    # --- 10.5 ROC Curve ---
    fpr, tpr, _ = roc_curve(y_test, y_prob)
    roc_auc = auc(fpr, tpr)

    plt.figure()
    plt.plot(fpr, tpr, label=f"AUC = {roc_auc:.2f}")
    plt.plot([0, 1], [0, 1], linestyle='--')
    plt.xlabel("FPR")
    plt.ylabel("TPR")
    plt.title("ROC Curve")
    plt.legend()
    plt.grid()
    plt.show()"""


if __name__ == "__main__":
    main()
