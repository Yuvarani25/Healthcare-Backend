from datetime import datetime, timedelta, timezone

import pytest

from schemas import Trend, VitalReading
from services import trend_service


def reading(minutes_ago: int, heart_rate=None, blood_pressure=None, spo2=None, temperature=None):
    return VitalReading(
        patientId=1,
        heartRate=heart_rate,
        bloodPressure=blood_pressure,
        spo2=spo2,
        temperature=temperature,
        timestamp=datetime.now(timezone.utc) - timedelta(minutes=minutes_ago),
    )


def metric(report, name):
    return next(m for m in report.metrics if m.metric == name)


@pytest.mark.parametrize(
    "value,expected",
    [
        ("120/80", (120.0, 80.0)),
        ("90/60", (90.0, 60.0)),
        (None, (None, None)),
        ("", (None, None)),
        ("120", (None, None)),
        ("abc/def", (None, None)),
    ],
)
def test_split_blood_pressure(value, expected):
    assert trend_service._split_blood_pressure(value) == expected


def test_dataframe_is_sorted_by_timestamp():
    frame = trend_service.to_dataframe([reading(10, heart_rate=80), reading(60, heart_rate=70)])
    assert list(frame["heart_rate"]) == [70, 80]


def test_summary_computes_statistics():
    readings = [reading(50 - i * 10, heart_rate=70 + i * 5) for i in range(5)]
    report = trend_service.build_trend_report(1, readings, window_hours=24)

    hr = metric(report, "heart_rate")
    assert report.sample_count == 5
    assert hr.count == 5
    assert hr.minimum == 70
    assert hr.maximum == 90
    assert hr.average == 80.0
    assert hr.latest == 90


def test_rising_and_falling_trends_are_detected():
    rising = [reading(50 - i * 10, heart_rate=70 + i * 5) for i in range(5)]
    falling = [reading(50 - i * 10, heart_rate=110 - i * 5) for i in range(5)]

    assert metric(trend_service.build_trend_report(1, rising, 24), "heart_rate").trend is Trend.RISING
    assert metric(trend_service.build_trend_report(1, falling, 24), "heart_rate").trend is Trend.FALLING


def test_constant_series_is_stable():
    readings = [reading(50 - i * 10, spo2=98.0) for i in range(5)]
    assert metric(trend_service.build_trend_report(1, readings, 24), "spo2").trend is Trend.STABLE


def test_single_sample_has_unknown_trend():
    report = trend_service.build_trend_report(1, [reading(5, heart_rate=75)], 24)
    assert metric(report, "heart_rate").trend is Trend.UNKNOWN


def test_window_filter_excludes_old_readings():
    readings = [reading(minutes_ago=10, heart_rate=80), reading(minutes_ago=60 * 30, heart_rate=60)]
    report = trend_service.build_trend_report(1, readings, window_hours=24)

    assert report.sample_count == 1
    assert metric(report, "heart_rate").latest == 80


def test_blood_pressure_is_split_into_two_metrics():
    readings = [reading(20, blood_pressure="130/85"), reading(10, blood_pressure="128/84")]
    report = trend_service.build_trend_report(1, readings, 24)

    assert metric(report, "systolic_bp").latest == 128
    assert metric(report, "diastolic_bp").latest == 84


def test_empty_readings_produce_zero_counts():
    report = trend_service.build_trend_report(1, [], 24)

    assert report.sample_count == 0
    assert report.from_time is None
    assert all(m.count == 0 for m in report.metrics)


def test_chart_returns_png_bytes():
    readings = [reading(50 - i * 10, heart_rate=70 + i, spo2=97.0) for i in range(5)]
    png = trend_service.render_chart(1, readings, 24)

    assert png.startswith(b"\x89PNG\r\n\x1a\n")


def test_chart_renders_placeholder_when_no_data():
    png = trend_service.render_chart(1, [], 24)
    assert png.startswith(b"\x89PNG\r\n\x1a\n")
