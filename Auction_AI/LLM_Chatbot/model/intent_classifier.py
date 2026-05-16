import math
from dataclasses import dataclass

from .text_processing import normalize_text, tokenize


@dataclass
class IntentMatch:
    is_in_scope: bool
    document: dict | None
    score: float


class IntentClassifier:
    def __init__(self, documents, threshold=0.18):
        self.documents = documents
        self.threshold = threshold
        self.prepared_documents = [self._prepare_document(document) for document in documents]
        self.idf = self._build_idf()

        for prepared_document in self.prepared_documents:
            prepared_document["vector"] = self._tfidf_vector(prepared_document["tokens"])

    def classify(self, question):
        question_tokens = tokenize(question)

        if not question_tokens:
            return IntentMatch(False, None, 0.0)

        question_vector = self._tfidf_vector(question_tokens)
        normalized_question = normalize_text(question)
        best_document = None
        best_score = 0.0

        for prepared_document in self.prepared_documents:
            tfidf_score = self._cosine_similarity(question_vector, prepared_document["vector"])
            keyword_score = self._keyword_score(
                normalized_question,
                set(question_tokens),
                prepared_document,
            )
            score = (0.65 * tfidf_score) + (0.35 * keyword_score)

            if score > best_score:
                best_score = score
                best_document = prepared_document["document"]

        if best_score < self.threshold:
            return IntentMatch(False, None, best_score)

        return IntentMatch(True, best_document, best_score)

    def _prepare_document(self, document):
        text = f"{document['title']} {document['content']}"
        title_tokens = tokenize(document["title"], remove_stopwords=False)
        content_tokens = tokenize(text)

        return {
            "document": document,
            "normalized_title": normalize_text(document["title"]),
            "title_tokens": set(title_tokens),
            "tokens": content_tokens,
            "token_set": set(content_tokens),
        }

    def _build_idf(self):
        document_count = len(self.prepared_documents)
        document_frequency = {}

        for prepared_document in self.prepared_documents:
            for token in prepared_document["token_set"]:
                document_frequency[token] = document_frequency.get(token, 0) + 1

        return {
            token: math.log((document_count + 1) / (frequency + 1)) + 1
            for token, frequency in document_frequency.items()
        }

    def _tfidf_vector(self, tokens):
        vector = {}

        for token in tokens:
            if token not in self.idf:
                continue
            vector[token] = vector.get(token, 0.0) + 1.0

        for token in vector:
            vector[token] *= self.idf[token]

        return vector

    def _cosine_similarity(self, first_vector, second_vector):
        if not first_vector or not second_vector:
            return 0.0

        dot_product = sum(
            first_vector[token] * second_vector.get(token, 0.0)
            for token in first_vector
        )
        first_norm = math.sqrt(sum(value * value for value in first_vector.values()))
        second_norm = math.sqrt(sum(value * value for value in second_vector.values()))

        if first_norm == 0.0 or second_norm == 0.0:
            return 0.0

        return dot_product / (first_norm * second_norm)

    def _keyword_score(self, normalized_question, question_tokens, prepared_document):
        score = 0.0

        if prepared_document["normalized_title"] in normalized_question:
            score += 0.6

        title_overlap = question_tokens & prepared_document["title_tokens"]
        content_overlap = question_tokens & prepared_document["token_set"]

        if prepared_document["title_tokens"]:
            score += 0.3 * (len(title_overlap) / len(prepared_document["title_tokens"]))

        if question_tokens:
            score += 0.1 * (len(content_overlap) / len(question_tokens))

        return min(score, 1.0)
