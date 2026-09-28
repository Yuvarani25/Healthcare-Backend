"""Trend analytics over patient vitals using pandas, with matplotlib chart rendering."""

import io
from datetime import datetime, timedelta, timezone
from typing import Optional

import matplotlib

matplotlib.use("Agg")  # headless rendering; must be set before pyplot import

import matplotlib.pyplot as plt
import pandas as pd

from schemas import MetricSummary, PatientTrendResponse, Trend, VitalReading

NUMERIC_METRICS = ["heart_rate", "systolic_bp", "diastolic_bp", "spo2", "temperature"]

# Relative slope (per hour, as a fraction of the mean) beyond which a metric is no longer "stable".
TREND_THRESHOLD = 0.01


def _split_blood_pressure(value: Optional[str]) -> tuple[Optional[float], Optional[float]]:
    if not value or "/" not in value:
        return None, None
    systolic, _, diastolic = value.partition("/")
    try:
        return float(systolic), float(diastolic)
    except ValueError:
        return None, None


def to_dataframe(readings: list[VitalReading]) -> pd.DataFrame:
    rows = []
    for reading in readings:
        systolic, diastolic = _split_blood_pressure(reading.blood_pressure)
        rows.append(
            {
                "timestamp": reading.timestamp,
                "heart_rate": reading.heart_rate,
                "systolic_bp": systolic,
                "diastolic_bp": diastolic,
                "spo2": reading.spo2,
                "temperature": reading.temperature,
            }
        )

    frame = pd.DataFrame(rows, columns=["timestamp", *NUMERIC_METRICS])
    if frame.empty:
        return frame

    frame["timestamp"] = pd.to_datetime(frame["timestamp"], utc=True, errors="coerce")
    frame = frame.dropna(subset=["timestamp"]).sort_values("timestamp").reset_index(drop=True)
    return frame


def filter_window(frame: pd.DataFrame, window_hours: int) -> pd.DataFrame:
    if frame.empty:
        return frame
    cutoff = datetime.now(timezone.utc) - timedelta(hours=window_hours)
    return frame[frame["timestamp"] >= cutoff].reset_index(drop=True)


def _classify_trend(series: pd.Series, timestamps: pd.Series) -> Trend:
    values = series.dropna()
    if len(values) < 2:
        return Trend.UNKNOWN

    hours = (timestamps[values.index] - timestamps[values.index].iloc[0]).dt.total_seconds() / 3600.0
    if hours.iloc[-1] == hours.iloc[0]:
        return Trend.UNKNOWN

    slope = float(pd.Series(values.values).cov(pd.Series(hours.values)) / pd.Series(hours.values).var())
    mean = float(values.mean())
    if mean == 0:
        return Trend.UNKNOWN

    relative_slope = slope / abs(mean)
    if relative_slope > TREND_THRESHOLD:
        return Trend.RISING
    if relative_slope < -TREND_THRESHOLD:
        return Trend.FALLING
    return Trend.STABLE


def summarise(frame: pd.DataFrame) -> list[MetricSummary]:
    summaries: list[MetricSummary] = []
    for metric in NUMERIC_METRICS:
        if frame.empty:
            summaries.append(MetricSummary(metric=metric, count=0))
            continue

        series = frame[metric]
        values = series.dropna()
        if values.empty:
            summaries.append(MetricSummary(metric=metric, count=0))
            continue

        summaries.append(
            MetricSummary(
                metric=metric,
                count=int(values.count()),
                average=round(float(values.mean()), 2),
                minimum=float(values.min()),
                maximum=float(values.max()),
                latest=float(values.iloc[-1]),
                trend=_classify_trend(series, frame["timestamp"]),
            )
        )
    return summaries


def build_trend_report(
    patient_id: int, readings: list[VitalReading], window_hours: int
) -> PatientTrendResponse:
    frame = filter_window(to_dataframe(readings), window_hours)
    return PatientTrendResponse(
        patient_id=patient_id,
        window_hours=window_hours,
        sample_count=int(len(frame)),
        from_time=frame["timestamp"].iloc[0].to_pydatetime() if not frame.empty else None,
        to_time=frame["timestamp"].iloc[-1].to_pydatetime() if not frame.empty else None,
        metrics=summarise(frame),
    )


def render_chart(patient_id: int, readings: list[VitalReading], window_hours: int) -> bytes:
    """Returns a PNG time-series chart of the patient's vitals."""
    frame = filter_window(to_dataframe(readings), window_hours)

    figure, axes = plt.subplots(figsize=(10, 5))
    try:
        if frame.empty:
            axes.text(0.5, 0.5, "No vitals in the selected window", ha="center", va="center")
            axes.set_axis_off()
        else:
            for metric in NUMERIC_METRICS:
                if frame[metric].notna().any():
                    axes.plot(frame["timestamp"], frame[metric], marker="o", label=metric)
            axes.set_xlabel("Time")
            axes.set_ylabel("Value")
            axes.legend(loc="best")
            axes.grid(True, alpha=0.3)
            figure.autofmt_xdate()

        axes.set_title(f"Patient {patient_id} - vitals over last {window_hours}h")
        buffer = io.BytesIO()
        figure.savefig(buffer, format="png", dpi=100, bbox_inches="tight")
        return buffer.getvalue()
    finally:
        plt.close(figure)
