package com.scamshield.app.listener

import android.app.Notification
import android.os.Bundle
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

    // Packages to ignore (system services, own app)
    private val ignoredPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.vending",
        "com.google.android.gms",
        "com.scamshield.app"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val pkg = sbn.packageName ?: return
        if (ignoredPackages.contains(pkg)) return

        val extras = sbn.notification.extras ?: return
        val allText = extractAllText(extras)

        if (allText.isBlank()) return

        val urls = UrlExtractor.extractUrls(allText)
        if (urls.isNotEmpty()) {
            Log.d("ScamShield", "Found URLs in notification from $pkg: $urls")
            for (url in urls) {
                analyzeUrl(url)
            }
        }
    }

    private fun extractAllText(extras: Bundle): String {
        val sb = StringBuilder()

        // 1. Title
        extras.getCharSequence(Notification.EXTRA_TITLE)?.let { sb.append(it).append(" ") }
        
        // 2. Standard Text
        extras.getCharSequence(Notification.EXTRA_TEXT)?.let { sb.append(it).append(" ") }
        
        // 3. Big Text (Expanded notifications)
        extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.let { sb.append(it).append(" ") }
        
        // 4. Sub Text
        extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.let { sb.append(it).append(" ") }

        // 5. Text Lines (InboxStyle)
        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach { line ->
            line?.let { sb.append(it).append(" ") }
        }

        // 6. MessagingStyle messages (WhatsApp, Telegram)
        try {
            val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            messages?.forEach { msg ->
                if (msg is Bundle) {
                    msg.getCharSequence("text")?.let { sb.append(it).append(" ") }
                }
            }
        } catch (e: Exception) {
            // Ignore parcelable parsing issues
        }

        return sb.toString()
    }

    private fun analyzeUrl(url: String) {
        scope.launch {
            try {
                Log.d("ScamShield", "Analyzing URL: $url")
                val response = ScamShieldApi.service.analyze(
                    AnalyzeRequest(url = url)
                )
                Log.d("ScamShield", "Verdict for $url: ${response.verdict} (${response.color})")
                // Updates the shared reactive StateFlow for the floating bubble
                ScamShieldState.updateVerdict(url, response)
            } catch (e: Exception) {
                Log.e("ScamShield", "Error calling analyze API for $url", e)
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("ScamShield", "ScamShield Notification Listener connected successfully!")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("ScamShield", "Notification listener disconnected")
    }
}
