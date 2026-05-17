def build_chatbot_prompt(question, labels, documents):
    selected_documents = _select_documents(labels, documents)
    label_line = ", ".join(labels) if labels else "Không có nhãn đủ tin cậy."
    topics = _format_topic_blocks(selected_documents)

    return f"""
Bạn là chatbot hỗ trợ người dùng trong hệ thống đấu giá.

Ngữ cảnh đã xử lý:
- Câu hỏi đã được bộ phân loại nội bộ chọn nhãn trước khi chuyển sang LLM.
- Nhãn đã chọn: {label_line}
- Nội dung chuẩn được phép dùng:
{topics}

Yêu cầu trả lời:
- Trả lời bằng tiếng Việt, ngắn gọn, rõ ràng, không markdown, không JSON.
- Chỉ dựa trên nhãn đã chọn và nội dung chuẩn ở trên khi nhãn đủ tin cậy.
- Nếu không có nhãn đủ tin cậy, trả lời ngắn và điều hướng tự nhiên về các thao tác trong hệ thống đấu giá.
- Không nhắc đến thuật toán, mô hình, Logistic Regression, LLM, nhãn nội bộ hoặc quá trình phân loại.
- Không từ chối bằng lý do phạm vi, nhãn hoặc chủ đề.
- Nếu câu hỏi có nhiều ý, trả lời theo thứ tự thao tác tự nhiên.
- Nếu nội dung chưa chắc chắn, hướng người dùng hỏi rõ hơn trong phạm vi hệ thống đấu giá.

Câu hỏi người dùng:
{question}
""".strip()


def _select_documents(labels, documents):
    document_map = _to_document_map(documents)
    if not labels:
        return list(document_map.values())

    selected = []
    for label in labels:
        document = document_map.get(_normalize_label(label))
        if document:
            selected.append(document)

    return selected


def _to_document_map(documents):
    if isinstance(documents, dict):
        iterable = documents.values()
    else:
        iterable = documents

    return {
        _normalize_label(document["title"]): document
        for document in iterable
        if isinstance(document, dict)
        and isinstance(document.get("title"), str)
        and isinstance(document.get("content"), str)
    }


def _format_topic_blocks(documents):
    if not documents:
        return "- Không có nội dung chuẩn phù hợp."

    return "\n".join(
        f"- Nhãn: {document['title']}\n"
        f"  Nội dung chuẩn: {document['content']}"
        for document in documents
    )


def _normalize_label(label):
    return " ".join(str(label).strip().upper().split())
