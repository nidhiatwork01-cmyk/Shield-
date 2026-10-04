from google import genai
from config import settings
import logging

logger = logging.getLogger(__name__)

_client = None

def _get_client():
    global _client
    if _client is None and settings.GOOGLE_GEMINI_API_KEY:
        try:
            _client = genai.Client(api_key=settings.GOOGLE_GEMINI_API_KEY)
        except Exception as e:
            logger.error(f"Failed to initialize Gemini client: {e}")
    return _client

async def generate_explanation(reasons: list[str], language: str) -> str:
    fallback_map = {
        "en": "This website has multiple suspicious signals. Be careful and avoid sharing personal information.",
        "hi": "इस वेबसाइट के कई संदेहास्पद संकेत हैं। सावधान रहें और व्यक्तिगत जानकारी साझा करने से बचें।",
        "te": "ఈ వెబ్‌సైట్‌కు పలు అనుమానాస్పద సంకేతాలు ఉన్నాయి. జాగ్రత్తగా ఉండండి మరియు వ్యక్తిగత సమాచారాన్ని పంచుకోవద్దు."
    }
    fallback = fallback_map.get(language, fallback_map["en"])
    
    if not reasons:
        return "This website appears safe based on our automated checks, but always stay vigilant."
        
    client = _get_client()
    if not client:
        return fallback

    language_name = {"en": "English", "hi": "Hindi", "te": "Telugu"}.get(language, "English")

    prompt = (
        f"You are a cybersecurity assistant. I have flagged a link as dangerous for these reasons: {', '.join(reasons)}. "
        f"Generate exactly ONE simple sentence explaining why this is dangerous. "
        f"Explain it like talking to a 70-year-old who doesn't understand technology. "
        f"Respond in {language_name}. Do not use any technical jargon."
    )

    try:
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=prompt
        )
        text = response.text.strip()
        if not text:
            return fallback
        return text
    except Exception as e:
        logger.error(f"Gemini API error: {e}")
        return fallback
