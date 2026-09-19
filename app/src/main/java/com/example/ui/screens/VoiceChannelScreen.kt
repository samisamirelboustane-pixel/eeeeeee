package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ActiveVoiceState
import com.example.data.repository.VoiceParticipantState
import com.example.ui.components.AvatarWithStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChannelScreen(
    voiceState: ActiveVoiceState,
    onBackClick: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleDeafen: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onLeaveVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "🔊 ${voiceState.channelName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${voiceState.serverName} • RTC Connected (24ms)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SamirStatusOnline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("voice_screen_back")) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize voice", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SamirDarkBg)
            )
        },
        bottomBar = {
            Surface(
                color = SamirSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                tonalElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute Button
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (voiceState.isMuted) SamirError.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (voiceState.isMuted) SamirError else Color.Transparent, CircleShape)
                            .testTag("voice_mute_btn")
                    ) {
                        Icon(
                            imageVector = if (voiceState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (voiceState.isMuted) "Unmute" else "Mute",
                            tint = if (voiceState.isMuted) SamirError else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Deafen / Undeafen Button
                    IconButton(
                        onClick = onToggleDeafen,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (voiceState.isDeafened) SamirError.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (voiceState.isDeafened) SamirError else Color.Transparent, CircleShape)
                            .testTag("voice_deafen_btn")
                    ) {
                        Icon(
                            imageVector = if (voiceState.isDeafened) Icons.Default.HeadsetOff else Icons.Default.Headset,
                            contentDescription = if (voiceState.isDeafened) "Undeafen" else "Deafen",
                            tint = if (voiceState.isDeafened) SamirError else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Speaker Output Toggle Button
                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (voiceState.isSpeakerOn) SamirPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("voice_speaker_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaker",
                            tint = if (voiceState.isSpeakerOn) SamirPrimaryLight else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Disconnect / Leave Button
                    IconButton(
                        onClick = onLeaveVoice,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SamirError)
                            .testTag("voice_leave_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Disconnect from voice",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
        containerColor = SamirDarkBg,
        modifier = modifier.testTag("voice_channel_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Voice status banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SamirStatusOnline.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SamirStatusOnline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SamirStatusOnline)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Voice connected • Opus 64kbps Real-Time Audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = SamirStatusOnline,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Participants Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(voiceState.participants) { participant ->
                    VoiceParticipantCard(participant)
                }
            }
        }
    }
}

@Composable
fun VoiceParticipantCard(participant: VoiceParticipantState) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SamirSurface),
        border = if (participant.isSpeaking) {
            androidx.compose.foundation.BorderStroke(2.dp, SamirStatusOnline)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, SamirSurfaceBorder)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AvatarWithStatus(
                    displayName = participant.name,
                    avatarBgColor = participant.avatarBgColor,
                    isSpeaking = participant.isSpeaking,
                    size = 64.dp,
                    showStatus = false
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = participant.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (participant.isSpeaking) {
                    Text(
                        text = "Speaking...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SamirStatusOnline
                    )
                }
            }

            // Mute Icon Indicator in bottom right
            if (participant.isMuted) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(SamirError.copy(alpha = 0.3f))
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "Muted",
                        tint = SamirError,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
