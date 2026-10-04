package com.scamshield.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.scamshield.app.overlay.BubbleOverlayService
import com.scamshield.app.setup.SetupActivity
import com.scamshield.app.ui.theme.ScamShieldTheme
import com.scamshield.app.ui.theme.VerdictGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (!isSetupComplete()) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        // Start the floating bubble service
        val serviceIntent = Intent(this, BubbleOverlayService::class.java)
        startForegroundService(serviceIntent)

        setContent {
            ScamShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onOpenSettings = {
                            startActivity(Intent(this, SetupActivity::class.java))
                        },
                        onStopService = {
                            stopService(Intent(this, BubbleOverlayService::class.java))
                        }
                    )
                }
            }
        }
    }
    
    private fun isSetupComplete(): Boolean {
        val notificationEnabled = NotificationManagerCompat
            .getEnabledListenerPackages(this)
            .contains(packageName)
        val overlayEnabled = Settings.canDrawOverlays(this)
        return notificationEnabled && overlayEnabled
    }
}

@Composable
fun MainScreen(onOpenSettings: () -> Unit, onStopService: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Big green shield
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(VerdictGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🛡️", fontSize = 56.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "ScamShield is Active",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Your phone is being protected from scam links.\n\nThe floating shield bubble is watching over you.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // How it works section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "How ScamShield protects you:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("🟢  Green bubble = You are safe", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🟡  Yellow bubble = Be careful", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🔴  Red bubble = Scam detected!", fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Settings", fontSize = 16.sp)
        }
    }
}
