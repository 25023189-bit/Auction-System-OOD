import sys
from pathlib import Path


AUCTION_AI_DIR = Path(__file__).resolve().parents[3]
if str(AUCTION_AI_DIR) not in sys.path:
    sys.path.insert(0, str(AUCTION_AI_DIR))

from ChatBot.Logist.chatbot.intent_chatbot import classify_message, predict_message


LABELS = ["ĐẶT GIÁ TRONG PHIÊN ĐẤU GIÁ", "ĐĂNG NHẬP HỆ THỐNG"]
LABELS_WITH_OUT_OF_SCOPE = [*LABELS, "NGOÀI LỀ"]


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


def test_low_confidence_prediction_uses_out_of_scope_label():
    prediction = classify_message(
        "Bệnh viện ở đâu?",
        model=FakeModel([0.21, 0.19, 0.12]),
        labels=LABELS_WITH_OUT_OF_SCOPE,
    )

    assert prediction.labels == ["NGOÀI LỀ"]


def test_out_of_scope_answer_redirects_to_auction_support():
    answer = predict_message(
        "Thời tiết hôm nay thế nào?",
        model=FakeModel([0.21, 0.19, 0.12]),
        labels=LABELS_WITH_OUT_OF_SCOPE,
    )

    assert "hệ thống đấu giá" in answer.lower()
    assert "thời tiết" not in answer.lower()


def main() -> None:
    test_selects_highest_probability_label()
    test_rule_based_answer_uses_selected_label()
    test_low_confidence_prediction_uses_out_of_scope_label()
    test_out_of_scope_answer_redirects_to_auction_support()
    print("Logistic chatbot tests passed.")


if __name__ == "__main__":
    main()
