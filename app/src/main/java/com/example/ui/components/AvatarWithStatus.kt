package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AvatarWithStatus(
    displayName: String,
    avatarBgColor: Long,
    status: String = "ONLINE",
    isSpeaking: Boolean = false,
    size: Dp = 44.dp,
    showStatus: Boolean = true,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status.uppercase()) {
        "ONLINE" -> SamirStatusOnline
        "IDLE" -> SamirStatusIdle
        "DND" -> SamirStatusDnd
        else -> SamirStatusOffline
    }

    val infiniteTransition = rememberInfiniteTransition(label = "speaking_pulse")
    val pulseBorderWidth by infiniteTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_width"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSpeaking) {
                        Modifier.border(
                            width = pulseBorderWidth.dp,
                            brush = Brush.radialGradient(
                                listOf(SamirStatusOnline, Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(if (isSpeaking) 3.dp else 0.dp)
                .clip(CircleShape)
                .background(Color(avatarBgColor)),
            contentAlignment = Alignment.Center
        ) {
            val initial = displayName.trim().take(1).uppercase().ifEmpty { "U" }
            Text(
                text = initial,
                color = Color.White,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Presence Status indicator dot
        if (showStatus) {
            val dotSize = (size.value * 0.28f).coerceIn(10f, 16f).dp
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
        }
    }
}
