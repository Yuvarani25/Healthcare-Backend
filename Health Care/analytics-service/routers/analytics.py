from fastapi import APIRouter, HTTPException, Path, Query
from fastapi.responses import Response

from clients.vitals_client import VitalsServiceError, vitals_client
from schemas import PatientTrendResponse
from services import trend_service

router = APIRouter(prefix="/api/analytics", tags=["analytics"])


async def _load_readings(patient_id: int):
    try:
        return await vitals_client.get_patient_vitals(patient_id)
    except VitalsServiceError as exc:
        raise HTTPException(status_code=exc.status_code, detail=str(exc)) from exc


@router.get("/patients/{patient_id}/trends", response_model=PatientTrendResponse)
async def get_patient_trends(
    patient_id: int = Path(..., ge=1),
    window_hours: int = Query(24, ge=1, le=720, alias="windowHours"),
) -> PatientTrendResponse:
    readings = await _load_readings(patient_id)
    return trend_service.build_trend_report(patient_id, readings, window_hours)


@router.get(
    "/patients/{patient_id}/chart",
    responses={200: {"content": {"image/png": {}}}},
    response_class=Response,
)
async def get_patient_chart(
    patient_id: int = Path(..., ge=1),
    window_hours: int = Query(24, ge=1, le=720, alias="windowHours"),
) -> Response:
    readings = await _load_readings(patient_id)
    png = trend_service.render_chart(patient_id, readings, window_hours)
    return Response(content=png, media_type="image/png")
