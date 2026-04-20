import argparse
import json
from pathlib import Path

import joblib
import pandas as pd

BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "auction_logreg_bow.pkl"
"""
python predict_one.py --title "iphone 13 fullbox chinh hang" --description "opncpen" --seller_rating 4.5 --seller_completed_auctions 120 --seller_cancel_rate 0.05 --title_length 28 --desc_length 75 --num_positive_keywords 3 --num_negative_keywords 0 --start_price_log 15.2 --duration_minutes 90 --extension_seconds 30 --start_hour 20 --day_of_week 6 --is_weekend 1
"""
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--title", required=True)
    parser.add_argument("--description", required=True)
    parser.add_argument("--seller_rating", type=float, required=True)
    parser.add_argument("--seller_completed_auctions", type=int, required=True)
    parser.add_argument("--seller_cancel_rate", type=float, required=True)
    parser.add_argument("--title_length", type=int, required=True)
    parser.add_argument("--desc_length", type=int, required=True)
    parser.add_argument("--num_positive_keywords", type=int, required=True)
    parser.add_argument("--num_negative_keywords", type=int, required=True)
    parser.add_argument("--start_price_log", type=float, required=True)
    parser.add_argument("--duration_minutes", type=int, required=True)
    parser.add_argument("--extension_seconds", type=int, required=True)
    parser.add_argument("--start_hour", type=int, required=True)
    parser.add_argument("--day_of_week", type=int, required=True)
    parser.add_argument("--is_weekend", type=int, required=True)
    args = parser.parse_args()

    model = joblib.load(MODEL_PATH)

    row = pd.DataFrame([vars(args)])
    prob = float(model.predict_proba(row)[0][1])
    pred = int(model.predict(row)[0])

    print(json.dumps({
        "auto_approve": pred,
        "probability": prob
    }, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    main()