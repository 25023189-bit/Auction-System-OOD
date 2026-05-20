import json
import traceback
from datetime import datetime
from pathlib import Path

from ChatBot.Logist.chatbot.intent_chatbot import (
    build_rule_based_answer,
    classify_message,
    documents_by_label,
    has_out_of_scope_label,
    load_chatbot_model,
    load_labels,
)
from ChatBot.LLM.model.response_generator import generate_llm_answer

from .paths import ERROR_INFO_PATH, INFORMATION_PATH, INPUT_PATH, OUTPUT_PATH


def handle_question(
    question,
    input_path=INPUT_PATH,
    output_path=OUTPUT_PATH,
    error_info_path=ERROR_INFO_PATH,
    information_path=INFORMATION_PATH,
    llm_client=None,
):
    try:
        normalized_question = _normalize_question(question)
        write_input_file(input_path, normalized_question)
        result, information = _call_llm(normalized_question, llm_client=llm_client)
        write_output_file(output_path, result)
        write_information_file(information_path, information)
        clear_error_info(error_info_path)
        return result
    except Exception as exc:
        return handle_error(
            exc,
            question,
            output_path=output_path,
            error_info_path=error_info_path,
            information_path=information_path,
        )


def run_from_file(
    input_path=INPUT_PATH,
    output_path=OUTPUT_PATH,
    error_info_path=ERROR_INFO_PATH,
    information_path=INFORMATION_PATH,
    llm_client=None,
):
    try:
        question = read_input_file(input_path)
        return handle_question(
            question,
            input_path=input_path,
            output_path=output_path,
            error_info_path=error_info_path,
            information_path=information_path,
            llm_client=llm_client,
        )
    except Exception as exc:
        return handle_error(
            exc,
            "",
            output_path=output_path,
            error_info_path=error_info_path,
            information_path=information_path,
        )


def read_input_file(path=INPUT_PATH):
    input_path = Path(path)
    payload = json.loads(input_path.read_text(encoding="utf-8"))
    if not isinstance(payload, dict):
        raise ValueError("File input.json phải là JSON object.")

    question = payload.get("question") or payload.get("message")
    return _normalize_question(question)


def write_input_file(path, question):
    input_path = Path(path)
    input_path.parent.mkdir(parents=True, exist_ok=True)
    input_path.write_text(
        json.dumps({"question": question}, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def write_output_file(path, payload):
    output_path = Path(path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def write_information_file(path, payload):
    information_path = Path(path)
    information_path.parent.mkdir(parents=True, exist_ok=True)
    information_path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def clear_error_info(path=ERROR_INFO_PATH):
    error_info_path = Path(path)
    if error_info_path.exists():
        error_info_path.unlink()


def handle_error(
    exc,
    question,
    output_path=OUTPUT_PATH,
    error_info_path=ERROR_INFO_PATH,
    information_path=INFORMATION_PATH,
):
    normalized_question = question.strip() if isinstance(question, str) else ""
    logist_context = (
        forward_error_to_logist(normalized_question, exc)
        if normalized_question
        else {
            "status": "skipped",
            "message": "Không có câu hỏi hợp lệ để chuyển sang Logist.",
        }
    )
    has_logist_fallback = logist_context.get("status") == "forwarded"
    information = build_information_payload(
        normalized_question,
        labels=logist_context.get("labels", []),
        scores=logist_context.get("scores", {}),
        answer_source="ERROR",
        status="error",
        fallback_answer_source="Logist" if has_logist_fallback else None,
        fallback_answer=logist_context.get("answer") if has_logist_fallback else None,
        error={
            "type": type(exc).__name__,
            "message": _user_error_message(exc),
        },
    )
    write_error_info_file(error_info_path, exc, normalized_question, logist_context)
    write_information_file(information_path, information)

    if has_logist_fallback:
        fallback_output = build_fallback_output(logist_context["answer"])
        write_output_file(output_path, fallback_output)
        return fallback_output

    error_output = build_error_output(exc)
    write_output_file(output_path, error_output)
    return error_output


def _call_llm(question, llm_client=None):
    model = load_chatbot_model()
    labels = load_labels()
    documents = documents_by_label()
    prediction = classify_message(question, model=model, labels=labels)

    if has_out_of_scope_label(prediction.labels):
        answer = build_rule_based_answer(question, prediction.labels, documents)
        information = build_information_payload(
            question,
            labels=prediction.labels,
            scores=prediction.scores,
            answer_source="Logist",
            status="success",
        )
        return {"answer": answer}, information

    answer = generate_llm_answer(
        question,
        prediction.labels,
        documents,
        client=llm_client,
    )
    information = build_information_payload(
        question,
        labels=prediction.labels,
        scores=prediction.scores,
        answer_source="LLM",
        status="success",
    )
    return {"answer": answer}, information


def forward_error_to_logist(question, exc):
    try:
        model = load_chatbot_model()
        labels = load_labels()
        documents = documents_by_label()
        prediction = classify_message(question, model=model, labels=labels)
        answer = build_rule_based_answer(question, prediction.labels, documents)
        return {
            "status": "forwarded",
            "answer": answer,
            "labels": prediction.labels,
            "scores": prediction.scores,
        }
    except Exception as logist_exc:
        return {
            "status": "failed",
            "error_type": type(logist_exc).__name__,
            "message": str(logist_exc),
            "original_error_type": type(exc).__name__,
        }


def build_error_output(exc):
    return {
        "status": "error",
        "message": _user_error_message(exc),
        "timestamp": _timestamp(),
    }


def build_fallback_output(answer):
    return {
        "status": "fallback",
        "answer_source": "Logist",
        "answer": answer,
        "timestamp": _timestamp(),
    }


def build_information_payload(
    question,
    labels=None,
    scores=None,
    answer_source="LLM",
    status="success",
    fallback_answer_source=None,
    fallback_answer=None,
    error=None,
):
    selected_labels = list(labels or [])
    all_scores = dict(scores or {})
    selected_set = set(selected_labels)
    selected_scores = {
        label: all_scores[label]
        for label in selected_labels
        if label in all_scores
    }
    remaining_scores = {
        label: score
        for label, score in all_scores.items()
        if label not in selected_set
    }

    payload = {
        "status": status,
        "timestamp": _timestamp(),
        "question": question,
        "prediction_model": "Logist",
        "answer_source": answer_source,
        "llm_used_for_answer": answer_source == "LLM",
        "logist_used_for_answer": answer_source == "Logist",
        "selected_labels": selected_labels,
        "selected_scores": selected_scores,
        "remaining_scores": remaining_scores,
        "all_scores": all_scores,
    }
    if fallback_answer_source:
        payload["fallback_answer_source"] = fallback_answer_source
    if fallback_answer:
        payload["fallback_answer"] = fallback_answer
    if error:
        payload["error"] = error
    return payload


def write_error_info_file(path, exc, question, logist_context):
    error_info_path = Path(path)
    error_info_path.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "status": "error",
        "timestamp": _timestamp(),
        "question": question,
        "error_type": type(exc).__name__,
        "message": str(exc),
        "traceback": traceback.format_exception(type(exc), exc, exc.__traceback__),
        "logist": logist_context,
    }
    error_info_path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )


def _normalize_question(question):
    if not isinstance(question, str) or not question.strip():
        raise ValueError("Câu hỏi không được rỗng.")
    return question.strip()


def _timestamp():
    return datetime.now().isoformat(timespec="seconds")


def _user_error_message(exc):
    message = str(exc).strip()
    if message:
        return message
    return "Không thể kết nối tới LLM API."
