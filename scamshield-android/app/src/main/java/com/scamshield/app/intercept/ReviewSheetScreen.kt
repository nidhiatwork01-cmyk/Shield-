package com.scamshield.app.intercept

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.scamshield.app.api.AnalyzeRequest
import com.scamshield.app.api.ScamShieldApi
import com.scamshield.app.api.VerdictResponse
import com.scamshield.app.family.FamilyShareHelper
import com.scamshield.app.ui.theme.VerdictAmber
import com.scamshield.app.ui.theme.VerdictGreen
import com.scamshield.app.ui.theme.VerdictRed
import kotlinx.coroutines.launch

@Composable
fun ReviewSheetScreen(url: String, onGoBack: () -> Unit, onOpenAnyway: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var verdict by remember { mutableStateOf<VerdictResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(url) {
        try {
            verdict = ScamShieldApi.service.analyze(AnalyzeRequest(url = url))
        } catch (e: Exception) {
            isError = true
        } finally {
            isLoading = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Checking link safety...", style = MaterialTheme.typography.headlineLarge)
            } else if (isError || verdict == null) {
                VerdictUi(
                    color = VerdictAmber,
                    title = "Could not verify link",
                    message = "We couldn't reach our safety servers to check if this link is safe.",
                    url = url,
                    onGoBack = onGoBack,
                    onOpenAnyway = onOpenAnyway,
                    onAskFamily = { FamilyShareHelper.shareWithFamily(context, url, "UNKNOWN") }
                )
            } else {
                val safe = verdict!!.verdict == "SAFE"
                val scam = verdict!!.verdict == "SCAM"
                VerdictUi(
                    color = if (safe) VerdictGreen else if (scam) VerdictRed else VerdictAmber,
                    title = if (safe) "This link looks SAFE" else if (scam) "This link looks like a SCAM!" else "Be CAREFUL with this link",
                    message = verdict!!.plainLanguage,
                    url = url,
                    onGoBack = onGoBack,
                    onOpenAnyway = onOpenAnyway,
                    onAskFamily = { FamilyShareHelper.shareWithFamily(context, url, verdict!!.verdict) }
                )
            }
        }
    }
}

@Composable
fun VerdictUi(color: Color, title: String, message: String, url: String, onGoBack: () -> Unit, onOpenAnyway: () -> Unit, onAskFamily: () -> Unit) {
    Box(modifier = Modifier.size(96.dp).background(color, CircleShape))
    Spacer(modifier = Modifier.height(24.dp))
    Text(title, style = MaterialTheme.typography.headlineLarge, color = color)
    Spacer(modifier = Modifier.height(16.dp))
    Text(message, style = MaterialTheme.typography.bodyLarge)
    Spacer(modifier = Modifier.height(32.dp))
    
    Button(
        onClick = onGoBack,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerdictGreen)
    ) {
        Text("← Go Back", style = MaterialTheme.typography.headlineLarge)
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onAskFamily,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("👨👩👧 Ask My Family", style = MaterialTheme.typography.labelLarge)
    }
    Spacer(modifier = Modifier.height(16.dp))
    TextButton(
        onClick = onOpenAnyway,
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text("Open Anyway →", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelLarge)
    }
}
