import sys
from pathlib import Path


AUCTION_AI_DIR = Path(__file__).resolve().parents[3]
if str(AUCTION_AI_DIR) not in sys.path:
    sys.path.insert(0, str(AUCTION_AI_DIR))

from ChatBot.LLM.model.response_generator import generate_llm_answer


LABEL = "ĐẶT GIÁ TRONG PHIÊN ĐẤU GIÁ"
QUESTION = "Tôi muốn đặt giá"
DOCUMENTS = {
    LABEL: {
        "title": LABEL,
        "content": "Bidder có thể đặt giá khi phiên đấu giá đang diễn ra.",
    }
}


class SuccessfulClient:
    def __init__(self):
        self.prompts = []

    def generate(self, prompt):
        self.prompts.append(prompt)
        return "Bạn có thể nhập giá hợp lệ khi phiên đang diễn ra."


class EmptyClient:
    def generate(self, prompt):
        return "  "


class RefusalClient:
    def generate(self, prompt):
        return "Tôi không thể trả lời câu hỏi này."


def test_generate_llm_answer_builds_prompt_and_returns_answer():
    client = SuccessfulClient()

    answer = generate_llm_answer(QUESTION, [LABEL], DOCUMENTS, client=client)

    assert answer == "Bạn có thể nhập giá hợp lệ khi phiên đang diễn ra."
    assert len(client.prompts) == 1
    assert f"Nhãn đã chọn: {LABEL}" in client.prompts[0]
    assert QUESTION in client.prompts[0]


def test_generate_llm_answer_raises_for_empty_answer():
    try:
        generate_llm_answer(QUESTION, [LABEL], DOCUMENTS, client=EmptyClient())
    except RuntimeError as exc:
        assert str(exc) == "LLM answer is empty or out-of-scope."
    else:
        raise AssertionError("Expected RuntimeError for empty LLM answer.")


def test_generate_llm_answer_raises_for_scope_refusal():
    try:
        generate_llm_answer(QUESTION, [LABEL], DOCUMENTS, client=RefusalClient())
    except RuntimeError as exc:
        assert str(exc) == "LLM answer is empty or out-of-scope."
    else:
        raise AssertionError("Expected RuntimeError for out-of-scope LLM answer.")


def main():
    test_generate_llm_answer_builds_prompt_and_returns_answer()
    test_generate_llm_answer_raises_for_empty_answer()
    test_generate_llm_answer_raises_for_scope_refusal()
    print("LLM response generator logic tests passed.")


if __name__ == "__main__":
    main()
