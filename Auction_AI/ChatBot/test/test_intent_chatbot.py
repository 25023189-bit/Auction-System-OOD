import sys
from pathlib import Path


CHATBOT_DIR = Path(__file__).resolve().parents[1]
if str(CHATBOT_DIR) not in sys.path:
    sys.path.insert(0, str(CHATBOT_DIR))

from chatbot.intent_chatbot import predict_message


def main() -> None:
    answer = predict_message("Xin chào tôi muốn biết số dư là gì và hướng dẫn tôi đặt giá")
    assert "Xin chào" in answer
    assert "Số dư" in answer
    assert "Để đặt giá" in answer
    print("Intent chatbot smoke test passed.")


if __name__ == "__main__":
    main()
