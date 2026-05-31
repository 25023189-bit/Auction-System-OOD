# Auction AI AutoApprove

## Runtime files

- `predict_from_input.py`: Java-facing inference script. It reads JSON/CSV input and writes either rich prediction JSON or the Java bridge boolean output.
- `input_ap.json`: Java bridge input sample/state file.
- `output_ap.json`: Java bridge output created at runtime by `predict_from_input.py`.
- `auction_model.pkl`: trained Logistic Regression pipeline used by inference.
- `auction_logreg_bow.pkl`: older trained artifact retained for compatibility checks/debugging.
- `diagnostics.py`: logging and path diagnostics shared by the inference script.
- `prediction_results.json`: rich debug output from local/manual inference runs.
- `status/`: runtime/debug logs and health-check output.

## Training files

- `Training_Data.csv`: current training dataset.
- `train_logistic_regression.py`: retrains `auction_model.pkl` from `Training_Data.csv`.
- `metrics.json`: latest training metrics.
- `test_samples.json`: smoke-test samples for inference and `Auction_AI/health_check.py`.

## Install dependencies

```bash
pip install -r ../requirements.txt
```

## Retrain model

```bash
python train_logistic_regression.py
```

## Run inference smoke test

```bash
python predict_from_input.py --input test_samples.json --output prediction_results.json
```

When Java calls this module, it passes `--input input_ap.json --output output_ap.json`. In that bridge mode, `output_ap.json` must contain only `true` or `false`; rich diagnostics are kept in `prediction_results.json`.
