import sys
from pathlib import Path


AUCTION_AI_DIR = Path(__file__).resolve().parents[3]
if str(AUCTION_AI_DIR) not in sys.path:
    sys.path.insert(0, str(AUCTION_AI_DIR))

from ChatBot.Logist.chatbot.intent_chatbot import classify_message, predict_message


LABELS = ["ĐẶT GIÁ TRONG PHIÊN ĐẤU GIÁ", "ĐĂNG NHẬP HỆ THỐNG"]


class FakeModel:
    def __init__(self, probabilities):
        self.probabilities = probabilities

    def predict_proba(self, messages):
        return [self.probabilities]


def test_selects_highest_probability_label():
    prediction = classify_message(
        "Tôi muốn đặt giá",
        model=FakeModel([0.51, 0.49]),
        labels=LABELS,
    )

    assert prediction.labels == ["ĐẶT GIÁ TRONG PHIÊN ĐẤU GIÁ"]
    assert prediction.scores == {
        "ĐẶT GIÁ TRONG PHIÊN ĐẤU GIÁ": 0.51,
        "ĐĂNG NHẬP HỆ THỐNG": 0.49,
    }


def test_rule_based_answer_uses_selected_label():
    answer = predict_message(
        "Tôi muốn đặt giá",
        model=FakeModel([0.51, 0.49]),
        labels=LABELS,
    )

    assert "đặt giá" in answer.lower()


def main() -> None:
    test_selects_highest_probability_label()
    test_rule_based_answer_uses_selected_label()
    print("Logistic chatbot tests passed.")


if __name__ == "__main__":
    main()
