import asyncio
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from models import AnalyzeRequest, VerdictResponse, HealthResponse, VerdictEnum
from analyzers.safe_browsing import check_safe_browsing
from analyzers.domain_checker import check_domain
from analyzers.heuristics import analyze_url_heuristics
from analyzers.gemini_explainer import generate_explanation
import uvicorn
import os
import sys

# Ensure backend folder is in path for imports
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

app = FastAPI(title="ScamShield API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health", response_model=HealthResponse)
async def health_check():
    return HealthResponse(status="ok", version="1.0.0")

@app.post("/analyze", response_model=VerdictResponse)
async def analyze_url(request: AnalyzeRequest):
    # Run analyzers concurrently with timeout
    try:
        results = await asyncio.gather(
            asyncio.wait_for(check_safe_browsing(request.url), timeout=5.0),
            asyncio.wait_for(check_domain(request.url), timeout=5.0),
            return_exceptions=True
        )
    except Exception:
        results = [False, {"domain_name": "", "domain_age_days": None}]
        
    safe_browsing_result = results[0] if not isinstance(results[0], Exception) else False
    domain_result = results[1] if not isinstance(results[1], Exception) else {"domain_name": "", "domain_age_days": None}
    
    # Heuristics is sync and fast
    heuristics_result = analyze_url_heuristics(request.url)
    
    score = 0.0
    reasons = []
    
    if safe_browsing_result:
        score += 0.6
        reasons.append("Flagged by Google Safe Browsing as dangerous")
        
    domain_age = domain_result.get("domain_age_days")
    if domain_age is not None:
        if domain_age < 7:
            score += 0.3
            reasons.append(f"Domain created very recently ({domain_age} days ago)")
        elif domain_age < 30:
            score += 0.15
            reasons.append(f"Domain created recently ({domain_age} days ago)")
            
    if heuristics_result.get("brand_mimicry"):
        score += 0.3
        reasons.append(f"Mimics {heuristics_result['brand_mimicry'].upper()} official domain")
        
    if heuristics_result.get("suspicious_tld"):
        score += 0.15
        reasons.append("Uses a suspicious top-level domain (TLD)")
        
    if heuristics_result.get("is_shortened"):
        score += 0.2
        reasons.append("Uses URL shortener to hide real destination")
        
    if heuristics_result.get("urgency_keywords"):
        score += 0.15
        reasons.append("Contains urgency or phishing keywords in URL")
        
    if heuristics_result.get("ip_as_domain"):
        score += 0.25
        reasons.append("Uses IP address instead of domain name")
        
    if heuristics_result.get("excessive_subdomains"):
        score += 0.1
        reasons.append("Contains excessive subdomains")
        
    # Determine verdict
    if score >= 0.6:
        verdict = VerdictEnum.SCAM
        color = "red"
    elif score >= 0.3:
        verdict = VerdictEnum.SUSPICIOUS
        color = "amber"
    else:
        verdict = VerdictEnum.SAFE
        color = "green"
        
    # Generate explanation
    plain_language = await generate_explanation(reasons, request.language)
    
    details = {
        "domain": domain_result.get("domain_name"),
        "domain_age_days": domain_age,
        "is_shortened": heuristics_result.get("is_shortened"),
        "safe_browsing_flagged": safe_browsing_result,
        "brand_mimicry": heuristics_result.get("brand_mimicry"),
        "suspicious_tld": heuristics_result.get("suspicious_tld")
    }
    
    # Confidence in the verdict: high score = high confidence it's a scam,
    # low score = high confidence it's safe
    if verdict == VerdictEnum.SAFE:
        confidence = max(0.85, 1.0 - score)
    else:
        confidence = min(score + 0.3, 0.99)
    
    return VerdictResponse(
        verdict=verdict,
        confidence=confidence,
        color=color,
        reasons=reasons,
        plain_language=plain_language,
        details=details
    )

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
