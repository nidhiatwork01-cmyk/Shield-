package com.scamshield.app.api

import com.google.gson.annotations.SerializedName

data class AnalyzeRequest(
    val url: String,
    val language: String = "en"
)

data class VerdictResponse(
    val verdict: String, // "SAFE", "SUSPICIOUS", "SCAM"
    val confidence: Float,
    val color: String,
    val reasons: List<String>,
    @SerializedName("plain_language")
    val plainLanguage: String,
    val details: VerdictDetails
)

data class VerdictDetails(
    val domain: String?,
    @SerializedName("domain_age_days")
    val domainAgeDays: Int?,
    @SerializedName("is_shortened")
    val isShortened: Boolean?,
    @SerializedName("safe_browsing_flagged")
    val safeBrowsingFlagged: Boolean?,
    @SerializedName("brand_mimicry")
    val brandMimicry: String?,
    @SerializedName("suspicious_tld")
    val suspiciousTld: Boolean?
)

data class HealthResponse(
    val status: String,
    val version: String
)
