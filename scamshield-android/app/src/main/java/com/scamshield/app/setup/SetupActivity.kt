package com.scamshield.app.setup

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.scamshield.app.MainActivity
import com.scamshield.app.ui.theme.ScamShieldTheme

class SetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScamShieldTheme {
                SetupScreen(
                    onComplete = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SetupScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    var notifGranted by remember { mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)) }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Setup ScamShield", style = MaterialTheme.typography.headlineLarge)
        
        Text("We need two permissions to protect you from scam links.", style = MaterialTheme.typography.bodyLarge)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("1. Read your notifications", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) {
                    Text(if (notifGranted) "Granted ✓" else "Grant Permission", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2. Show over other apps", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) {
                    Text(if (overlayGranted) "Granted ✓" else "Grant Permission", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onComplete,
            enabled = notifGranted && overlayGranted,
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Text("Continue", style = MaterialTheme.typography.labelLarge)
        }
        
        Button(
            onClick = {
                notifGranted = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
                overlayGranted = Settings.canDrawOverlays(context)
            },
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Text("Refresh Status", style = MaterialTheme.typography.labelLarge)
        }
    }
}
