import json
import logging
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from joblib import load

from .paths import CHATBOT_DIR, KNOWLEDGE_PATH, LABELS_PATH, MODEL_PATH


DEFAULT_THRESHOLD = 0.25
MAX_SELECTED_LABELS = 1
UNCLEAR_LABEL = "KHÔNG RÕ"
OUT_OF_SCOPE_LABEL = "NGOÀI LỀ"
DANGEROUS_TOPIC_LABEL = "CHỦ ĐỀ NGUY HIỂM"
DIRECT_LOGIST_ANSWER_LABELS = {OUT_OF_SCOPE_LABEL, DANGEROUS_TOPIC_LABEL}
LOG_PATH = CHATBOT_DIR / "status" / "chatbot_process.log"
LOGGER_NAME = "auction_ai.chatbot.logist"


def _logger() -> logging.Logger:
    logger = logging.getLogger(LOGGER_NAME)
    if logger.handlers:
        return logger
    logger.setLevel(logging.INFO)
    logger.propagate = False
    LOG_PATH.parent.mkdir(parents=True, exist_ok=True)
    handler = logging.FileHandler(LOG_PATH, encoding="utf-8")
    handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(name)s - %(message)s"))
    logger.addHandler(handler)
    return logger


SPECIAL_FALLBACK_LABELS = {
    DANGEROUS_TOPIC_LABEL,
    OUT_OF_SCOPE_LABEL,
    UNCLEAR_LABEL,
}


@dataclass(frozen=True)
class IntentPrediction:
    labels: list[str]
    scores: dict[str, float]


def normalize_label(label: str) -> str:
    return " ".join(label.strip().upper().split())


def has_out_of_scope_label(labels: list[str]) -> bool:
    return any(normalize_label(label) == OUT_OF_SCOPE_LABEL for label in labels)


def has_dangerous_topic_label(labels: list[str]) -> bool:
    return any(normalize_label(label) == DANGEROUS_TOPIC_LABEL for label in labels)


def has_direct_logist_answer_label(labels: list[str]) -> bool:
    return any(normalize_label(label) in DIRECT_LOGIST_ANSWER_LABELS for label in labels)


def _fallback_label(scores: dict[str, float], ranked: list[tuple[str, float]]) -> str:
    special_labels = [
        label for label in scores
        if normalize_label(label) in SPECIAL_FALLBACK_LABELS
    ]
    if special_labels:
        return max(special_labels, key=lambda label: scores[label])
    return ranked[0][0]


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
    logger = _logger()
    logger.info("Loading ChatBot Logist model. model_path=%s exists=%s", model_path, model_path.exists())
    if not model_path.exists():
        logger.warning("ChatBot Logist model is missing. Training model now. model_path=%s", model_path)
        train_chatbot_model()
    try:
        model = load(model_path)
    except Exception:
        logger.exception("Failed to load ChatBot Logist model. Retraining once. model_path=%s", model_path)
        train_chatbot_model()
        model = load(model_path)
    return make_model_runtime_compatible(model)


def train_chatbot_model() -> None:
    logger = _logger()
    script_path = Path(__file__).resolve().parents[1] / "training" / "train_model.py"
    cwd = script_path.parents[3]
    command = [sys.executable, str(script_path)]
    logger.info("Training ChatBot Logist model. command=%s cwd=%s script_exists=%s", command, cwd, script_path.exists())
    completed = subprocess.run(
        command,
        cwd=str(cwd),
        check=False,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if completed.returncode != 0:
        logger.error(
            "ChatBot Logist training failed. returncode=%s stdout=%s stderr=%s",
            completed.returncode,
            _truncate(completed.stdout),
            _truncate(completed.stderr),
        )
        completed.check_returncode()
    logger.info(
        "ChatBot Logist training completed. returncode=%s stdout=%s stderr=%s",
        completed.returncode,
        _truncate(completed.stdout),
        _truncate(completed.stderr),
    )


def _truncate(value: str | None, limit: int = 2000) -> str:
    if not value:
        return ""
    value = value.strip()
    if len(value) <= limit:
        return value
    return value[:limit] + "... [truncated]"


def make_model_runtime_compatible(model: Any) -> Any:
    for estimator in iter_estimators(model):
        if estimator.__class__.__name__ == "LogisticRegression" and not hasattr(estimator, "multi_class"):
            estimator.multi_class = "auto"
    return model


def iter_estimators(model: Any):
    yield model

    for _, step in getattr(model, "steps", []) or []:
        yield from iter_estimators(step)

    estimator = getattr(model, "estimator", None)
    if estimator is not None:
        yield from iter_estimators(estimator)

    for estimator in getattr(model, "estimators_", []) or []:
        yield from iter_estimators(estimator)


def load_labels(labels_path: Path = LABELS_PATH) -> list[str]:
    _logger().info("Loading ChatBot labels. labels_path=%s exists=%s", labels_path, labels_path.exists())
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
        selected = [_fallback_label(scores, ranked)]

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

    loaded_default_model = model is None
    model = model or load_chatbot_model()
    labels = labels or load_labels()
    try:
        scores = predict_scores(model, labels, cleaned_message)
    except AttributeError:
        if not loaded_default_model:
            raise
        train_chatbot_model()
        model = load_chatbot_model()
        scores = predict_scores(model, labels, cleaned_message)
    selected_labels = select_labels(scores, message=cleaned_message, threshold=threshold)
    _logger().info("Classified ChatBot message. message_length=%s selected_labels=%s", len(cleaned_message), selected_labels)
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

    if has_dangerous_topic_label(labels):
        return (
            "Mình không thể hỗ trợ nội dung có thể gây hại, vi phạm pháp luật hoặc làm tổn thương "
            "bản thân hay người khác. Nếu bạn hoặc ai đó đang gặp nguy hiểm ngay lúc này, hãy liên hệ "
            "người thân đáng tin cậy hoặc dịch vụ khẩn cấp tại địa phương. Bạn vẫn có thể hỏi mình "
            "về các thao tác trong hệ thống đấu giá."
        )

    if has_out_of_scope_label(labels):
        return (
            "Mình chưa có thông tin phù hợp để trả lời nội dung đó. "
            "Bạn cần hỗ trợ thao tác nào trong hệ thống đấu giá như đăng ký, đăng nhập, "
            "xem phiên, đặt giá hoặc quản lý số dư?"
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
