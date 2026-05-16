import sys
from pathlib import Path


CHATBOT_DIR = Path(__file__).resolve().parents[1]
if str(CHATBOT_DIR) not in sys.path:
    sys.path.insert(0, str(CHATBOT_DIR))

from model.beta_chatbot import BetaChatbot


class RecordingAnswerClient:
    def __init__(self):
        self.prompts = []

    def generate(self, prompt):
        self.prompts.append(prompt)
        return "Bạn có thể xem danh sách các phiên đấu giá đang mở ở lobby."


class RecordingRefusalClient:
    def __init__(self):
        self.prompts = []

    def generate(self, prompt):
        self.prompts.append(prompt)
        return "Câu hỏi này không thuộc phạm vi trả lời."


def test_in_label_prompt_is_used_once():
    client = RecordingAnswerClient()
    chatbot = BetaChatbot(client=client)

    answer = chatbot.answer("Tôi muốn xem danh sách phiên đấu giá")

    assert len(client.prompts) == 1
    assert "Câu hỏi có trong nhãn" in client.prompts[0]
    assert answer == "Bạn có thể xem danh sách các phiên đấu giá đang mở ở lobby."


def test_out_of_label_prompt_is_used_once():
    client = RecordingAnswerClient()
    chatbot = BetaChatbot(client=client)

    answer = chatbot.answer("Công thức nấu phở bò là gì?")

    assert len(client.prompts) == 1
    assert "Câu hỏi không có trong nhãn" in client.prompts[0]
    assert answer == "Bạn có thể xem danh sách các phiên đấu giá đang mở ở lobby."


def test_scope_refusal_does_not_trigger_third_prompt():
    client = RecordingRefusalClient()
    chatbot = BetaChatbot(client=client)

    answer = chatbot.answer("Câu này không nằm trong nhãn nào cả")

    assert len(client.prompts) == 1
    assert "không thuộc phạm vi" not in answer.lower()
    assert "phiên đấu giá" in answer


def main():
    test_in_label_prompt_is_used_once()
    test_out_of_label_prompt_is_used_once()
    test_scope_refusal_does_not_trigger_third_prompt()
    print("LLM chatbot two-prompt tests passed.")


if __name__ == "__main__":
    main()
