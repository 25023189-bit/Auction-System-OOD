from pathlib import Path

import pandas as pd

try:
    import matplotlib

    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
except Exception:
    plt = None


MONITOR_DIR = Path(__file__).resolve().parent
BASE_DIR = MONITOR_DIR.parent
DATA_PATH = BASE_DIR / "Training_Data.csv"
REPORT_DIR = MONITOR_DIR / "distribution_reports"
COLUMN = "seller_rating"


def main():
    df = pd.read_csv(DATA_PATH)
    if COLUMN not in df.columns:
        raise KeyError(f"Missing column: {COLUMN}")

    REPORT_DIR.mkdir(exist_ok=True)
    values = pd.to_numeric(df[COLUMN], errors="coerce")

    summary = values.describe(
        percentiles=[0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95, 0.99]
    ).to_frame(COLUMN)
    summary.loc["missing", COLUMN] = values.isna().sum()
    summary.loc["valid", COLUMN] = values.notna().sum()
    summary.to_csv(REPORT_DIR / f"{COLUMN}_summary.csv", encoding="utf-8-sig")

    bins = pd.cut(
        values,
        bins=[0, 3.6, 4.2, 5.0],
        labels=["risk_lt_3_6", "medium_3_6_to_4_2", "good_ge_4_2"],
        include_lowest=True,
    )
    bin_report = bins.value_counts(dropna=False).sort_index().rename("count").to_frame()
    bin_report["percent"] = (bin_report["count"] / len(df) * 100).round(2)
    bin_report.to_csv(REPORT_DIR / f"{COLUMN}_bins.csv", encoding="utf-8-sig")

    if "auto_approve" in df.columns:
        by_label = values.groupby(df["auto_approve"]).describe().round(4)
        by_label.to_csv(REPORT_DIR / f"{COLUMN}_by_auto_approve.csv", encoding="utf-8-sig")

    if plt is not None:
        fig, ax = plt.subplots(figsize=(9, 5))
        ax.hist(values.dropna(), bins=20, color="#2563eb", edgecolor="white")
        ax.set_title("seller_rating distribution")
        ax.set_xlabel(COLUMN)
        ax.set_ylabel("count")
        ax.grid(axis="y", alpha=0.25)
        fig.tight_layout()
        fig.savefig(REPORT_DIR / f"{COLUMN}_histogram.png", dpi=150)
        plt.close(fig)

    print(f"Saved {COLUMN} distribution reports to {REPORT_DIR}")


if __name__ == "__main__":
    main()
