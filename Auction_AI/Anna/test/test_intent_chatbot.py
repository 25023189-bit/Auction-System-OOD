import sys
from pathlib import Path


ANNA_DIR = Path(__file__).resolve().parents[1]
if str(ANNA_DIR) not in sys.path:
    sys.path.insert(0, str(ANNA_DIR))

from chatbot.intent_chatbot import predict_message


def main() -> None:
    answer = predict_message("Tôi muốn biết số dư là gì và hướng dẫn tôi đặt giá")
    assert "Số dư" in answer
    assert "Để đặt giá" in answer
    print("Intent chatbot smoke test passed.")


if __name__ == "__main__":
    main()
