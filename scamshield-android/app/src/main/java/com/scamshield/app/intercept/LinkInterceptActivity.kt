package com.scamshield.app.intercept

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.scamshield.app.api.ScamShieldApi
import com.scamshield.app.api.VerdictResponse
import com.scamshield.app.api.AnalyzeRequest
import com.scamshield.app.ui.theme.ScamShieldTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LinkInterceptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val url = intent.dataString ?: return finish()
        
        setContent {
            ScamShieldTheme {
                ReviewSheetScreen(
                    url = url,
                    onGoBack = { finish() },
                    onOpenAnyway = { openInBrowser(url) }
                )
            }
        }
    }

    private fun openInBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            // Prevent our own app from catching this again by setting package or flags
        }
        val chooser = Intent.createChooser(browserIntent, "Open with").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(chooser)
        finish()
    }
}
