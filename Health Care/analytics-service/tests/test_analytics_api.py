from datetime import datetime, timedelta, timezone

import pytest
from fastapi.testclient import TestClient

from clients import vitals_client as vitals_client_module
from clients.vitals_client import VitalsServiceError
from main import app
from schemas import VitalReading


@pytest.fixture
def client():
    with TestClient(app) as test_client:
        yield test_client


def sample_readings():
    now = datetime.now(timezone.utc)
    return [
        VitalReading(
            patientId=1,
            heartRate=70 + i,
            bloodPressure="120/80",
            spo2=98.0,
            temperature=36.8,
            timestamp=now - timedelta(minutes=10 * (5 - i)),
        )
        for i in range(5)
    ]


def test_health_endpoint(client):
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "UP"


def test_trends_endpoint_returns_camel_case_payload(client, monkeypatch):
    async def fake_get(patient_id: int):
        return sample_readings()

    monkeypatch.setattr(vitals_client_module.vitals_client, "get_patient_vitals", fake_get)

    response = client.get("/api/analytics/patients/1/trends?windowHours=24")

    assert response.status_code == 200
    body = response.json()
    assert body["patientId"] == 1
    assert body["sampleCount"] == 5
    assert any(m["metric"] == "heart_rate" for m in body["metrics"])


def test_chart_endpoint_returns_png(client, monkeypatch):
    async def fake_get(patient_id: int):
        return sample_readings()

    monkeypatch.setattr(vitals_client_module.vitals_client, "get_patient_vitals", fake_get)

    response = client.get("/api/analytics/patients/1/chart")

    assert response.status_code == 200
    assert response.headers["content-type"] == "image/png"
    assert response.content.startswith(b"\x89PNG\r\n\x1a\n")


def test_unknown_patient_is_propagated_as_404(client, monkeypatch):
    async def fake_get(patient_id: int):
        raise VitalsServiceError("Patient not found", status_code=404)

    monkeypatch.setattr(vitals_client_module.vitals_client, "get_patient_vitals", fake_get)

    response = client.get("/api/analytics/patients/999/trends")

    assert response.status_code == 404


def test_vitals_service_outage_returns_502(client, monkeypatch):
    async def fake_get(patient_id: int):
        raise VitalsServiceError("unreachable")

    monkeypatch.setattr(vitals_client_module.vitals_client, "get_patient_vitals", fake_get)

    response = client.get("/api/analytics/patients/1/trends")

    assert response.status_code == 502
