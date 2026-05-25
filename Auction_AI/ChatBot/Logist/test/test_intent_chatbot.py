import sys
from pathlib import Path


AUCTION_AI_DIR = Path(__file__).resolve().parents[3]
if str(AUCTION_AI_DIR) not in sys.path:
    sys.path.insert(0, str(AUCTION_AI_DIR))

from ChatBot.Logist.chatbot.intent_chatbot import classify_message, predict_message


LABELS = ["ĐẶT GIÁ TRONG PHIÊN ĐẤU GIÁ", "ĐĂNG NHẬP HỆ THỐNG"]
LABELS_WITH_OUT_OF_SCOPE = [*LABELS, "NGOÀI LỀ"]
LABELS_WITH_DANGEROUS_TOPIC = [*LABELS, "CHỦ ĐỀ NGUY HIỂM", "NGOÀI LỀ"]


class FakeModel:
    def __init__(self, probabilities):
        self.probabilities = probabilities

    def predict_proba(self, messages):
        return [self.probabilities]


def test_selects_all_labels_at_or_above_threshold():
    prediction = classify_message(
        "Tôi muốn đặt giá và đăng nhập",
        model=FakeModel([0.51, 0.50]),
        labels=LABELS,
    )

    assert prediction.labels == ["ĐẶT GIÁ TRONG PHIÊN ĐẤU GIÁ", "ĐĂNG NHẬP HỆ THỐNG"]
    assert prediction.scores == {
        "ĐẶT GIÁ TRONG PHIÊN ĐẤU GIÁ": 0.51,
        "ĐĂNG NHẬP HỆ THỐNG": 0.5,
    }


def test_rule_based_answer_uses_selected_label():
    answer = predict_message(
        "Tôi muốn đặt giá",
        model=FakeModel([0.51, 0.49]),
        labels=LABELS,
    )

    assert "đặt giá" in answer.lower()


def test_low_confidence_prediction_uses_unclear_label():
    prediction = classify_message(
        "Bệnh viện ở đâu?",
        model=FakeModel([0.21, 0.19, 0.12]),
        labels=LABELS_WITH_OUT_OF_SCOPE,
    )

    assert prediction.labels == ["KHÔNG RÕ"]


def test_out_of_scope_answer_redirects_to_auction_support():
    answer = predict_message(
        "Thời tiết hôm nay thế nào?",
        model=FakeModel([0.21, 0.19, 0.12]),
        labels=LABELS_WITH_OUT_OF_SCOPE,
    )

    assert "chưa đủ thông tin" in answer.lower()


def test_dangerous_topic_answer_uses_safety_redirect():
    answer = predict_message(
        "Tôi muốn làm hại người khác",
        model=FakeModel([0.08, 0.06, 0.74, 0.12]),
        labels=LABELS_WITH_DANGEROUS_TOPIC,
    )

    assert "không thể hỗ trợ" in answer.lower()
    assert "gây hại" in answer.lower()
    assert "hệ thống đấu giá" in answer.lower()


def main() -> None:
    test_selects_all_labels_at_or_above_threshold()
    test_rule_based_answer_uses_selected_label()
    test_low_confidence_prediction_uses_unclear_label()
    test_out_of_scope_answer_redirects_to_auction_support()
    test_dangerous_topic_answer_uses_safety_redirect()
    print("Logistic chatbot tests passed.")


if __name__ == "__main__":
    main()
