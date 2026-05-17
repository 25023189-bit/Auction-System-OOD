import argparse
import json
import sys
from pathlib import Path


AUCTION_AI_DIR = Path(__file__).resolve().parents[2]
if str(AUCTION_AI_DIR) not in sys.path:
    sys.path.insert(0, str(AUCTION_AI_DIR))

from ChatBot.Connect.middleware import handle_question, run_from_file
from ChatBot.Connect.paths import ERROR_INFO_PATH, INPUT_PATH, OUTPUT_PATH


def configure_stdout() -> None:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")


def parse_args():
    parser = argparse.ArgumentParser(description="Auction chatbot Connect entrypoint.")
    parser.add_argument("--question", default=None)
    parser.add_argument("--input", default=str(INPUT_PATH))
    parser.add_argument("--output", default=str(OUTPUT_PATH))
    parser.add_argument("--error-info", default=str(ERROR_INFO_PATH))
    return parser.parse_args()


def main() -> int:
    configure_stdout()
    args = parse_args()

    if args.question is not None:
        result = handle_question(
            args.question,
            input_path=args.input,
            output_path=args.output,
            error_info_path=args.error_info,
        )
    else:
        result = run_from_file(
            input_path=args.input,
            output_path=args.output,
            error_info_path=args.error_info,
        )

    sys.stdout.write(json.dumps(result, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
