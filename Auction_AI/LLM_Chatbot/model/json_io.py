import json
from pathlib import Path


def read_question_file(path):
    input_path = Path(path)

    if not input_path.is_file():
        raise FileNotFoundError(f"Không tìm thấy file input: {input_path}")

    try:
        payload = json.loads(input_path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        raise ValueError("File input không đúng định dạng JSON.") from exc

    if not isinstance(payload, dict):
        raise ValueError("File input phải là một JSON object.")

    if set(payload.keys()) != {"question"}:
        raise ValueError("File input chỉ được chứa đúng một trường: question.")

    question = payload["question"]

    if not isinstance(question, str) or not question.strip():
        raise ValueError("Trường question phải là chuỗi không rỗng.")

    return question.strip()


def write_answer_file(path, payload):
    output_path = Path(path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
