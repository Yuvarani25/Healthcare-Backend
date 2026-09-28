from datetime import datetime
from enum import Enum
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field


def to_camel(value: str) -> str:
    head, *tail = value.split("_")
    return head + "".join(part.capitalize() for part in tail)


class CamelModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class VitalReading(CamelModel):
    """A single reading as returned by vitals-service."""

    vital_id: Optional[int] = None
    patient_id: int
    heart_rate: Optional[float] = None
    blood_pressure: Optional[str] = None
    spo2: Optional[float] = None
    temperature: Optional[float] = None
    timestamp: datetime


class Trend(str, Enum):
    RISING = "RISING"
    FALLING = "FALLING"
    STABLE = "STABLE"
    UNKNOWN = "UNKNOWN"


class MetricSummary(CamelModel):
    metric: str
    count: int
    average: Optional[float] = None
    minimum: Optional[float] = None
    maximum: Optional[float] = None
    latest: Optional[float] = None
    trend: Trend = Trend.UNKNOWN


class PatientTrendResponse(CamelModel):
    patient_id: int
    window_hours: int
    sample_count: int
    from_time: Optional[datetime] = None
    to_time: Optional[datetime] = None
    metrics: list[MetricSummary] = Field(default_factory=list)


class ErrorResponse(CamelModel):
    detail: str
