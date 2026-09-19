package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.data.model.Server
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDetailScreen(
    server: Server,
    channels: List<Channel>,
    onBackClick: () -> Unit,
    onChannelClick: (Channel) -> Unit,
    onInviteClick: () -> Unit,
    onModerationClick: () -> Unit,
    onCreateChannel: (name: String, type: String, category: String, topic: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateChannelDialog by remember { mutableStateOf(false) }

    val informationChannels = remember(channels) { channels.filter { it.category == "INFORMATION" } }
    val textChannels = remember(channels) { channels.filter { it.category == "TEXT" || it.type == "TEXT" } }
    val voiceChannels = remember(channels) { channels.filter { it.type == "VOICE" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(server.iconColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = server.iconEmoji, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = server.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${server.memberCount} members • ${server.privacy.lowercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("server_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onInviteClick, modifier = Modifier.testTag("server_invite_btn")) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Invite Friends", tint = SamirPrimary)
                    }
                    IconButton(onClick = onModerationClick, modifier = Modifier.testTag("server_mod_btn")) {
                        Icon(Icons.Default.Shield, contentDescription = "Moderation", tint = SamirFlame)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateChannelDialog = true },
                containerColor = SamirPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_channel_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Channel")
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // Server Description Banner
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SamirSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ABOUT COMMUNITY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SamirPrimary
                        )
                        Text(
                            text = server.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Invite Code: ${server.inviteCode}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SamirSecondaryLight
                            )
                            Button(
                                onClick = onInviteClick,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Share Invite", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // INFORMATION CATEGORY
            if (informationChannels.isNotEmpty()) {
                item {
                    Text(
                        text = "INFORMATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = SamirTextSecondary
                    )
                }
                items(informationChannels) { channel ->
                    ChannelListItem(channel = channel, onClick = { onChannelClick(channel) })
                }
            }

            // TEXT CHANNELS CATEGORY
            if (textChannels.isNotEmpty()) {
                item {
                    Text(
                        text = "TEXT CHANNELS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = SamirTextSecondary
                    )
                }
                items(textChannels) { channel ->
                    ChannelListItem(channel = channel, onClick = { onChannelClick(channel) })
                }
            }

            // VOICE CHANNELS CATEGORY
            if (voiceChannels.isNotEmpty()) {
                item {
                    Text(
                        text = "VOICE CHANNELS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = SamirStatusOnline
                    )
                }
                items(voiceChannels) { channel ->
                    ChannelListItem(channel = channel, onClick = { onChannelClick(channel) })
                }
            }
        }
    }

    if (showCreateChannelDialog) {
        CreateChannelDialog(
            onDismiss = { showCreateChannelDialog = false },
            onCreate = { name, type, category, topic ->
                onCreateChannel(name, type, category, topic)
                showCreateChannelDialog = false
            }
        )
    }
}

@Composable
fun ChannelListItem(
    channel: Channel,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SamirSurfaceBorder.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("channel_item_${channel.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (channel.type) {
                "VOICE" -> Icons.Default.VolumeUp
                "ANNOUNCEMENT" -> Icons.Default.Campaign
                else -> Icons.Default.Tag
            }
            val tint = when (channel.type) {
                "VOICE" -> SamirStatusOnline
                "ANNOUNCEMENT" -> SamirFlame
                else -> SamirPrimary
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (channel.topic.isNotBlank()) {
                    Text(
                        text = channel.topic,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (channel.type == "VOICE") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SamirStatusOnline.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Join Voice",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SamirStatusOnline
                    )
                }
            }
        }
    }
}

@Composable
fun CreateChannelDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, category: String, topic: String) -> Unit
) {
    var channelName by remember { mutableStateOf("") }
    var channelTopic by remember { mutableStateOf("") }
    var channelType by remember { mutableStateOf("TEXT") } // "TEXT" or "VOICE"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Channel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = channelName,
                    onValueChange = { channelName = it },
                    label = { Text("Channel Name") },
                    placeholder = { Text("e.g. gaming-clips") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_channel_name_input")
                )

                OutlinedTextField(
                    value = channelTopic,
                    onValueChange = { channelTopic = it },
                    label = { Text("Topic / Description") },
                    placeholder = { Text("What is discussed here?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_channel_topic_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = channelType == "TEXT",
                        onClick = { channelType = "TEXT" },
                        label = { Text("# Text Channel") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = channelType == "VOICE",
                        onClick = { channelType = "VOICE" },
                        label = { Text("🔊 Voice Room") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val category = if (channelType == "VOICE") "VOICE" else "TEXT"
                    onCreate(channelName.trim(), channelType, category, channelTopic.trim())
                },
                enabled = channelName.isNotBlank(),
                modifier = Modifier.testTag("submit_create_channel_btn")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
