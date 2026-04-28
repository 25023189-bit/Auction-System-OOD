import argparse
import csv
import json
import subprocess
import sys
import time
from datetime import datetime
from pathlib import Path


MONITOR_DIR = Path(__file__).resolve().parent
REPORT_DIR = MONITOR_DIR / "distribution_reports"
DEFAULT_PATTERN = "monitor_*_distribution.py"


def now_text():
    return datetime.now().isoformat(timespec="seconds")


def discover_scripts(pattern):
    scripts = []
    for path in sorted(MONITOR_DIR.glob(pattern)):
        if path.name == Path(__file__).name:
            continue
        if path.is_file():
            scripts.append(path)
    return scripts


def run_script(script):
    started_at = now_text()
    started = time.perf_counter()
    result = subprocess.run(
        [sys.executable, script.name],
        cwd=MONITOR_DIR,
        text=True,
        capture_output=True,
    )
    duration = round(time.perf_counter() - started, 3)

    return {
        "script": script.name,
        "status": "ok" if result.returncode == 0 else "failed",
        "return_code": result.returncode,
        "started_at": started_at,
        "ended_at": now_text(),
        "duration_seconds": duration,
        "stdout": result.stdout.strip(),
        "stderr": result.stderr.strip(),
    }


def write_status(results):
    REPORT_DIR.mkdir(exist_ok=True)
    csv_path = REPORT_DIR / "monitor_run_status.csv"
    json_path = REPORT_DIR / "monitor_run_status.json"

    fields = [
        "script",
        "status",
        "return_code",
        "started_at",
        "ended_at",
        "duration_seconds",
        "stdout",
        "stderr",
    ]
    with csv_path.open("w", newline="", encoding="utf-8-sig") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields)
        writer.writeheader()
        writer.writerows(results)

    with json_path.open("w", encoding="utf-8") as handle:
        json.dump(results, handle, ensure_ascii=False, indent=2)

    return csv_path, json_path


def main():
    parser = argparse.ArgumentParser(
        description="Run every monitor script and update distribution reports."
    )
    parser.add_argument(
        "--pattern",
        default=DEFAULT_PATTERN,
        help=f"Glob pattern for monitor scripts. Default: {DEFAULT_PATTERN}",
    )
    parser.add_argument(
        "--fail-fast",
        action="store_true",
        help="Stop after the first failed monitor script.",
    )
    parser.add_argument(
        "--list",
        action="store_true",
        help="Only list scripts that would be run.",
    )
    args = parser.parse_args()

    scripts = discover_scripts(args.pattern)
    if not scripts:
        print(f"No monitor scripts found with pattern: {args.pattern}")
        return 1

    if args.list:
        for script in scripts:
            print(script.name)
        return 0

    print(f"Found {len(scripts)} monitor scripts.")
    results = []
    for index, script in enumerate(scripts, start=1):
        print(f"[{index}/{len(scripts)}] Running {script.name}...")
        result = run_script(script)
        results.append(result)
        print(f"    {result['status']} in {result['duration_seconds']}s")
        if result["stderr"]:
            print(f"    stderr: {result['stderr']}")
        if args.fail_fast and result["status"] != "ok":
            break

    csv_path, json_path = write_status(results)
    failed = [item for item in results if item["status"] != "ok"]

    print(f"Status CSV: {csv_path}")
    print(f"Status JSON: {json_path}")
    print(f"Completed: {len(results) - len(failed)} ok, {len(failed)} failed.")

    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
