import unicodedata

from .ollama_client import OllamaClient
from .prompt_builder import build_chatbot_prompt


SCOPE_REFUSAL_PATTERNS = (
    "khong thuoc pham vi",
    "ngoai pham vi",
    "khong lien quan",
    "toi khong the tra loi",
)


def generate_llm_answer(question, labels, documents, client=None):
    prompt = build_chatbot_prompt(question, labels, documents)
    answer = (client or OllamaClient()).generate(prompt)
    if is_bad_llm_answer(answer):
        raise RuntimeError("LLM answer is empty or out-of-scope.")
    return answer


def is_bad_llm_answer(answer):
    if not answer or not answer.strip():
        return True
    normalized = normalize_for_guard(answer)
    return any(pattern in normalized for pattern in SCOPE_REFUSAL_PATTERNS)


def normalize_for_guard(text):
    standardized = text.replace("đ", "d").replace("Đ", "D")
    decomposed = unicodedata.normalize("NFD", standardized)
    without_accents = "".join(
        character for character in decomposed
        if unicodedata.category(character) != "Mn"
    )
    return " ".join(without_accents.lower().split())
