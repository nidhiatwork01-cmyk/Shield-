package com.scamshield.app

import com.scamshield.app.api.VerdictResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton state holder for bubble communication.
 * The NotificationListener writes verdict state here,
 * the BubbleView reads it reactively via StateFlow.
 */
object ScamShieldState {
    
    enum class BubbleStatus {
        IDLE,       // Green - all good
        SUSPICIOUS, // Amber - be careful
        SCAM        // Red - danger
    }
    
    data class BubbleState(
        val status: BubbleStatus = BubbleStatus.IDLE,
        val lastVerdict: VerdictResponse? = null,
        val lastUrl: String? = null,
        val message: String = "Protected"
    )
    
    private val _state = MutableStateFlow(BubbleState())
    val state: StateFlow<BubbleState> = _state.asStateFlow()
    
    fun updateVerdict(url: String, verdict: VerdictResponse) {
        val status = when (verdict.color) {
            "red" -> BubbleStatus.SCAM
            "amber" -> BubbleStatus.SUSPICIOUS
            else -> BubbleStatus.IDLE
        }
        _state.value = BubbleState(
            status = status,
            lastVerdict = verdict,
            lastUrl = url,
            message = when (status) {
                BubbleStatus.SCAM -> "⚠️ Scam link detected!"
                BubbleStatus.SUSPICIOUS -> "⚠️ Suspicious link"
                BubbleStatus.IDLE -> "✅ Link looks safe"
            }
        )
    }
    
    fun reset() {
        _state.value = BubbleState()
    }
}
