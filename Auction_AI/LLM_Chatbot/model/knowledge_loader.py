import json
from pathlib import Path


def load_documents(path):
    knowledge_path = Path(path)

    if not knowledge_path.is_file():
        raise FileNotFoundError(f"Không tìm thấy file knowledge: {knowledge_path}")

    try:
        payload = json.loads(knowledge_path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        raise ValueError("File knowledge không đúng định dạng JSON.") from exc

    documents = payload.get("documents")

    if not isinstance(documents, list) or not documents:
        raise ValueError("File knowledge phải có trường documents là danh sách không rỗng.")

    for document in documents:
        if not isinstance(document, dict):
            raise ValueError("Mỗi document trong knowledge phải là một JSON object.")

        if set(document.keys()) != {"title", "content"}:
            raise ValueError("Mỗi document chỉ được chứa đúng hai trường: title và content.")

        if not isinstance(document["title"], str) or not document["title"].strip():
            raise ValueError("Trường title trong document phải là chuỗi không rỗng.")

        if not isinstance(document["content"], str) or not document["content"].strip():
            raise ValueError("Trường content trong document phải là chuỗi không rỗng.")

    return [
        {
            "title": document["title"].strip(),
            "content": document["content"].strip(),
        }
        for document in documents
    ]
