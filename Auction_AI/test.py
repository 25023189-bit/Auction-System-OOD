import json
from pathlib import Path

import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, confusion_matrix, precision_score, recall_score, f1_score, accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

BASE_DIR = Path(__file__).resolve().parent
DATA_PATH = BASE_DIR / "auction_dataset.csv"
MODEL_PATH = BASE_DIR / "auction_logreg_bow.pkl"
METRICS_PATH = BASE_DIR / "metrics.json"

df = pd.read_csv(DATA_PATH)

X = df.drop(columns=["auto_approve"])
y = df["auto_approve"]

numeric_features = [
        "seller_rating",
        "seller_completed_auctions",
        "seller_cancel_rate",
        "title_length",
        "desc_length",
        "num_positive_keywords",
        "num_negative_keywords",
        "start_price_log",
        "duration_minutes",
        "extension_seconds",
        "start_hour",
        "day_of_week",
        "is_weekend",
]

title_feature = "title"
desc_feature = "description"

preprocessor = ColumnTransformer(
    transformers=[
        ("title_bow", CountVectorizer(), title_feature),
        ("desc_bow", CountVectorizer(max_features=1500), desc_feature),
        ("num", StandardScaler(), numeric_features),
        ],
    remainder="drop",
    sparse_threshold=0.3,
)
print(preprocessor)