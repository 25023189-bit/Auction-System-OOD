import requests

from .config import OLLAMA_MODEL, OLLAMA_URL, REQUEST_TIMEOUT_SECONDS


class OllamaClient:
    def __init__(self, url=OLLAMA_URL, model=OLLAMA_MODEL):
        self.url = url
        self.model = model

    def generate(self, prompt):
        try:
            response = requests.post(
                self.url,
                json={
                    "model": self.model,
                    "prompt": prompt,
                    "stream": False,
                },
                timeout=REQUEST_TIMEOUT_SECONDS,
            )
            response.raise_for_status()
            data = response.json()
        except requests.RequestException as exc:
            raise RuntimeError("Không gọi được Ollama. Hãy kiểm tra server và model.") from exc
        except ValueError as exc:
            raise RuntimeError("Ollama trả về dữ liệu không phải JSON hợp lệ.") from exc

        answer = data.get("response")

        if not isinstance(answer, str) or not answer.strip():
            raise RuntimeError("Ollama không trả về trường response hợp lệ.")

        return answer.strip()
