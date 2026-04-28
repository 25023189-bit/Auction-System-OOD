from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


KNOWLEDGE_BASE = [
    {
        "title": "Kết thúc phiên do hết thời gian",
        "content": """
Khi phiên đấu giá hết thời gian, hệ thống sẽ xác định bidder có mức giá cao nhất là người thắng.
Bidder thắng sẽ bị trừ tiền tương ứng với giá thắng.
Seller sẽ được cộng tiền tương ứng.
Các bidder không thắng không bị trừ tiền.
"""
    },
    {
        "title": "Seller chủ động kết thúc phiên",
        "content": """
Nếu seller chủ động kết thúc phiên đấu giá trước thời hạn, hệ thống sẽ không thực hiện giao dịch tiền.
Bidder không bị trừ tiền.
Seller không được cộng tiền.
Phiên đấu giá được xem là kết thúc không giao dịch.
"""
    },
    {
        "title": "Admin ban phiên đấu giá",
        "content": """
Nếu admin ban một phiên đấu giá, hệ thống sẽ dừng phiên đó ngay lập tức.
Không có bidder nào bị trừ tiền.
Seller không được nhận tiền.
Lý do là phiên đấu giá bị xem là không hợp lệ hoặc vi phạm quy định.
"""
    },
    {
        "title": "Tạo phiên đấu giá",
        "content": """
Seller có thể tạo phiên đấu giá bằng cách nhập thông tin sản phẩm, mô tả, giá khởi điểm và thời gian đấu giá.
Sau khi tạo, phiên có thể cần được hệ thống hoặc admin kiểm duyệt trước khi công khai.
"""
    },
    {
        "title": "Tham gia đấu giá",
        "content": """
Bidder có thể tham gia phiên đấu giá đang chạy.
Bidder cần đặt giá cao hơn giá hiện tại.
Nếu giá đặt hợp lệ, hệ thống cập nhật bidder đó thành người đang giữ giá cao nhất.
"""
    },
    {
        "title": "Vai trò trong hệ thống",
        "content": """
Hệ thống có ba vai trò chính: BIDDER, SELLER và ADMIN.
Bidder tham gia đấu giá và đặt giá.
Seller tạo và quản lý phiên đấu giá của mình.
Admin có quyền kiểm duyệt, ban phiên đấu giá và xử lý vi phạm.
"""
    }
]


class AuctionChatbot:
    def __init__(self, knowledge_base):
        self.knowledge_base = knowledge_base

        self.documents = [
            item["title"] + "\n" + item["content"]
            for item in knowledge_base
        ]

        self.vectorizer = TfidfVectorizer(
            lowercase=True,
            ngram_range=(1, 2)
        )

        self.doc_vectors = self.vectorizer.fit_transform(self.documents)

    def retrieve(self, question, top_k=2):
        question_vector = self.vectorizer.transform([question])
        similarities = cosine_similarity(question_vector, self.doc_vectors)[0]

        ranked_indices = similarities.argsort()[::-1]

        results = []
        for idx in ranked_indices[:top_k]:
            results.append({
                "score": similarities[idx],
                "title": self.knowledge_base[idx]["title"],
                "content": self.knowledge_base[idx]["content"]
            })

        return results

    def generate_answer(self, question, retrieved_docs):
        best_doc = retrieved_docs[0]

        if best_doc["score"] < 0.08:
            return (
                "Mình chưa tìm thấy kiến thức phù hợp trong hệ thống đấu giá để trả lời câu này. "
                "Bạn nên hỏi về tạo phiên, tham gia đấu giá, kết thúc phiên, thanh toán, admin ban hoặc vai trò người dùng."
            )

        context = "\n".join(
            f"- {doc['title']}:\n{doc['content']}"
            for doc in retrieved_docs
        )

        answer = f"""
Dựa trên kiến thức hiện có của hệ thống đấu giá:

{best_doc["content"].strip()}

Câu hỏi của bạn là: "{question}"

Tóm lại: {self.summarize(best_doc["title"])}
"""
        return answer.strip()

    def summarize(self, title):
        if title == "Kết thúc phiên do hết thời gian":
            return "nếu hết thời gian, bidder trả giá cao nhất thắng, bidder bị trừ tiền và seller nhận tiền."

        if title == "Seller chủ động kết thúc phiên":
            return "nếu seller tự kết thúc phiên, hệ thống không trừ tiền bidder và không cộng tiền cho seller."

        if title == "Admin ban phiên đấu giá":
            return "nếu admin ban phiên, phiên bị dừng và không có giao dịch tiền."

        if title == "Tạo phiên đấu giá":
            return "seller cần nhập thông tin sản phẩm, mô tả, giá khởi điểm và thời gian đấu giá."

        if title == "Tham gia đấu giá":
            return "bidder cần đặt giá cao hơn giá hiện tại để tham gia hợp lệ."

        if title == "Vai trò trong hệ thống":
            return "hệ thống gồm bidder, seller và admin, mỗi vai trò có quyền khác nhau."

        return "đây là thông tin liên quan đến nghiệp vụ đấu giá."

    def ask(self, question):
        docs = self.retrieve(question)
        return self.generate_answer(question, docs)


def main():
    bot = AuctionChatbot(KNOWLEDGE_BASE)

    print("=== Auction Chatbot ===")
    print("Gõ 'exit' để thoát.\n")

    while True:
        question = input("Bạn: ").strip()

        if question.lower() in ["exit", "quit", "q"]:
            print("Bot: Tạm biệt!")
            break

        answer = bot.ask(question)
        print("\nBot:", answer)
        print("-" * 60)


if __name__ == "__main__":
    main()