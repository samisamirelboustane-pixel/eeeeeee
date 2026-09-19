package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.data.model.MessageReaction
import com.example.ui.components.AvatarWithStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatDestination
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    destination: ChatDestination,
    messages: List<Message>,
    reactions: List<MessageReaction>,
    replyingTo: Message?,
    currentUserId: String,
    onBackClick: () -> Unit,
    onSendMessage: (content: String, attachmentType: String?, attachmentName: String?, attachmentSize: String?) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onTogglePin: (Message) -> Unit,
    onToggleReaction: (messageId: String, emoji: String) -> Unit,
    onSetReplyingTo: (Message?) -> Unit,
    onReportMessage: (Message) -> Unit,
    onPinnedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var selectedMessageForAction by remember { mutableStateOf<Message?>(null) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val title = when (destination) {
        is ChatDestination.ServerChannel -> "# ${destination.channel.name}"
        is ChatDestination.DirectConversation -> destination.conversation.name
    }
    val subtitle = when (destination) {
        is ChatDestination.ServerChannel -> destination.server.name
        is ChatDestination.DirectConversation -> "Real Friend Direct Conversation"
    }

    // Auto scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("chat_back_btn")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onPinnedClick, modifier = Modifier.testTag("chat_pinned_btn")) {
                        Icon(Icons.Default.PushPin, contentDescription = "Pinned Messages", tint = SamirFlame)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Active Reply Banner
                AnimatedVisibility(visible = replyingTo != null) {
                    if (replyingTo != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Reply, contentDescription = null, tint = SamirPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Replying to ${replyingTo.authorName}: ",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SamirPrimary
                                    )
                                    Text(
                                        text = replyingTo.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = { onSetReplyingTo(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Quick emoji toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("🔥", "❤️", "😂", "👍", "🎮", "🚀", "⚡", "✨").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { inputText += emoji }
                                .padding(4.dp)
                        )
                    }
                }

                // Text Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAttachmentSheet = true },
                        modifier = Modifier.size(42.dp).testTag("chat_attachment_btn")
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add attachment", tint = SamirPrimary)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message $title...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = SamirPrimary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim(), null, null, null)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) SamirPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Message",
                            tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val msgReactions = reactions.filter { it.messageId == msg.id }
                MessageBubbleItem(
                    message = msg,
                    reactions = msgReactions,
                    currentUserId = currentUserId,
                    onMessageClick = { selectedMessageForAction = msg },
                    onReactionClick = { emoji -> onToggleReaction(msg.id, emoji) }
                )
            }
        }
    }

    // Message Action Bottom Sheet / Dialog
    if (selectedMessageForAction != null) {
        val targetMsg = selectedMessageForAction!!
        AlertDialog(
            onDismissRequest = { selectedMessageForAction = null },
            title = {
                Text(
                    text = "Message by ${targetMsg.authorName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick React Emojis
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("🔥", "❤️", "😂", "👍", "🎉", "💩").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        onToggleReaction(targetMsg.id, emoji)
                                        selectedMessageForAction = null
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Reply
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetReplyingTo(targetMsg)
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = null, tint = SamirPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Reply to message")
                    }

                    // Pin
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTogglePin(targetMsg)
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = SamirFlame)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (targetMsg.isPinned) "Unpin message" else "Pin message to channel")
                    }

                    // Copy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                clipboardManager.setText(AnnotatedString(targetMsg.content))
                                Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Copy text")
                    }

                    // Report
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onReportMessage(targetMsg)
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = SamirWarning)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Report message")
                    }

                    // Delete (if authored by current user)
                    if (targetMsg.authorId == currentUserId || targetMsg.authorId == "user_me") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDeleteMessage(targetMsg.id)
                                    selectedMessageForAction = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = SamirError)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete message", color = SamirError)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMessageForAction = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Attachment Chooser Sheet
    if (showAttachmentSheet) {
        AlertDialog(
            onDismissRequest = { showAttachmentSheet = false },
            title = { Text("Send Attachment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSendMessage("Shared an image screenshot 📸", "IMAGE", "screenshot_match.png", "2.1 MB")
                                showAttachmentSheet = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = SamirPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Photo / Image", fontWeight = FontWeight.Bold)
                            Text("Send full-res picture", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSendMessage("Shared a gameplay recording 🎬", "VIDEO", "squad_highlight_clip.mp4", "18.4 MB")
                                showAttachmentSheet = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = SamirFlame)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Video Recording", fontWeight = FontWeight.Bold)
                            Text("Send video clip up to 100MB", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSendMessage("Shared project document 📄", "FILE", "samir_architecture_v1.pdf", "420 KB")
                                showAttachmentSheet = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = SamirSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Document / File", fontWeight = FontWeight.Bold)
                            Text("PDF, ZIP, APK or code", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentSheet = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun MessageBubbleItem(
    message: Message,
    reactions: List<MessageReaction>,
    currentUserId: String,
    onMessageClick: () -> Unit,
    onReactionClick: (String) -> Unit
) {
    val isMe = message.authorId == currentUserId || message.authorId == "user_me"
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.createdAt) { timeFormat.format(Date(message.createdAt)) }

    // Group reactions by emoji
    val reactionGroups = remember(reactions) {
        reactions.groupBy { it.emoji }.map { (emoji, list) ->
            Triple(emoji, list.size, list.any { it.userId == currentUserId || it.userId == "user_me" })
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onMessageClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        AvatarWithStatus(
            displayName = message.authorName,
            avatarBgColor = message.authorColor,
            size = 38.dp,
            showStatus = false
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Header: Name, Time, Pin badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(message.authorColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                if (message.isPinned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SamirFlame.copy(alpha = 0.2f),
                        contentColor = SamirFlame
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = "Pinned", modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Pinned", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Reply Quote Snippet if any
            if (message.replyToAuthor != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(top = 3.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "↳ ${message.replyToAuthor}: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SamirPrimary
                        )
                        Text(
                            text = message.replyToSnippet ?: "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Message text
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Attachment Card if present
            if (message.attachmentType != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SamirSurfaceBorder),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (message.attachmentType) {
                            "IMAGE" -> Icons.Default.Image
                            "VIDEO" -> Icons.Default.Videocam
                            else -> Icons.Default.InsertDriveFile
                        }
                        Icon(imageVector = icon, contentDescription = null, tint = SamirPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = message.attachmentName ?: "Attachment",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${message.attachmentType} • ${message.attachmentSize ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Reactions row
            if (reactionGroups.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reactionGroups.forEach { (emoji, count, userReacted) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (userReacted) SamirPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (userReacted) androidx.compose.foundation.BorderStroke(1.dp, SamirPrimary) else null,
                            modifier = Modifier.clickable { onReactionClick(emoji) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$count",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userReacted) SamirPrimaryLight else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
