import re
import unicodedata

from .config import KNOWLEDGE_PATH
from .intent_classifier import IntentClassifier
from .knowledge_loader import load_documents
from .ollama_client import OllamaClient
from .prompt_builder import build_in_label_prompt, build_out_of_label_prompt


SCOPE_REFUSAL_PATTERNS = (
    "khong thuoc pham vi",
    "ngoai pham vi",
    "khong lien quan den chu de",
    "chu de duoc phep",
    "khong the tra loi cau hoi nay",
    "toi khong the tra loi",
    "yeu cau cua ban khong phu hop",
)

SAFE_BRIDGE_ANSWER = (
    "Mình ghi nhận câu hỏi của bạn. Mình có thể hỗ trợ rõ nhất về các phiên đấu giá, "
    "cách đặt giá, số dư, thời gian còn lại và anti-sniping."
)


class BetaChatbot:
    def __init__(self, client=None, knowledge_path=KNOWLEDGE_PATH):
        self.client = client or OllamaClient()
        self.documents = load_documents(knowledge_path)
        self.classifier = IntentClassifier(self.documents)

    def answer(self, question):
        match = self.classifier.classify(question)

        if not match.is_in_scope:
            prompt = build_out_of_label_prompt(question, self.documents)
            return self._generate_without_scope_refusal(prompt)

        prompt = build_in_label_prompt(question, match.document)
        return self._generate_without_scope_refusal(prompt)

    def _generate_without_scope_refusal(self, prompt):
        answer = self.client.generate(prompt)
        if not self._looks_like_scope_refusal(answer):
            return answer

        return SAFE_BRIDGE_ANSWER

    def _looks_like_scope_refusal(self, answer):
        normalized_answer = self._normalize(answer)
        return any(pattern in normalized_answer for pattern in SCOPE_REFUSAL_PATTERNS)

    def _normalize(self, text):
        standardized = text.replace("đ", "d").replace("Đ", "D")
        without_diacritics = unicodedata.normalize("NFD", standardized)
        without_diacritics = "".join(
            character for character in without_diacritics
            if unicodedata.category(character) != "Mn"
        )
        return re.sub(r"\s+", " ", without_diacritics.lower()).strip()
