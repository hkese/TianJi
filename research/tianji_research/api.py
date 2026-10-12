import json
import os
import secrets
from pathlib import Path
from fastapi import FastAPI, Depends, Header, HTTPException
from pydantic import BaseModel, Field
from .models import Event
from .data import dataset
from .experiment import evaluate
from .registry import register, approve

def create_app(directory: Path = Path("output"), token: str | None = None):
    expected = token or os.environ.get("TIANJI_RESEARCH_TOKEN", "")
    if len(expected) < 24:
        raise ValueError("Configure TIANJI_RESEARCH_TOKEN with at least 24 characters")
    directory.mkdir(parents=True, exist_ok=True)
    app = FastAPI(title="TianJi Research — exploratory only")
    def auth(authorization: str = Header(default="")):
        if not secrets.compare_digest(authorization, "Bearer " + expected):
            raise HTTPException(401, "Authentication required")
    class ImportRequest(BaseModel):
        events: list[Event] = Field(max_length=100000)
    class ExperimentRequest(BaseModel):
        dataset_hash: str = Field(pattern="^[0-9a-f]{64}$")
        seed: int = 42
    class ApprovalRequest(BaseModel):
        reviewer: str = Field(min_length=1)
    @app.get("/healthz")
    def health():
        return {"status": "ok", "evidence": "RESEARCH_ONLY"}
    @app.post("/v1/datasets/import", dependencies=[Depends(auth)])
    def ingest(body: ImportRequest):
        digest, text = dataset(body.events)
        path = directory / (digest + ".jsonl")
        if not path.exists():
            with path.open("x") as stream:
                stream.write(text)
        return {"dataset_hash": digest, "count": len(body.events)}
    @app.post("/v1/experiments", dependencies=[Depends(auth)])
    def experiment(body: ExperimentRequest):
        path = directory / (body.dataset_hash + ".jsonl")
        if not path.exists():
            raise HTTPException(404, "Dataset not found")
        events = [Event.model_validate_json(line) for line in path.read_text().splitlines()]
        try:
            report = evaluate(events, body.seed)
        except ValueError as error:
            raise HTTPException(422, str(error)) from error
        return register(report, directory / "models")
    def model(identifier: str):
        import re
        if not re.fullmatch("[0-9a-f]{64}", identifier):
            raise HTTPException(404, "Not found")
        path = directory / "models" / (identifier + ".json")
        if not path.exists():
            raise HTTPException(404, "Not found")
        return json.loads(path.read_text())
    @app.get("/v1/experiments/{identifier}", dependencies=[Depends(auth)])
    @app.get("/v1/experiments/{identifier}/metrics", dependencies=[Depends(auth)])
    def metrics(identifier: str):
        return model(identifier)["report"]
    @app.get("/v1/models", dependencies=[Depends(auth)])
    def models():
        return [json.loads(p.read_text()) for p in sorted((directory / "models").glob("*.json"))]
    @app.post("/v1/models/{identifier}/approve", dependencies=[Depends(auth)])
    def approval(identifier: str, body: ApprovalRequest):
        try:
            reviewed = approve(model(identifier), body.reviewer)
        except ValueError as error:
            raise HTTPException(422, str(error)) from error
        # Append review separately; preserve immutable original metadata.
        with (directory / "models" / (identifier + ".review.json")).open("x") as stream:
            stream.write(json.dumps(reviewed))
        return reviewed
    return app
