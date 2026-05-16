import re
import unicodedata


STOPWORDS = {
    "ai",
    "ban",
    "bang",
    "bi",
    "cho",
    "co",
    "cong",
    "cua",
    "duoc",
    "gi",
    "hay",
    "hoi",
    "la",
    "lam",
    "may",
    "mot",
    "nay",
    "neu",
    "nhung",
    "noi",
    "o",
    "toi",
    "trong",
    "ve",
    "voi",
}


def normalize_text(text):
    lowered_text = text.lower().replace("đ", "d")
    decomposed_text = unicodedata.normalize("NFD", lowered_text)
    without_accents = "".join(
        character
        for character in decomposed_text
        if unicodedata.category(character) != "Mn"
    )
    return re.sub(r"\s+", " ", without_accents).strip()


def tokenize(text, remove_stopwords=True):
    normalized_text = normalize_text(text)
    tokens = re.findall(r"[a-z0-9]+", normalized_text)

    if not remove_stopwords:
        return tokens

    return [
        token
        for token in tokens
        if token not in STOPWORDS and len(token) > 1
    ]
