"""Diagnostics and logging helpers for the AutoApprove Python bridge.

This module is intentionally dependency-light so it can be imported by both
CLI scripts and Java-triggered process calls.
"""

from __future__ import annotations

import logging
import os
import sys
from pathlib import Path
from typing import Iterable

BASE_DIR = Path(__file__).resolve().parent
STATUS_DIR = BASE_DIR / "status"
LOG_PATH = STATUS_DIR / "autoapprove_process.log"
DEFAULT_INPUT_PATH = BASE_DIR / "input_ap.json"
DEFAULT_OUTPUT_PATH = BASE_DIR / "output_ap.json"
DEFAULT_DEBUG_OUTPUT_PATH = BASE_DIR / "prediction_results.json"
DEFAULT_MODEL_PATH = BASE_DIR / "auction_model.pkl"
DEFAULT_SCRIPT_PATH = BASE_DIR / "predict_from_input.py"

LOGGER_NAME = "auction_ai.autoapprove"


def setup_logging(log_path: Path = LOG_PATH, level: int = logging.INFO) -> logging.Logger:
    """Configure file + stderr logging once and return the AutoApprove logger."""
    log_path = Path(log_path)
    log_path.parent.mkdir(parents=True, exist_ok=True)

    logger = logging.getLogger(LOGGER_NAME)
    logger.setLevel(level)
    logger.propagate = False

    resolved_log = str(log_path.resolve())
    has_file_handler = any(
        isinstance(handler, logging.FileHandler)
        and getattr(handler, "baseFilename", None) == resolved_log
        for handler in logger.handlers
    )
    if not has_file_handler:
        file_handler = logging.FileHandler(log_path, encoding="utf-8")
        file_handler.setLevel(level)
        file_handler.setFormatter(_formatter())
        logger.addHandler(file_handler)

    has_stream_handler = any(
        isinstance(handler, logging.StreamHandler)
        and not isinstance(handler, logging.FileHandler)
        for handler in logger.handlers
    )
    if not has_stream_handler:
        stream_handler = logging.StreamHandler(sys.stderr)
        stream_handler.setLevel(level)
        stream_handler.setFormatter(_formatter())
        logger.addHandler(stream_handler)

    return logger


def get_logger() -> logging.Logger:
    logger = logging.getLogger(LOGGER_NAME)
    if not logger.handlers:
        return setup_logging()
    return logger


def _formatter() -> logging.Formatter:
    return logging.Formatter(
        fmt="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )


def resolve_python_command() -> str:
    value = os.getenv("AUCTION_AUTO_APPROVE_PYTHON") or os.getenv("AUCTION_PYTHON_COMMAND")
    if value and value.strip():
        return value.strip()
    return "python"


def log_path_diagnostics(
    *,
    input_path: Path = DEFAULT_INPUT_PATH,
    output_path: Path = DEFAULT_OUTPUT_PATH,
    model_path: Path = DEFAULT_MODEL_PATH,
    script_path: Path = DEFAULT_SCRIPT_PATH,
    extra_paths: Iterable[Path] | None = None,
) -> dict[str, object]:
    """Log and return a compact diagnostic snapshot for AutoApprove paths."""
    logger = get_logger()
    paths = {
        "ai_directory": BASE_DIR,
        "input_file": Path(input_path),
        "output_file": Path(output_path),
        "debug_output_file": DEFAULT_DEBUG_OUTPUT_PATH,
        "model_file": Path(model_path),
        "script_file": Path(script_path),
        "log_file": LOG_PATH,
    }
    if extra_paths:
        for index, path in enumerate(extra_paths, start=1):
            paths[f"extra_path_{index}"] = Path(path)

    logger.info("AutoApprove AI directory resolved: %s", BASE_DIR)
    logger.info("AutoApprove Python command resolved: %s", resolve_python_command())
    for label, path in paths.items():
        logger.info("AutoApprove %s: %s", label, path)
        if label.endswith("directory"):
            logger.info("AutoApprove %s exists: %s", label, path.is_dir())
        else:
            logger.info("AutoApprove %s exists: %s", label, path.exists())

    for label in ("input_file", "output_file", "debug_output_file", "log_file"):
        parent = paths[label].parent
        parent.mkdir(parents=True, exist_ok=True)
        logger.info("AutoApprove %s parent writable: %s", label, os.access(parent, os.W_OK))

    diagnostics = {
        label: {
            "path": str(path),
            "exists": path.exists(),
            "is_dir": path.is_dir(),
            "is_file": path.is_file(),
        }
        for label, path in paths.items()
    }
    diagnostics["python_command"] = resolve_python_command()
    return diagnostics
