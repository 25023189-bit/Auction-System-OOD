"""Health check for Auction_AI integrations.

Run from the Auction_AI directory or from the repository root:
    python Auction_AI/health_check.py

The script checks path availability and executes lightweight prediction/chatbot
smoke tests without changing the Java-facing protocols.
"""

from __future__ import annotations

import json
import subprocess
import sys
import tempfile
from pathlib import Path
import os

AUCTION_AI_DIR = Path(__file__).resolve().parent
if str(AUCTION_AI_DIR) not in sys.path:
    sys.path.insert(0, str(AUCTION_AI_DIR))


def main() -> int:
    print("Auction_AI health check")
    print(f"AUCTION_AI_DIR = {AUCTION_AI_DIR}")

    checks = []
    checks.extend(check_autoapprove())
    checks.extend(check_chatbot())

    failed = [check for check in checks if not check[1]]
    for name, ok, detail in checks:
        status = "OK" if ok else "FAIL"
        print(f"[{status}] {name}: {detail}")

    if failed:
        print(f"\nHealth check failed: {len(failed)} issue(s).")
        return 1

    print("\nHealth check passed.")
    return 0


def check_autoapprove() -> list[tuple[str, bool, str]]:
    auto_dir = AUCTION_AI_DIR / "AutoApprove"
    checks = [
        ("AutoApprove directory", auto_dir.is_dir(), str(auto_dir)),
        ("AutoApprove predict script", (auto_dir / "predict_from_input.py").is_file(), str(auto_dir / "predict_from_input.py")),
        ("AutoApprove model", (auto_dir / "auction_model.pkl").is_file(), str(auto_dir / "auction_model.pkl")),
        ("AutoApprove bridge input", (auto_dir / "input_ap.json").exists(), str(auto_dir / "input_ap.json")),
        ("AutoApprove bridge output", (auto_dir / "output_ap.json").exists(), str(auto_dir / "output_ap.json")),
        ("AutoApprove test samples", (auto_dir / "test_samples.json").exists(), str(auto_dir / "test_samples.json")),
    ]

    if not all(ok for _, ok, _ in checks[:3]):
        return checks

    env = os.environ.copy()
    env["PYTHONIOENCODING"] = "utf-8"
    env["PYTHONUTF8"] = "1"

    with tempfile.TemporaryDirectory() as tmp:
        output_path = Path(tmp) / "prediction_results.json"
        command = [
            sys.executable,
            str(auto_dir / "predict_from_input.py"),
            "--input",
            str(auto_dir / "test_samples.json"),
            "--output",
            str(output_path),
        ]

        try:
            completed = subprocess.run(
                command,
                cwd=str(auto_dir),
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
                timeout=45,
                env=env,
            )

            ok = completed.returncode == 0 and output_path.exists()

            checks.append(
                (
                    "AutoApprove smoke prediction",
                    ok,
                    (
                        f"returncode={completed.returncode} "
                        f"output_exists={output_path.exists()} "
                        f"stdout={_truncate(completed.stdout)} "
                        f"stderr={_truncate(completed.stderr)}"
                    ),
                )
            )

        except subprocess.TimeoutExpired as exc:
            checks.append(
                (
                    "AutoApprove smoke prediction",
                    False,
                    (
                        f"timeout after {exc.timeout}s "
                        f"stdout={_truncate(exc.stdout or '')} "
                        f"stderr={_truncate(exc.stderr or '')}"
                    ),
                )
            )

    return checks


def check_chatbot() -> list[tuple[str, bool, str]]:
    chatbot_dir = AUCTION_AI_DIR / "ChatBot"
    checks = [
        ("ChatBot directory", chatbot_dir.is_dir(), str(chatbot_dir)),
        ("ChatBot app", (chatbot_dir / "Main" / "app.py").is_file(), str(chatbot_dir / "Main" / "app.py")),
        ("ChatBot Logist model", (chatbot_dir / "Logist" / "models" / "chatbot_intent_model.joblib").is_file(), str(chatbot_dir / "Logist" / "models" / "chatbot_intent_model.joblib")),
        ("ChatBot labels", (chatbot_dir / "Logist" / "models" / "labels.json").is_file(), str(chatbot_dir / "Logist" / "models" / "labels.json")),
        ("ChatBot knowledge", (chatbot_dir / "LLM" / "knowledge" / "label_constraints.json").is_file(), str(chatbot_dir / "LLM" / "knowledge" / "label_constraints.json")),
    ]

    if not all(ok for _, ok, _ in checks[:5]):
        return checks

    command = [
        sys.executable,
        str(chatbot_dir / "Main" / "app.py"),
        "--question",
        "Tôi muốn tham gia phòng đấu giá",
    ]
    completed = subprocess.run(
        command,
        cwd=str(AUCTION_AI_DIR),
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    chatbot_ok = completed.returncode == 0 and _is_json_object(completed.stdout)
    checks.append(
        (
            "ChatBot smoke question",
            chatbot_ok,
            f"returncode={completed.returncode} stdout={_truncate(completed.stdout)} stderr={_truncate(completed.stderr)}",
        )
    )
    return checks


def _is_json_object(value: str) -> bool:
    try:
        payload = json.loads(value)
    except json.JSONDecodeError:
        return False
    return isinstance(payload, dict)


def _truncate(value: str | None, limit: int = 300) -> str:
    if not value:
        return ""
    value = value.strip().replace("\n", " | ")
    if len(value) <= limit:
        return value
    return value[:limit] + "... [truncated]"


if __name__ == "__main__":
    raise SystemExit(main())