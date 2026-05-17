import json
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from joblib import load

from .paths import KNOWLEDGE_PATH, LABELS_PATH, MODEL_PATH


DEFAULT_THRESHOLD = 0.25
MAX_SELECTED_LABELS = 1
UNCLEAR_LABEL = "KHÔNG RÕ"


@dataclass(frozen=True)
class IntentPrediction:
    labels: list[str]
    scores: dict[str, float]


def normalize_label(label: str) -> str:
    return " ".join(label.strip().upper().split())


def load_documents(path: Path = KNOWLEDGE_PATH) -> list[dict[str, str]]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    documents = payload.get("documents", [])
    result = []

    for document in documents:
        title = str(document["title"]).strip()
        content = str(document["content"]).strip()
        result.append(
            {
                "label": normalize_label(title),
                "title": title,
                "content": content,
            }
        )

    return result


def documents_by_label(path: Path = KNOWLEDGE_PATH) -> dict[str, dict[str, str]]:
    return {document["label"]: document for document in load_documents(path)}


def load_chatbot_model(model_path: Path = MODEL_PATH):
    if not model_path.exists():
        raise FileNotFoundError(
            f"Chua co model tai {model_path}. Hay chay: python Auction_AI\\ChatBot\\Logist\\training\\train_model.py"
        )
    return load(model_path)


def load_labels(labels_path: Path = LABELS_PATH) -> list[str]:
    return json.loads(labels_path.read_text(encoding="utf-8"))


def predict_scores(model: Any, labels: list[str], message: str) -> dict[str, float]:
    probabilities = model.predict_proba([message])[0]
    return {
        label: round(float(probability), 6)
        for label, probability in zip(labels, probabilities)
    }


def select_labels(
    scores: dict[str, float],
    message: str = "",
    threshold: float = DEFAULT_THRESHOLD,
    max_labels: int = MAX_SELECTED_LABELS,
) -> list[str]:
    ranked = sorted(scores.items(), key=lambda item: item[1], reverse=True)
    selected = [
        label for label, score in ranked
        if score >= threshold
    ]

    if not selected and ranked:
        selected = [ranked[0][0]]

    return selected[:max_labels]


def classify_message(
    message: str,
    model: Any | None = None,
    labels: list[str] | None = None,
    threshold: float = DEFAULT_THRESHOLD,
) -> IntentPrediction:
    cleaned_message = message.strip()
    if not cleaned_message:
        return IntentPrediction([], {})

    model = model or load_chatbot_model()
    labels = labels or load_labels()
    scores = predict_scores(model, labels, cleaned_message)
    selected_labels = select_labels(scores, message=cleaned_message, threshold=threshold)
    return IntentPrediction(selected_labels, scores)


def build_rule_based_answer(
    message: str,
    labels: list[str],
    documents: dict[str, dict[str, str]] | None = None,
) -> str:
    documents = documents or documents_by_label()

    if not message.strip():
        return "Bạn hãy nhập câu hỏi về hệ thống đấu giá để mình hỗ trợ đúng nội dung."

    if not labels:
        return (
            "Mình chưa xác định được chủ đề phù hợp. Bạn có thể hỏi về đăng ký, đăng nhập, "
            "tìm phiên đấu giá, tham gia phòng, đặt giá, số dư, thời gian còn lại hoặc anti-sniping."
        )

    if any(normalize_label(label) == UNCLEAR_LABEL for label in labels):
        return (
            "Mình chưa đủ thông tin để xác định chính xác yêu cầu. "
            "Bạn có thể mô tả rõ hơn màn hình, thao tác hoặc lỗi đang gặp trong hệ thống đấu giá không?"
        )

    answers = []
    for label in labels:
        document = documents.get(label)
        if not document:
            continue

        content = document["content"].strip()
        answers.append(f"Về {document['title']}: {content}")

    if answers:
        return "\n".join(answers)

    return (
        "Mình đã nhận câu hỏi của bạn nhưng chưa có mẫu trả lời phù hợp. "
        "Bạn có thể hỏi lại rõ hơn về thao tác trong hệ thống đấu giá."
    )


def predict_message(
    message: str,
    model: Any | None = None,
    labels: list[str] | None = None,
    threshold: float = DEFAULT_THRESHOLD,
) -> str:
    prediction = classify_message(message, model=model, labels=labels, threshold=threshold)
    return build_rule_based_answer(message, prediction.labels)
