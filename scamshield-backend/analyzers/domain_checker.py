import whois
import datetime
import urllib.parse
import asyncio
import logging
from typing import Dict, Any, Optional

logger = logging.getLogger(__name__)

def extract_domain(url: str) -> str:
    try:
        parsed = urllib.parse.urlparse(url)
        # Handle cases without scheme
        if not parsed.netloc:
            parsed = urllib.parse.urlparse(f"http://{url}")
        domain = parsed.netloc.split(':')[0]
        if domain.startswith('www.'):
            domain = domain[4:]
        return domain
    except Exception:
        return ""

def _get_whois_sync(domain: str) -> Dict[str, Any]:
    try:
        w = whois.whois(domain)
        creation_date = w.creation_date
        if isinstance(creation_date, list):
            creation_date = creation_date[0]
            
        age_days = None
        if isinstance(creation_date, datetime.datetime):
            age_days = (datetime.datetime.now() - creation_date).days
            
        return {
            "domain_name": domain,
            "creation_date": creation_date.isoformat() if isinstance(creation_date, datetime.datetime) else None,
            "registrar": w.registrar,
            "domain_age_days": age_days
        }
    except Exception as e:
        logger.error(f"WHOIS error for {domain}: {e}")
        return {"domain_name": domain, "domain_age_days": None}

async def check_domain(url: str) -> Dict[str, Any]:
    domain = extract_domain(url)
    if not domain:
        return {"domain_name": "", "domain_age_days": None}
    
    # Run blocking whois lookup in threadpool
    loop = asyncio.get_event_loop()
    result = await loop.run_in_executor(None, _get_whois_sync, domain)
    return result
