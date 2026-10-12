from __future__ import annotations
from datetime import datetime
from typing import Literal, Any
from uuid import UUID
from zoneinfo import ZoneInfo
from pydantic import BaseModel, ConfigDict, Field, model_validator

class Strict(BaseModel):
    model_config = ConfigDict(extra="forbid", allow_inf_nan=False)

class ChartInput(Strict):
    instant_utc: datetime
    timezone: str
    latitude: float | None = Field(default=None, ge=-90, le=90)
    longitude: float | None = Field(default=None, ge=-180, le=180)
    @model_validator(mode="after")
    def validate_input(self):
        utc(self.instant_utc)
        ZoneInfo(self.timezone)
        if (self.latitude is None) != (self.longitude is None):
            raise ValueError("Coordinates must be paired")
        return self

class Ruleset(Strict):
    id: str
    version: str
    source: str

class Palace(Strict):
    index: int = Field(ge=1, le=9)
    direction: str
    door: str | None
    star: str | None
    deity: str | None
    heaven_stem: str | None
    earth_stem: str | None

class Chart(Strict):
    schema_version: Literal[1] = 1
    chart_id: UUID
    status: Literal["BLOCKED", "VERIFIED", "SYNTHETIC"]
    input: ChartInput
    ruleset: Ruleset
    calendar: dict[str, str]
    dun_type: str | None
    ju_number: int | None = Field(ge=1, le=9)
    palaces: list[Palace]
    derivation_steps: list[str]
    warnings: list[str]
    input_hash: str = Field(pattern=r"^[0-9a-f]{64}$")
    @model_validator(mode="after")
    def validate_chart(self):
        if self.status == "BLOCKED":
            if self.palaces or self.dun_type is not None or self.ju_number is not None:
                raise ValueError("Blocked charts cannot contain calculated symbols")
        elif [p.index for p in self.palaces] != list(range(1, 10)):
            raise ValueError("Palaces must have fixed index order 1..9")
        if self.status == "SYNTHETIC" and self.ruleset.id != "synthetic-demo":
            raise ValueError("Synthetic charts require synthetic-demo ruleset")
        return self

def utc(value: datetime):
    if value.tzinfo is None or value.utcoffset().total_seconds() != 0:
        raise ValueError("Timestamp must be explicitly UTC")

class Action(Strict):
    kind: str = Field(min_length=1, max_length=64)
    cost: float = Field(ge=0)

class Event(Strict):
    event_id: UUID
    subject_pseudonym: str = Field(min_length=1, max_length=100)
    occurred_at_utc: datetime
    timezone: str
    location_coarse: str | None = None
    context: dict[str, Any]
    action: Action
    outcome: Literal[0, 1] | None
    outcome_at_utc: datetime | None
    source: str
    consent_scope: Literal["research-export", "synthetic-example"]
    chart_ref: Chart | None = None
    dataset_version: Literal["1"] = "1"
    @model_validator(mode="after")
    def validate_event(self):
        utc(self.occurred_at_utc)
        ZoneInfo(self.timezone)
        if self.outcome_at_utc:
            utc(self.outcome_at_utc)
            if self.outcome_at_utc < self.occurred_at_utc:
                raise ValueError("Outcome precedes action")
        if self.outcome is not None and self.outcome_at_utc is None:
            raise ValueError("Known outcomes need observation timestamps")
        if self.consent_scope == "synthetic-example" and self.source != "synthetic-demo":
            raise ValueError("Real records require explicit research consent")
        if self.chart_ref:
            if self.chart_ref.input.instant_utc > self.occurred_at_utc:
                raise ValueError("Future chart leakage")
            if self.chart_ref.input.timezone != self.timezone:
                raise ValueError("Chart timezone mismatch")
            if self.chart_ref.status == "SYNTHETIC" and self.source != "synthetic-demo":
                raise ValueError("Synthetic chart in real record")
        return self
