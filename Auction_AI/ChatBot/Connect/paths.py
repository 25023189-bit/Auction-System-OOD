from pathlib import Path


CONNECT_DIR = Path(__file__).resolve().parent
CHATBOT_DIR = CONNECT_DIR.parent
IODATA_DIR = CHATBOT_DIR / "IOdata"

INPUT_PATH = IODATA_DIR / "input.json"
OUTPUT_PATH = IODATA_DIR / "output.json"
STATUS_DIR = CHATBOT_DIR / "status"

# Backward-compatible paths currently used by the Java bridge.
# Keep these constants until Java is migrated to the corrected filenames.
ERROR_INFO_PATH = STATUS_DIR / "errol_info.json"
INFORMATION_PATH = STATUS_DIR / "infomation.json"

# Corrected aliases for diagnostics/new integrations.
CANONICAL_ERROR_INFO_PATH = STATUS_DIR / "error_info.json"
CANONICAL_INFORMATION_PATH = STATUS_DIR / "information.json"
