from pydantic import BaseModel
from typing import List, Dict, Any
from enum import Enum

class AnalyzeRequest(BaseModel):
    url: str
    language: str = "en"

class VerdictEnum(str, Enum):
    SAFE = "SAFE"
    SUSPICIOUS = "SUSPICIOUS"
    SCAM = "SCAM"

class VerdictResponse(BaseModel):
    verdict: VerdictEnum
    confidence: float
    color: str
    reasons: List[str]
    plain_language: str
    details: Dict[str, Any]

class HealthResponse(BaseModel):
    status: str
    version: str
