package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ActiveVoiceState
import com.example.data.repository.SamirRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainNavTab {
    HOME,
    MESSAGES,
    FRIENDS,
    NOTIFICATIONS,
    PROFILE
}

sealed class ChatDestination {
    data class ServerChannel(val server: Server, val channel: Channel) : ChatDestination()
    data class DirectConversation(val conversation: Conversation) : ChatDestination()
}

class SamirViewModel(application: Application) : AndroidViewModel(application) {
    val repository = SamirRepository(application)

    // Current top-level tab
    private val _selectedTab = MutableStateFlow(MainNavTab.HOME)
    val selectedTab: StateFlow<MainNavTab> = _selectedTab.asStateFlow()

    // Navigation sub-views
    private val _currentChatDestination = MutableStateFlow<ChatDestination?>(null)
    val currentChatDestination: StateFlow<ChatDestination?> = _currentChatDestination.asStateFlow()

    private val _currentServer = MutableStateFlow<Server?>(null)
    val currentServer: StateFlow<Server?> = _currentServer.asStateFlow()

    private val _showVoiceRoomScreen = MutableStateFlow(false)
    val showVoiceRoomScreen: StateFlow<Boolean> = _showVoiceRoomScreen.asStateFlow()

    // Dialog & Sheet States
    val showCreateServerDialog = MutableStateFlow(false)
    val showJoinServerDialog = MutableStateFlow(false)
    val showInviteDialog = MutableStateFlow(false)
    val showSearchDialog = MutableStateFlow(false)
    val showEditProfileDialog = MutableStateFlow(false)
    val showAddFriendDialog = MutableStateFlow(false)
    val showNewMessageDialog = MutableStateFlow(false)
    val showModerationDialog = MutableStateFlow(false)
    val showReportDialog = MutableStateFlow(false)
    val showSettingsDialog = MutableStateFlow(false)
    val showPinnedMessagesSheet = MutableStateFlow(false)
    val showServerMembersSheet = MutableStateFlow(false)

    // Report target context
    val reportTarget = MutableStateFlow("Message in #general")

    // Reply state for chat input
    val replyingToMessage = MutableStateFlow<Message?>(null)

    // Data streams
    val currentUser: StateFlow<User?> = repository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allOtherUsers: StateFlow<List<User>> = repository.getAllOtherUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val servers: StateFlow<List<Server>> = repository.getAllServers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<Conversation>> = repository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friendships: StateFlow<List<Friendship>> = repository.getAllFriendships()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationItem>> = repository.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = repository.getUnreadNotificationsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val voiceState: StateFlow<ActiveVoiceState> = repository.voiceState

    val allReactions: StateFlow<List<MessageReaction>> = repository.getAllReactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun selectTab(tab: MainNavTab) {
        _selectedTab.value = tab
        _currentChatDestination.value = null
        _currentServer.value = null
    }

    fun openServer(server: Server) {
        _currentServer.value = server
        _currentChatDestination.value = null
    }

    fun closeServer() {
        _currentServer.value = null
    }

    fun openChannel(server: Server, channel: Channel) {
        if (channel.type == "VOICE") {
            repository.connectToVoice(channel, server)
            _showVoiceRoomScreen.value = true
        } else {
            _currentChatDestination.value = ChatDestination.ServerChannel(server, channel)
        }
    }

    fun openConversation(conversation: Conversation) {
        _currentChatDestination.value = ChatDestination.DirectConversation(conversation)
    }

    fun closeChat() {
        _currentChatDestination.value = null
        replyingToMessage.value = null
    }

    fun openVoiceRoomScreen() {
        _showVoiceRoomScreen.value = true
    }

    fun closeVoiceRoomScreen() {
        _showVoiceRoomScreen.value = false
    }

    // Chat Actions
    fun sendMessage(
        content: String,
        attachmentType: String? = null,
        attachmentName: String? = null,
        attachmentSize: String? = null
    ) {
        val dest = _currentChatDestination.value ?: return
        val reply = replyingToMessage.value
        viewModelScope.launch {
            when (dest) {
                is ChatDestination.ServerChannel -> {
                    repository.sendMessage(
                        channelId = dest.channel.id,
                        content = content,
                        replyToId = reply?.id,
                        replyToAuthor = reply?.authorName,
                        replyToSnippet = reply?.content?.take(50),
                        attachmentType = attachmentType,
                        attachmentName = attachmentName,
                        attachmentSize = attachmentSize
                    )
                }
                is ChatDestination.DirectConversation -> {
                    repository.sendMessage(
                        conversationId = dest.conversation.id,
                        content = content,
                        replyToId = reply?.id,
                        replyToAuthor = reply?.authorName,
                        replyToSnippet = reply?.content?.take(50),
                        attachmentType = attachmentType,
                        attachmentName = attachmentName,
                        attachmentSize = attachmentSize
                    )
                }
            }
            replyingToMessage.value = null
        }
    }

    fun deleteMessage(msgId: String) {
        viewModelScope.launch { repository.deleteMessage(msgId) }
    }

    fun togglePinMessage(message: Message) {
        viewModelScope.launch { repository.togglePin(message) }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch { repository.toggleReaction(messageId, emoji) }
    }

    fun setReplyingTo(message: Message?) {
        replyingToMessage.value = message
    }

    // Community / Server Actions
    fun createServer(name: String, description: String, emoji: String, color: Long, privacy: String) {
        viewModelScope.launch {
            val serverId = repository.createServer(name, description, emoji, color, privacy)
            showCreateServerDialog.value = false
        }
    }

    fun joinServerByInvite(code: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val server = repository.joinServerByInvite(code)
            if (server != null) {
                showJoinServerDialog.value = false
                openServer(server)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun createChannel(serverId: String, name: String, type: String, category: String, topic: String) {
        viewModelScope.launch {
            repository.createChannel(serverId, name, type, category, topic)
        }
    }

    fun deleteChannel(channelId: String, serverId: String, channelName: String) {
        viewModelScope.launch {
            repository.deleteChannel(channelId, serverId, channelName)
        }
    }

    // Friend Actions
    fun sendFriendRequest(username: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.sendFriendRequest(username)
            onResult(success)
        }
    }

    fun acceptFriend(friendshipId: String) {
        viewModelScope.launch { repository.acceptFriendRequest(friendshipId) }
    }

    fun declineFriend(friendshipId: String) {
        viewModelScope.launch { repository.declineFriendRequest(friendshipId) }
    }

    fun blockUser(friendshipId: String) {
        viewModelScope.launch { repository.blockUser(friendshipId) }
    }

    fun unblockUser(friendshipId: String) {
        viewModelScope.launch { repository.unblockUser(friendshipId) }
    }

    fun startDirectMessage(user: User) {
        viewModelScope.launch {
            val convId = repository.startDirectMessageWith(user)
            val conv = repository.getConversation(convId).first()
            if (conv != null) {
                _selectedTab.value = MainNavTab.MESSAGES
                openConversation(conv)
            }
        }
    }

    fun createGroupChat(name: String, memberNames: List<String>) {
        viewModelScope.launch {
            val convId = repository.createGroupChat(name, memberNames)
            val conv = repository.getConversation(convId).first()
            if (conv != null) {
                showNewMessageDialog.value = false
                _selectedTab.value = MainNavTab.MESSAGES
                openConversation(conv)
            }
        }
    }

    // Profile Actions
    fun updateProfile(displayName: String, bio: String, customStatus: String, status: String) {
        viewModelScope.launch {
            repository.updateProfile(displayName, bio, customStatus, status)
            showEditProfileDialog.value = false
        }
    }

    fun updatePresence(status: String) {
        viewModelScope.launch { repository.updatePresenceStatus(status) }
    }

    // Voice Actions
    fun toggleVoiceMute() = repository.toggleVoiceMute()
    fun toggleVoiceDeafen() = repository.toggleVoiceDeafen()
    fun toggleVoiceSpeaker() = repository.toggleVoiceSpeaker()
    fun leaveVoice() {
        repository.leaveVoice()
        _showVoiceRoomScreen.value = false
    }

    // Moderation
    fun submitReport(serverId: String, target: String, reason: String) {
        viewModelScope.launch {
            val me = currentUser.value?.displayName ?: "Alex"
            repository.submitReport(serverId, me, target, reason)
            showReportDialog.value = false
        }
    }

    fun kickOrBanUser(serverId: String, action: String, username: String) {
        viewModelScope.launch {
            repository.performModerationAction(serverId, action, "$action applied to @$username")
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }
}
