package com.scamshield.app.listener

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.scamshield.app.ScamShieldState
import com.scamshield.app.api.AnalyzeRequest
import com.scamshield.app.api.ScamShieldApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ScamShieldNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val supportedPackages = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "com.google.android.apps.messaging",
        "org.telegram.messenger",
        "com.google.android.gm",
        "org.thoughtcrime.securesms",
        "com.instagram.android",
        "com.facebook.orca"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        if (!supportedPackages.contains(sbn.packageName)) return

        val extras = sbn.notification.extras
        // Check both EXTRA_TEXT and EXTRA_BIG_TEXT for longer messages
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString() ?: return

        val urls = UrlExtractor.extractUrls(text)
        if (urls.isNotEmpty()) {
            Log.d("ScamShield", "Found URLs in ${sbn.packageName}: $urls")
            analyzeUrl(urls.first())
        }
    }

    private fun analyzeUrl(url: String) {
        scope.launch {
            try {
                Log.d("ScamShield", "Analyzing URL: $url")
                val normalizedUrl = if (!url.startsWith("http")) "https://$url" else url
                val response = ScamShieldApi.service.analyze(
                    AnalyzeRequest(url = normalizedUrl)
                )
                // Update the bubble state — this triggers UI changes in the overlay
                ScamShieldState.updateVerdict(normalizedUrl, response)
                Log.d("ScamShield", "Verdict: ${response.verdict} for $url")
            } catch (e: Exception) {
                Log.e("ScamShield", "Error analyzing URL: $url", e)
                // Don't crash — bubble stays in current state
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("ScamShield", "Notification listener disconnected")
    }
}
