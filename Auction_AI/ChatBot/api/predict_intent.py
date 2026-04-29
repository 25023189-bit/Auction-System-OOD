import sys
from pathlib import Path


ANNA_DIR = Path(__file__).resolve().parents[1]
if str(ANNA_DIR) not in sys.path:
    sys.path.insert(0, str(ANNA_DIR))

from chatbot.intent_chatbot import predict_to_file
from chatbot.paths import OUTPUT_PATH


def main() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")

    answer = predict_to_file()
    print(answer)
    print(f"\nSaved output to: {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
