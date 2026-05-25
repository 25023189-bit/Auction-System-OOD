import os


def _config(name, default):
    return os.getenv(f"AUCTION_CHATBOT_{name}") or os.getenv(name) or default


def _int_config(name, default):
    try:
        return int(_config(name, str(default)))
    except ValueError:
        return default


OLLAMA_URL = _config("OLLAMA_URL", "http://localhost:11434/api/generate")
OLLAMA_MODEL = _config("OLLAMA_MODEL", "llama3")
REQUEST_TIMEOUT_SECONDS = _int_config("REQUEST_TIMEOUT_SECONDS", 30)
