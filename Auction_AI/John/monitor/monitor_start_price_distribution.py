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
COLUMN = "start_price"


def quantile_bins(values):
    clean = values.dropna()
    if clean.nunique() <= 20:
        counts = clean.value_counts().sort_index().rename("count").to_frame()
    else:
        counts = pd.qcut(clean, q=10, duplicates="drop").value_counts().sort_index().rename("count").to_frame()
    counts["percent"] = (counts["count"] / len(values) * 100).round(2)
    return counts


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

    bins = quantile_bins(values)
    bins.to_csv(REPORT_DIR / f"{COLUMN}_quantile_bins.csv", encoding="utf-8-sig")

    if "category" in df.columns:
        by_category = values.groupby(df["category"]).describe().round(4)
        by_category.to_csv(REPORT_DIR / f"{COLUMN}_by_category.csv", encoding="utf-8-sig")

    if "auto_approve" in df.columns:
        by_label = values.groupby(df["auto_approve"]).describe().round(4)
        by_label.to_csv(REPORT_DIR / f"{COLUMN}_by_auto_approve.csv", encoding="utf-8-sig")

    if plt is not None:
        fig, ax = plt.subplots(figsize=(9, 5))
        clean = values.dropna()
        ax.hist(clean, bins=30, color="#ea580c", edgecolor="white")
        if (clean > 0).all():
            ax.set_xscale("log")
        ax.set_title("start_price distribution")
        ax.set_xlabel(COLUMN)
        ax.set_ylabel("count")
        ax.grid(axis="y", alpha=0.25)
        fig.tight_layout()
        fig.savefig(REPORT_DIR / f"{COLUMN}_histogram.png", dpi=150)
        plt.close(fig)

    print(f"Saved {COLUMN} distribution reports to {REPORT_DIR}")


if __name__ == "__main__":
    main()
