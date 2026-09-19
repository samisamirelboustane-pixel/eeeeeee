package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.User
import com.example.ui.components.SamirBottomNav
import com.example.ui.components.SamirTopBar
import com.example.ui.components.VoiceActiveFloatingBar
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatDestination
import com.example.ui.viewmodel.MainNavTab
import com.example.ui.viewmodel.SamirViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SamirApp()
            }
        }
    }
}

@Composable
fun SamirApp(vm: SamirViewModel = viewModel()) {
    val selectedTab by vm.selectedTab.collectAsState()
    val currentChatDestination by vm.currentChatDestination.collectAsState()
    val currentServer by vm.currentServer.collectAsState()
    val showVoiceRoomScreen by vm.showVoiceRoomScreen.collectAsState()

    val currentUser by vm.currentUser.collectAsState()
    val allOtherUsers by vm.allOtherUsers.collectAsState()
    val servers by vm.servers.collectAsState()
    val conversations by vm.conversations.collectAsState()
    val friendships by vm.friendships.collectAsState()
    val notifications by vm.notifications.collectAsState()
    val unreadNotifCount by vm.unreadNotifCount.collectAsState()
    val voiceState by vm.voiceState.collectAsState()
    val reactions by vm.allReactions.collectAsState()
    val replyingTo by vm.replyingToMessage.collectAsState()

    // Dialog state collectors
    val showCreateServer by vm.showCreateServerDialog.collectAsState()
    val showJoinServer by vm.showJoinServerDialog.collectAsState()
    val showInvite by vm.showInviteDialog.collectAsState()
    val showSearch by vm.showSearchDialog.collectAsState()
    val showEditProfile by vm.showEditProfileDialog.collectAsState()
    val showAddFriend by vm.showAddFriendDialog.collectAsState()
    val showNewMessage by vm.showNewMessageDialog.collectAsState()
    val showModeration by vm.showModerationDialog.collectAsState()
    val showReport by vm.showReportDialog.collectAsState()
    val showSettings by vm.showSettingsDialog.collectAsState()
    val showPinned by vm.showPinnedMessagesSheet.collectAsState()
    val reportTargetDesc by vm.reportTarget.collectAsState()

    // Back button handling for sub-navigation
    BackHandler(enabled = currentChatDestination != null || currentServer != null || showVoiceRoomScreen) {
        when {
            showVoiceRoomScreen -> vm.closeVoiceRoomScreen()
            currentChatDestination != null -> vm.closeChat()
            currentServer != null -> vm.closeServer()
        }
    }

    val totalUnreadMessages = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    val onlineFriends = remember(allOtherUsers) {
        allOtherUsers.filter { it.status.uppercase() == "ONLINE" || it.status.uppercase() == "IDLE" }
    }

    // Full screen voice channel room if expanded
    if (showVoiceRoomScreen && voiceState.isConnected) {
        VoiceChannelScreen(
            voiceState = voiceState,
            onBackClick = { vm.closeVoiceRoomScreen() },
            onToggleMute = { vm.toggleVoiceMute() },
            onToggleDeafen = { vm.toggleVoiceDeafen() },
            onToggleSpeaker = { vm.toggleVoiceSpeaker() },
            onLeaveVoice = { vm.leaveVoice() }
        )
    } else if (currentChatDestination != null) {
        // Active Chat Screen (Server Channel or Direct Conversation)
        val dest = currentChatDestination!!
        val channelId = (dest as? ChatDestination.ServerChannel)?.channel?.id
        val convId = (dest as? ChatDestination.DirectConversation)?.conversation?.id

        val messagesFlow = remember(dest) {
            if (channelId != null) vm.repository.getMessagesForChannel(channelId)
            else vm.repository.getMessagesForConversation(convId ?: "")
        }
        val messages by messagesFlow.collectAsState(initial = emptyList())

        val pinnedFlow = remember(channelId) {
            if (channelId != null) vm.repository.getPinnedMessages(channelId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val pinnedMessages by pinnedFlow.collectAsState(initial = emptyList())

        ChatScreen(
            destination = dest,
            messages = messages,
            reactions = reactions,
            replyingTo = replyingTo,
            currentUserId = currentUser?.id ?: "user_me",
            onBackClick = { vm.closeChat() },
            onSendMessage = { content, type, name, size ->
                vm.sendMessage(content, type, name, size)
            },
            onDeleteMessage = { msgId -> vm.deleteMessage(msgId) },
            onTogglePin = { msg -> vm.togglePinMessage(msg) },
            onToggleReaction = { msgId, emoji -> vm.toggleReaction(msgId, emoji) },
            onSetReplyingTo = { msg -> vm.setReplyingTo(msg) },
            onReportMessage = { msg ->
                vm.reportTarget.value = "Message by ${msg.authorName}: \"${msg.content.take(30)}\""
                vm.showReportDialog.value = true
            },
            onPinnedClick = { vm.showPinnedMessagesSheet.value = true }
        )

        if (showPinned) {
            val chName = (dest as? ChatDestination.ServerChannel)?.channel?.name ?: "chat"
            PinnedMessagesDialog(
                channelName = chName,
                pinnedMessages = pinnedMessages,
                onDismiss = { vm.showPinnedMessagesSheet.value = false },
                onUnpin = { msg -> vm.togglePinMessage(msg) }
            )
        }
    } else if (currentServer != null) {
        // Server Community Channel Browser Screen
        val server = currentServer!!
        val serverChannelsFlow = remember(server.id) { vm.repository.getChannels(server.id) }
        val serverChannels by serverChannelsFlow.collectAsState(initial = emptyList())

        ServerDetailScreen(
            server = server,
            channels = serverChannels,
            onBackClick = { vm.closeServer() },
            onChannelClick = { channel -> vm.openChannel(server, channel) },
            onInviteClick = { vm.showInviteDialog.value = true },
            onModerationClick = { vm.showModerationDialog.value = true },
            onCreateChannel = { name, type, category, topic ->
                vm.createChannel(server.id, name, type, category, topic)
            }
        )
    } else {
        // Main Tab Layout (Home, Messages, Friends, Notifications, Profile)
        Scaffold(
            topBar = {
                val title = when (selectedTab) {
                    MainNavTab.HOME -> "SAMIR"
                    MainNavTab.MESSAGES -> "Messages"
                    MainNavTab.FRIENDS -> "Real Friends"
                    MainNavTab.NOTIFICATIONS -> "Notifications"
                    MainNavTab.PROFILE -> "My Profile"
                }
                val subtitle = when (selectedTab) {
                    MainNavTab.HOME -> "Real friends & conversations"
                    MainNavTab.MESSAGES -> "${conversations.size} conversations"
                    MainNavTab.FRIENDS -> "${allOtherUsers.size} real connections"
                    MainNavTab.NOTIFICATIONS -> "$unreadNotifCount new"
                    MainNavTab.PROFILE -> "@${currentUser?.username ?: "alex_rivers"}"
                }
                SamirTopBar(
                    title = title,
                    subtitle = subtitle,
                    onSearchClick = { vm.showSearchDialog.value = true },
                    onSettingsClick = { vm.showSettingsDialog.value = true }
                )
            },
            bottomBar = {
                Column {
                    // Floating voice indicator if connected
                    VoiceActiveFloatingBar(
                        voiceState = voiceState,
                        onBarClick = { vm.openVoiceRoomScreen() },
                        onMuteToggle = { vm.toggleVoiceMute() },
                        onDeafenToggle = { vm.toggleVoiceDeafen() },
                        onLeaveVoice = { vm.leaveVoice() }
                    )

                    SamirBottomNav(
                        selectedTab = selectedTab,
                        unreadMessagesCount = totalUnreadMessages,
                        unreadNotifCount = unreadNotifCount,
                        onTabSelected = { vm.selectTab(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    MainNavTab.HOME -> {
                        HomeScreen(
                            currentUser = currentUser,
                            onlineFriends = onlineFriends,
                            servers = servers,
                            recentConversations = conversations,
                            onCreateServerClick = { vm.showCreateServerDialog.value = true },
                            onJoinServerClick = { vm.showJoinServerDialog.value = true },
                            onSearchClick = { vm.showSearchDialog.value = true },
                            onServerClick = { srv -> vm.openServer(srv) },
                            onConversationClick = { conv -> vm.openConversation(conv) },
                            onFriendClick = { friend -> vm.startDirectMessage(friend) }
                        )
                    }
                    MainNavTab.MESSAGES -> {
                        MessagesScreen(
                            conversations = conversations,
                            onConversationClick = { conv -> vm.openConversation(conv) },
                            onNewConversationClick = { vm.showNewMessageDialog.value = true }
                        )
                    }
                    MainNavTab.FRIENDS -> {
                        FriendsScreen(
                            friends = allOtherUsers,
                            friendships = friendships,
                            allOtherUsers = allOtherUsers,
                            onAddFriendClick = { vm.showAddFriendDialog.value = true },
                            onMessageFriend = { friend -> vm.startDirectMessage(friend) },
                            onAcceptRequest = { fId -> vm.acceptFriend(fId) },
                            onDeclineRequest = { fId -> vm.declineFriend(fId) },
                            onBlockUser = { fId -> vm.blockUser(fId) },
                            onUnblockUser = { fId -> vm.unblockUser(fId) }
                        )
                    }
                    MainNavTab.NOTIFICATIONS -> {
                        NotificationsScreen(
                            notifications = notifications,
                            onMarkAllRead = { vm.markNotificationsRead() },
                            onNotificationClick = { notif ->
                                // If target is a conversation
                                val conv = conversations.find { it.id == notif.targetId }
                                if (conv != null) {
                                    vm.openConversation(conv)
                                }
                            }
                        )
                    }
                    MainNavTab.PROFILE -> {
                        ProfileScreen(
                            currentUser = currentUser,
                            friendsCount = allOtherUsers.size,
                            serversCount = servers.size,
                            onEditProfileClick = { vm.showEditProfileDialog.value = true },
                            onSettingsClick = { vm.showSettingsDialog.value = true },
                            onStatusChange = { newStatus -> vm.updatePresence(newStatus) }
                        )
                    }
                }
            }
        }
    }

    // --- Global Modals & Dialogs ---

    if (showCreateServer) {
        CreateServerDialog(
            onDismiss = { vm.showCreateServerDialog.value = false },
            onCreate = { name, desc, emoji, color, privacy ->
                vm.createServer(name, desc, emoji, color, privacy)
            }
        )
    }

    if (showJoinServer) {
        JoinServerDialog(
            onDismiss = { vm.showJoinServerDialog.value = false },
            onJoin = { code, callback ->
                vm.joinServerByInvite(code, callback)
            }
        )
    }

    if (showInvite) {
        val targetServer = currentServer ?: servers.firstOrNull()
        if (targetServer != null) {
            ServerInviteDialog(
                server = targetServer,
                onDismiss = { vm.showInviteDialog.value = false }
            )
        }
    }

    if (showSearch) {
        SearchDialog(
            allUsers = allOtherUsers,
            allServers = servers,
            onDismiss = { vm.showSearchDialog.value = false },
            onUserSelect = { user ->
                vm.showSearchDialog.value = false
                vm.startDirectMessage(user)
            },
            onServerSelect = { server ->
                vm.showSearchDialog.value = false
                vm.openServer(server)
            }
        )
    }

    if (showEditProfile && currentUser != null) {
        EditProfileDialog(
            user = currentUser!!,
            onDismiss = { vm.showEditProfileDialog.value = false },
            onSave = { displayName, bio, customStatus, status ->
                vm.updateProfile(displayName, bio, customStatus, status)
            }
        )
    }

    if (showAddFriend) {
        AddFriendDialog(
            onDismiss = { vm.showAddFriendDialog.value = false },
            onSendRequest = { username, callback ->
                vm.sendFriendRequest(username, callback)
            }
        )
    }

    if (showNewMessage) {
        NewConversationDialog(
            friends = allOtherUsers,
            onDismiss = { vm.showNewMessageDialog.value = false },
            onStartDirectMessage = { user ->
                vm.showNewMessageDialog.value = false
                vm.startDirectMessage(user)
            },
            onCreateGroupChat = { name, members ->
                vm.createGroupChat(name, members)
            }
        )
    }

    if (showModeration) {
        val srv = currentServer ?: servers.firstOrNull()
        if (srv != null) {
            val logsFlow = remember(srv.id) { vm.repository.getAuditLogs(srv.id) }
            val reportsFlow = remember(srv.id) { vm.repository.getReports(srv.id) }
            val logs by logsFlow.collectAsState(initial = emptyList())
            val reports by reportsFlow.collectAsState(initial = emptyList())

            ModerationCenterDialog(
                server = srv,
                auditLogs = logs,
                reports = reports,
                onDismiss = { vm.showModerationDialog.value = false },
                onAction = { action, details ->
                    vm.kickOrBanUser(srv.id, action, details)
                }
            )
        }
    }

    if (showReport) {
        val srvId = currentServer?.id ?: "srv_general"
        ReportDialog(
            targetDescription = reportTargetDesc,
            onDismiss = { vm.showReportDialog.value = false },
            onSubmit = { reason ->
                vm.submitReport(srvId, reportTargetDesc, reason)
            }
        )
    }

    if (showSettings) {
        SettingsDialog(
            user = currentUser,
            onDismiss = { vm.showSettingsDialog.value = false }
        )
    }
}
