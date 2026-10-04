package com.scamshield.app.overlay

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scamshield.app.ScamShieldState
import com.scamshield.app.ScamShieldState.BubbleStatus
import com.scamshield.app.ui.theme.VerdictAmber
import com.scamshield.app.ui.theme.VerdictGreen
import com.scamshield.app.ui.theme.VerdictRed

@Composable
fun BubbleView() {
    val bubbleState by ScamShieldState.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    
    // Color based on current status
    val targetColor = when (bubbleState.status) {
        BubbleStatus.IDLE -> VerdictGreen
        BubbleStatus.SUSPICIOUS -> VerdictAmber
        BubbleStatus.SCAM -> VerdictRed
    }
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(500),
        label = "bubbleColor"
    )
    
    // Size based on status — scam is bigger and more urgent
    val targetSize = when (bubbleState.status) {
        BubbleStatus.IDLE -> 48f
        BubbleStatus.SUSPICIOUS -> 56f
        BubbleStatus.SCAM -> 64f
    }
    val animatedSize by animateFloatAsState(
        targetValue = targetSize,
        animationSpec = tween(300),
        label = "bubbleSize"
    )
    
    // Pulse animation for non-idle states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (bubbleState.status == BubbleStatus.IDLE) 1f else 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (bubbleState.status == BubbleStatus.SCAM) 600 else 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The bubble itself
        Box(
            modifier = Modifier
                .size(animatedSize.dp)
                .scale(pulseScale)
                .shadow(8.dp, CircleShape)
                .background(color = animatedColor, shape = CircleShape)
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🛡️",
                fontSize = (animatedSize * 0.4f).sp,
                textAlign = TextAlign.Center
            )
        }
        
        // Expanded info card when tapped
        if (expanded && bubbleState.lastVerdict != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .clickable { expanded = false },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = bubbleState.message,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = animatedColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bubbleState.lastVerdict?.plainLanguage ?: "",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to dismiss",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
