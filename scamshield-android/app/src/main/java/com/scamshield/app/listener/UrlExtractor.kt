package com.scamshield.app.listener

import android.util.Patterns

object UrlExtractor {
    fun extractUrls(text: String): List<String> {
        val urls = mutableListOf<String>()
        val matcher = Patterns.WEB_URL.matcher(text)
        while (matcher.find()) {
            val url = matcher.group()
            if (!isSafeDomain(url)) {
                urls.add(url)
            }
        }
        return urls
    }

    private fun isSafeDomain(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.contains("google.com") || lowerUrl.contains("youtube.com")
    }
}
