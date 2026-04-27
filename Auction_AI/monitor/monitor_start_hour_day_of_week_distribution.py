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
HOUR_COLUMN = "start_hour"
DAY_COLUMN = "day_of_week"
DAY_LABELS = {
    0: "mon",
    1: "tue",
    2: "wed",
    3: "thu",
    4: "fri",
    5: "sat",
    6: "sun",
}


def main():
    df = pd.read_csv(DATA_PATH)
    for column in (HOUR_COLUMN, DAY_COLUMN):
        if column not in df.columns:
            raise KeyError(f"Missing column: {column}")

    REPORT_DIR.mkdir(exist_ok=True)
    hours = pd.to_numeric(df[HOUR_COLUMN], errors="coerce")
    days = pd.to_numeric(df[DAY_COLUMN], errors="coerce")

    summary = hours.describe(
        percentiles=[0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95, 0.99]
    ).to_frame(HOUR_COLUMN)
    summary.loc["missing", HOUR_COLUMN] = hours.isna().sum()
    summary.loc["valid", HOUR_COLUMN] = hours.notna().sum()
    summary.to_csv(REPORT_DIR / f"{HOUR_COLUMN}_summary.csv", encoding="utf-8-sig")

    hour_counts = hours.value_counts(dropna=False).sort_index().rename("count").to_frame()
    hour_counts["percent"] = (hour_counts["count"] / len(df) * 100).round(2)
    hour_counts.to_csv(REPORT_DIR / f"{HOUR_COLUMN}_value_counts.csv", encoding="utf-8-sig")

    day_counts = days.value_counts(dropna=False).sort_index().rename("count").to_frame()
    day_counts["percent"] = (day_counts["count"] / len(df) * 100).round(2)
    day_counts.to_csv(REPORT_DIR / f"{DAY_COLUMN}_value_counts.csv", encoding="utf-8-sig")

    clean = pd.DataFrame({HOUR_COLUMN: hours, DAY_COLUMN: days}).dropna()
    clean[HOUR_COLUMN] = clean[HOUR_COLUMN].astype(int)
    clean[DAY_COLUMN] = clean[DAY_COLUMN].astype(int)
    matrix = pd.crosstab(clean[DAY_COLUMN], clean[HOUR_COLUMN])
    matrix = matrix.reindex(index=range(7), columns=range(24), fill_value=0)
    matrix.index = [DAY_LABELS.get(day, str(day)) for day in matrix.index]
    matrix.to_csv(REPORT_DIR / "start_hour_by_day_of_week_counts.csv", encoding="utf-8-sig")

    row_percent = matrix.div(matrix.sum(axis=1).replace(0, pd.NA), axis=0).fillna(0).round(4)
    row_percent.to_csv(REPORT_DIR / "start_hour_by_day_of_week_row_percent.csv", encoding="utf-8-sig")

    if "auto_approve" in df.columns:
        label_matrix = pd.crosstab([df[DAY_COLUMN], df[HOUR_COLUMN]], df["auto_approve"])
        label_matrix.to_csv(REPORT_DIR / "start_hour_day_of_week_by_auto_approve.csv", encoding="utf-8-sig")

    if plt is not None:
        fig, ax = plt.subplots(figsize=(12, 5))
        image = ax.imshow(matrix.values, aspect="auto", cmap="Blues")
        ax.set_title("start_hour by day_of_week")
        ax.set_xlabel("start_hour")
        ax.set_ylabel("day_of_week")
        ax.set_xticks(range(24))
        ax.set_yticks(range(7))
        ax.set_yticklabels(matrix.index)
        fig.colorbar(image, ax=ax, label="count")
        fig.tight_layout()
        fig.savefig(REPORT_DIR / "start_hour_by_day_of_week_heatmap.png", dpi=150)
        plt.close(fig)

    print(f"Saved {HOUR_COLUMN} and {DAY_COLUMN} distribution reports to {REPORT_DIR}")


if __name__ == "__main__":
    main()
