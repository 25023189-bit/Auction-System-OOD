"""Diagnostics and logging helpers for the ChatBot Python bridge."""

from __future__ import annotations

import logging
import os
import sys
from pathlib import Path
from typing import Iterable

from .paths import (
    CANONICAL_ERROR_INFO_PATH,
    CANONICAL_INFORMATION_PATH,
    CHATBOT_DIR,
    ERROR_INFO_PATH,
    INPUT_PATH,
    INFORMATION_PATH,
    OUTPUT_PATH,
    STATUS_DIR,
)

LOG_PATH = STATUS_DIR / "chatbot_process.log"
LOGGER_NAME = "auction_ai.chatbot"


def setup_logging(log_path: Path = LOG_PATH, level: int = logging.INFO) -> logging.Logger:
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
    value = os.getenv("AUCTION_CHATBOT_PYTHON") or os.getenv("AUCTION_PYTHON_COMMAND")
    if value and value.strip():
        return value.strip()
    return "python"


def log_path_diagnostics(extra_paths: Iterable[Path] | None = None) -> dict[str, object]:
    logger = get_logger()
    paths = {
        "chatbot_directory": CHATBOT_DIR,
        "input_file": INPUT_PATH,
        "output_file": OUTPUT_PATH,
        "legacy_error_info_file": ERROR_INFO_PATH,
        "legacy_information_file": INFORMATION_PATH,
        "canonical_error_info_file": CANONICAL_ERROR_INFO_PATH,
        "canonical_information_file": CANONICAL_INFORMATION_PATH,
        "log_file": LOG_PATH,
    }
    if extra_paths:
        for index, path in enumerate(extra_paths, start=1):
            paths[f"extra_path_{index}"] = Path(path)

    logger.info("ChatBot AI directory resolved: %s", CHATBOT_DIR)
    logger.info("ChatBot Python command resolved: %s", resolve_python_command())
    for label, path in paths.items():
        logger.info("ChatBot %s: %s", label, path)
        if label.endswith("directory"):
            logger.info("ChatBot %s exists: %s", label, path.is_dir())
        else:
            logger.info("ChatBot %s exists: %s", label, path.exists())

    for label in (
        "input_file",
        "output_file",
        "legacy_error_info_file",
        "legacy_information_file",
        "canonical_error_info_file",
        "canonical_information_file",
        "log_file",
    ):
        parent = paths[label].parent
        parent.mkdir(parents=True, exist_ok=True)
        logger.info("ChatBot %s parent writable: %s", label, os.access(parent, os.W_OK))

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
