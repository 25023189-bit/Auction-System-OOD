from pathlib import Path


CONNECT_DIR = Path(__file__).resolve().parent
CHATBOT_DIR = CONNECT_DIR.parent
IODATA_DIR = CHATBOT_DIR / "IOdata"

INPUT_PATH = IODATA_DIR / "input.json"
OUTPUT_PATH = IODATA_DIR / "output.json"
STATUS_DIR = CHATBOT_DIR / "status"
ERROR_INFO_PATH = STATUS_DIR / "errol_info.json"
INFORMATION_PATH = STATUS_DIR / "infomation.json"
