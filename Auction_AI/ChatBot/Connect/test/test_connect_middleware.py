import sys
from contextlib import contextmanager
from dataclasses import dataclass
from pathlib import Path


AUCTION_AI_DIR = Path(__file__).resolve().parents[3]
if str(AUCTION_AI_DIR) not in sys.path:
    sys.path.insert(0, str(AUCTION_AI_DIR))

from ChatBot.Connect import middleware


QUESTION = "Tôi muốn đặt giá trong phiên đấu giá"
LABEL = "ĐẶT GIÁ TRONG PHIÊN ĐẤU GIÁ"
OTHER_LABEL = "XEM DANH SÁCH PHIÊN ĐẤU GIÁ"
OUT_OF_SCOPE_LABEL = "NGOÀI LỀ"


@dataclass
class Prediction:
    labels: list[str]
    scores: dict[str, float]


@contextmanager
def patched(target, name, replacement):
    original = getattr(target, name)
    setattr(target, name, replacement)
    try:
        yield
    finally:
        setattr(target, name, original)


def test_handle_question_success_transitions_to_llm_and_clears_error_state():
    calls = []
    client = object()

    with patched(middleware, "write_input_file", lambda path, question: calls.append(("input", question))), \
        patched(middleware, "_call_llm", lambda question, llm_client=None: calls.append(("llm", question, llm_client)) or ({"answer": "ok"}, {"answer_source": "LLM"})), \
        patched(middleware, "write_output_file", lambda path, payload: calls.append(("output", payload))), \
        patched(middleware, "write_information_file", lambda path, payload: calls.append(("information", payload))), \
        patched(middleware, "clear_error_info", lambda path: calls.append(("clear_error", path))):

        result = middleware.handle_question(
            "  " + QUESTION + "  ",
            input_path="input-path",
            output_path="output-path",
            error_info_path="error-path",
            llm_client=client,
        )

    assert result == {"answer": "ok"}
    assert calls == [
        ("input", QUESTION),
        ("llm", QUESTION, client),
        ("output", {"answer": "ok"}),
        ("information", {"answer_source": "LLM"}),
        ("clear_error", "error-path"),
    ]


def test_handle_question_llm_error_forwards_context_to_logist():
    calls = []

    def raise_llm_error(question, llm_client=None):
        calls.append(("llm", question))
        raise RuntimeError("Không thể kết nối tới LLM API.")

    def forward_error(question, exc):
        calls.append(("logist", question, type(exc).__name__))
        return {
            "status": "forwarded",
            "answer": "fallback",
            "labels": [LABEL],
            "scores": {LABEL: 0.9, OTHER_LABEL: 0.1},
        }

    def capture_error_info(path, exc, question, logist_context):
        calls.append(("error_info", question, logist_context["status"], logist_context["labels"]))

    with patched(middleware, "write_input_file", lambda path, question: calls.append(("input", question))), \
        patched(middleware, "_call_llm", raise_llm_error), \
        patched(middleware, "forward_error_to_logist", forward_error), \
        patched(middleware, "write_error_info_file", capture_error_info), \
        patched(middleware, "write_information_file", lambda path, payload: calls.append(("information", payload["answer_source"], payload["fallback_answer_source"], payload["selected_labels"], payload["remaining_scores"]))), \
        patched(middleware, "write_output_file", lambda path, payload: calls.append(("output", payload["status"], payload.get("answer") or payload.get("message")))):

        result = middleware.handle_question(
            QUESTION,
            input_path="input-path",
            output_path="output-path",
            error_info_path="error-path",
        )

    assert result["status"] == "fallback"
    assert result["answer"] == "fallback"
    assert calls == [
        ("input", QUESTION),
        ("llm", QUESTION),
        ("logist", QUESTION, "RuntimeError"),
        ("error_info", QUESTION, "forwarded", [LABEL]),
        ("information", "ERROR", "Logist", [LABEL], {OTHER_LABEL: 0.1}),
        ("output", "fallback", "fallback"),
    ]


def test_handle_question_invalid_question_skips_logist_forwarding():
    calls = []

    def fail_if_forwarded(question, exc):
        raise AssertionError("Invalid question must not be forwarded to Logist.")

    def capture_error_info(path, exc, question, logist_context):
        calls.append(("error_info", type(exc).__name__, question, logist_context["status"]))

    with patched(middleware, "forward_error_to_logist", fail_if_forwarded), \
        patched(middleware, "write_error_info_file", capture_error_info), \
        patched(middleware, "write_information_file", lambda path, payload: calls.append(("information", payload["answer_source"], payload["selected_labels"]))), \
        patched(middleware, "write_output_file", lambda path, payload: calls.append(("output", payload["status"]))):

        result = middleware.handle_question(" ")

    assert result["status"] == "error"
    assert calls == [
        ("error_info", "ValueError", "", "skipped"),
        ("information", "ERROR", []),
        ("output", "error"),
    ]


def test_call_llm_uses_logist_prediction_before_generating_answer():
    calls = []

    with patched(middleware, "load_chatbot_model", lambda: "model"), \
        patched(middleware, "load_labels", lambda: [LABEL, OTHER_LABEL]), \
        patched(middleware, "documents_by_label", lambda: {LABEL: {"title": LABEL, "content": "content"}}), \
        patched(middleware, "classify_message", lambda question, model=None, labels=None: calls.append(("classify", question, model, labels)) or Prediction([LABEL], {LABEL: 0.8, OTHER_LABEL: 0.2})), \
        patched(middleware, "generate_llm_answer", lambda question, labels, documents, client=None: calls.append(("llm", question, labels, documents, client)) or "answer"):

        result, information = middleware._call_llm(QUESTION, llm_client="client")

    assert result == {"answer": "answer"}
    assert information["prediction_model"] == "Logist"
    assert information["answer_source"] == "LLM"
    assert information["llm_used_for_answer"] is True
    assert information["logist_used_for_answer"] is False
    assert information["selected_labels"] == [LABEL]
    assert information["selected_scores"] == {LABEL: 0.8}
    assert information["remaining_scores"] == {OTHER_LABEL: 0.2}
    assert calls == [
        ("classify", QUESTION, "model", [LABEL, OTHER_LABEL]),
        ("llm", QUESTION, [LABEL], {LABEL: {"title": LABEL, "content": "content"}}, "client"),
    ]


def test_call_llm_skips_llm_for_out_of_scope_prediction():
    calls = []

    def fail_if_called(*args, **kwargs):
        raise AssertionError("Out-of-scope questions must be answered by Logist fallback.")

    with patched(middleware, "load_chatbot_model", lambda: "model"), \
        patched(middleware, "load_labels", lambda: [LABEL, OUT_OF_SCOPE_LABEL]), \
        patched(middleware, "documents_by_label", lambda: {}), \
        patched(middleware, "classify_message", lambda question, model=None, labels=None: calls.append(("classify", question, model, labels)) or Prediction([OUT_OF_SCOPE_LABEL], {LABEL: 0.12, OUT_OF_SCOPE_LABEL: 0.31})), \
        patched(middleware, "generate_llm_answer", fail_if_called):

        result, information = middleware._call_llm("Bệnh viện ở đâu?", llm_client="client")

    assert "hệ thống đấu giá" in result["answer"].lower()
    assert information["prediction_model"] == "Logist"
    assert information["answer_source"] == "Logist"
    assert information["llm_used_for_answer"] is False
    assert information["logist_used_for_answer"] is True
    assert information["selected_labels"] == [OUT_OF_SCOPE_LABEL]
    assert information["selected_scores"] == {OUT_OF_SCOPE_LABEL: 0.31}
    assert information["remaining_scores"] == {LABEL: 0.12}
    assert calls == [
        ("classify", "Bệnh viện ở đâu?", "model", [LABEL, OUT_OF_SCOPE_LABEL]),
    ]


def test_build_information_payload_splits_selected_and_remaining_scores():
    payload = middleware.build_information_payload(
        QUESTION,
        labels=[LABEL],
        scores={LABEL: 0.73, OTHER_LABEL: 0.21},
        answer_source="LLM",
        status="success",
    )

    assert payload["selected_labels"] == [LABEL]
    assert payload["selected_scores"] == {LABEL: 0.73}
    assert payload["remaining_scores"] == {OTHER_LABEL: 0.21}
    assert payload["all_scores"] == {LABEL: 0.73, OTHER_LABEL: 0.21}
    assert payload["answer_source"] == "LLM"


def main():
    test_handle_question_success_transitions_to_llm_and_clears_error_state()
    test_handle_question_llm_error_forwards_context_to_logist()
    test_handle_question_invalid_question_skips_logist_forwarding()
    test_call_llm_uses_logist_prediction_before_generating_answer()
    test_call_llm_skips_llm_for_out_of_scope_prediction()
    test_build_information_payload_splits_selected_and_remaining_scores()
    print("Connect middleware logic tests passed.")


if __name__ == "__main__":
    main()
