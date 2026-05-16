from pathlib import Path


BASE_DIR = Path(__file__).resolve().parents[1]

OLLAMA_URL = "http://localhost:11434/api/generate"
OLLAMA_MODEL = "llama3"
REQUEST_TIMEOUT_SECONDS = 60
KNOWLEDGE_PATH = BASE_DIR / "knowledge" / "label_constraints.json"
REDIRECT_TOPIC = "các phiên đấu giá"
