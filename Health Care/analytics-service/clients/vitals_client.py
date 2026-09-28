import os
from typing import Optional

import httpx

from schemas import VitalReading

VITALS_SERVICE_URL = os.getenv("VITALS_SERVICE_URL", "http://localhost:8082")
REQUEST_TIMEOUT_SECONDS = float(os.getenv("VITALS_TIMEOUT_SECONDS", "10"))


class VitalsServiceError(RuntimeError):
    """Raised when vitals-service cannot be reached or returns an error."""

    def __init__(self, message: str, status_code: int = 502) -> None:
        super().__init__(message)
        self.status_code = status_code


class VitalsClient:
    def __init__(self, base_url: str = VITALS_SERVICE_URL) -> None:
        self._base_url = base_url.rstrip("/")
        self._client: Optional[httpx.AsyncClient] = None

    async def start(self) -> None:
        self._client = httpx.AsyncClient(base_url=self._base_url, timeout=REQUEST_TIMEOUT_SECONDS)

    async def close(self) -> None:
        if self._client is not None:
            await self._client.aclose()
            self._client = None

    async def get_patient_vitals(self, patient_id: int) -> list[VitalReading]:
        if self._client is None:
            raise VitalsServiceError("Vitals client is not initialised")

        try:
            response = await self._client.get(f"/api/vitals/patient/{patient_id}")
        except httpx.RequestError as exc:
            raise VitalsServiceError(f"vitals-service is unreachable: {exc}") from exc

        if response.status_code == 404:
            raise VitalsServiceError(f"Patient {patient_id} not found in vitals-service", status_code=404)
        if response.status_code >= 400:
            raise VitalsServiceError(
                f"vitals-service returned {response.status_code}", status_code=502
            )

        return [VitalReading.model_validate(item) for item in response.json()]


vitals_client = VitalsClient()
