from pathlib import Path


CHATBOT_DIR = Path(__file__).resolve().parents[1]
DATA_DIR = CHATBOT_DIR / "data"
MODEL_DIR = CHATBOT_DIR / "models"

TRAIN_DATA_PATH = DATA_DIR / "train_data.csv"
INPUT_PATH = DATA_DIR / "input.json"
OUTPUT_PATH = DATA_DIR / "output.json"

MODEL_PATH = MODEL_DIR / "chatbot_intent_model.joblib"
LABELS_PATH = MODEL_DIR / "labels.json"
METRICS_PATH = MODEL_DIR / "metrics.json"
