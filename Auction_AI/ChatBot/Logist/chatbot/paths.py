from pathlib import Path


LOGIST_DIR = Path(__file__).resolve().parents[1]
CHATBOT_DIR = LOGIST_DIR.parent
DATA_DIR = LOGIST_DIR / "data"
MODEL_DIR = LOGIST_DIR / "models"
LLM_DIR = CHATBOT_DIR / "LLM"

GENERATED_DATA_DIR = DATA_DIR / "generated"
TRAIN_DATA_MANIFEST_PATH = DATA_DIR / "train_data_manifest.json"
KNOWLEDGE_PATH = LLM_DIR / "knowledge" / "label_constraints.json"

MODEL_PATH = MODEL_DIR / "chatbot_intent_model.joblib"
LABELS_PATH = MODEL_DIR / "labels.json"
METRICS_PATH = MODEL_DIR / "metrics.json"
