package com.scamshield.app.listener

import java.util.regex.Pattern

object UrlExtractor {

    // Matches standard http(s) URLs as well as common domain patterns (e.g. site.xyz/path, bit.ly/abc)
    private val URL_PATTERN = Pattern.compile(
        "((https?://)?[a-zA-Z0-9][-a-zA-Z0-9]*(\\.[a-zA-Z0-9][-a-zA-Z0-9]*)+(:\\d+)?(/[^\\s]*)?)",
        Pattern.CASE_INSENSITIVE
    )

    fun extractUrls(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()

        val urls = mutableListOf<String>()
        val matcher = URL_PATTERN.matcher(text)

        while (matcher.find()) {
            val candidate = matcher.group().trimEnd('.', ',', '!', '?', ';', ':', ')', ']')
            // Avoid false positives like version numbers (e.g. 1.0.0) or short filenames
            if (candidate.contains(".") && !candidate.matches(Regex("^[0-9.]+$")) && candidate.length > 4) {
                val normalized = if (!candidate.startsWith("http://", ignoreCase = true) && 
                                     !candidate.startsWith("https://", ignoreCase = true)) {
                    "https://$candidate"
                } else {
                    candidate
                }
                urls.add(normalized)
            }
        }
        return urls
    }
}
