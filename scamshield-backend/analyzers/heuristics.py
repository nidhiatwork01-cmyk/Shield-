import urllib.parse
import re
from typing import Dict, Any, Tuple

KNOWN_SHORTENERS = {
    "bit.ly", "tinyurl.com", "t.co", "goo.gl", "rebrand.ly", 
    "cutt.ly", "short.io", "is.gd", "buff.ly", "ow.ly"
}

KNOWN_BRANDS = {
    "sbi": "onlinesbi.sbi",
    "hdfc": "hdfcbank.com",
    "icici": "icicibank.com",
    "paytm": "paytm.com",
    "phonepe": "phonepe.com",
    "gpay": "pay.google.com",
    "amazon": "amazon.com",
    "flipkart": "flipkart.com",
    "netflix": "netflix.com"
}

SUSPICIOUS_TLDS = {
    ".xyz", ".top", ".click", ".link", ".info", 
    ".club", ".online", ".site", ".work", ".buzz"
}

URGENCY_KEYWORDS = [
    "otp", "verify", "urgent", "suspend", "block", 
    "expire", "winner", "prize", "reward", "lottery"
]

def analyze_url_heuristics(url: str) -> Dict[str, Any]:
    try:
        parsed = urllib.parse.urlparse(url)
        if not parsed.netloc:
            parsed = urllib.parse.urlparse(f"http://{url}")
        
        domain = parsed.netloc.split(':')[0].lower()
        if domain.startswith('www.'):
            domain = domain[4:]
            
        path = parsed.path.lower()
        query = parsed.query.lower()
        
        is_shortened = domain in KNOWN_SHORTENERS
        
        brand_mimicry = None
        for brand, official_domain in KNOWN_BRANDS.items():
            if brand in domain and official_domain not in domain:
                brand_mimicry = brand
                break
                
        suspicious_tld = any(domain.endswith(tld) for tld in SUSPICIOUS_TLDS)
        
        url_text_to_check = path + query
        has_urgency_keywords = any(kw in url_text_to_check for kw in URGENCY_KEYWORDS)
        
        is_ip_domain = bool(re.match(r'^(\d{1,3}\.){3}\d{1,3}$', domain))
        
        subdomains = domain.split('.')
        excessive_subdomains = len(subdomains) > 3 
        
        return {
            "is_shortened": is_shortened,
            "brand_mimicry": brand_mimicry,
            "suspicious_tld": suspicious_tld,
            "urgency_keywords": has_urgency_keywords,
            "ip_as_domain": is_ip_domain,
            "excessive_subdomains": excessive_subdomains
        }
    except Exception:
        return {
            "is_shortened": False,
            "brand_mimicry": None,
            "suspicious_tld": False,
            "urgency_keywords": False,
            "ip_as_domain": False,
            "excessive_subdomains": False
        }
