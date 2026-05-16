import argparse
import json
import sys
from pathlib import Path

from model.beta_chatbot import BetaChatbot
from model.json_io import read_question_file, write_answer_file


APP_DIR = Path(__file__).resolve().parent


def configure_stdout():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Read one question from a JSON file and return one JSON answer."
    )
    parser.add_argument(
        "--input",
        default=str(APP_DIR / "input.json"),
        help="Path to a JSON file that contains only the 'question' field.",
    )
    parser.add_argument(
        "--output",
        default=str(APP_DIR / "output.json"),
        help="Path to save the JSON answer.",
    )
    return parser.parse_args()


def main():
    configure_stdout()
    args = parse_args()

    try:
        question = read_question_file(args.input)
        chatbot = BetaChatbot()
        answer = chatbot.answer(question)
        result = {"answer": answer}

        if args.output:
            write_answer_file(args.output, result)

        sys.stdout.write(json.dumps(result, ensure_ascii=False))
        return 0
    except Exception as exc:
        result = {"answer": f"Lỗi: {exc}"}
        if "args" in locals() and args.output:
            write_answer_file(args.output, result)
        sys.stdout.write(json.dumps(result, ensure_ascii=False))
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
