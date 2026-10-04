import httpx
import logging
from config import settings

logger = logging.getLogger(__name__)

async def check_safe_browsing(url: str) -> bool:
    if not settings.GOOGLE_SAFE_BROWSING_API_KEY:
        logger.warning("GOOGLE_SAFE_BROWSING_API_KEY not set. Skipping check.")
        return False

    api_url = f"https://safebrowsing.googleapis.com/v4/threatMatches:find?key={settings.GOOGLE_SAFE_BROWSING_API_KEY}"
    payload = {
        "client": {
            "clientId": "scamshield",
            "clientVersion": "1.0.0"
        },
        "threatInfo": {
            "threatTypes": ["SOCIAL_ENGINEERING", "MALWARE", "UNWANTED_SOFTWARE"],
            "platformTypes": ["ANY_PLATFORM"],
            "threatEntryTypes": ["URL"],
            "threatEntries": [
                {"url": url}
            ]
        }
    }

    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(api_url, json=payload, timeout=5.0)
            if response.status_code == 200:
                data = response.json()
                if "matches" in data and len(data["matches"]) > 0:
                    return True
            return False
    except Exception as e:
        logger.error(f"Safe Browsing API error: {e}")
        return False
