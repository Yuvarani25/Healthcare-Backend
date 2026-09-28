import os
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI

from clients.vitals_client import vitals_client
from routers import analytics


@asynccontextmanager
async def lifespan(_: FastAPI):
    await vitals_client.start()
    yield
    await vitals_client.close()


app = FastAPI(
    title="Analytics Service",
    description="Smart Patient Monitoring System - vitals trend analytics and charts",
    version="1.0.0",
    lifespan=lifespan,
)

app.include_router(analytics.router)


@app.get("/health", tags=["health"])
async def health() -> dict[str, str]:
    return {"status": "UP", "service": "analytics-service"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=int(os.getenv("PORT", "8000")))
