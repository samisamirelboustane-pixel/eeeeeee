package com.example.data.repository

import android.content.Context
import com.example.data.local.SamirDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class VoiceParticipantState(
    val userId: String,
    val name: String,
    val avatarBgColor: Long,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false
)

data class ActiveVoiceState(
    val isConnected: Boolean = false,
    val channelId: String = "",
    val channelName: String = "",
    val serverId: String = "",
    val serverName: String = "",
    val isMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val participants: List<VoiceParticipantState> = emptyList()
)

class SamirRepository(context: Context) {
    private val db = SamirDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val serverDao = db.serverDao()
    private val channelDao = db.channelDao()
    private val messageDao = db.messageDao()
    private val reactionDao = db.reactionDao()
    private val friendshipDao = db.friendshipDao()
    private val conversationDao = db.conversationDao()
    private val notificationDao = db.notificationDao()
    private val moderationDao = db.moderationDao()

    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory active voice state
    private val _voiceState = MutableStateFlow(ActiveVoiceState())
    val voiceState: StateFlow<ActiveVoiceState> = _voiceState.asStateFlow()

    init {
        scope.launch {
            seedInitialDataIfNeeded()
        }
    }

    // --- Users & Profiles ---
    fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser()
    fun getAllOtherUsers(): Flow<List<User>> = userDao.getAllOtherUsers()
    fun getUserFlow(id: String): Flow<User?> = userDao.getUserFlow(id)
    suspend fun getUser(id: String): User? = userDao.getUserById(id)
    fun searchUsers(query: String): Flow<List<User>> = userDao.searchUsers(query)

    suspend fun updateProfile(displayName: String, bio: String, customStatus: String, status: String) {
        val current = userDao.getCurrentUser().first() ?: return
        val updated = current.copy(
            displayName = displayName,
            bio = bio,
            customStatus = customStatus,
            status = status
        )
        userDao.updateUser(updated)
    }

    suspend fun updatePresenceStatus(status: String) {
        val current = userDao.getCurrentUser().first() ?: return
        userDao.updateUser(current.copy(status = status))
    }

    suspend fun updateCustomStatus(customStatus: String) {
        val current = userDao.getCurrentUser().first() ?: return
        userDao.updateUser(current.copy(customStatus = customStatus))
    }

    // --- Servers & Communities ---
    fun getAllServers(): Flow<List<Server>> = serverDao.getAllServers()
    fun getServer(id: String): Flow<Server?> = serverDao.getServerById(id)
    fun getChannels(serverId: String): Flow<List<Channel>> = channelDao.getChannelsForServer(serverId)
    fun getChannel(id: String): Flow<Channel?> = channelDao.getChannelById(id)

    suspend fun createServer(
        name: String,
        description: String,
        emoji: String,
        color: Long,
        privacy: String
    ): String {
        val serverId = "server_" + UUID.randomUUID().toString().take(8)
        val inviteCode = "SAMIR-" + UUID.randomUUID().toString().take(6).uppercase()
        val server = Server(
            id = serverId,
            ownerId = "user_me",
            name = name,
            description = description,
            iconEmoji = emoji,
            iconColor = color,
            privacy = privacy,
            memberCount = 1,
            inviteCode = inviteCode
        )
        serverDao.insertServer(server)

        // Default channels
        val defaultChannels = listOf(
            Channel(id = "ch_${serverId}_info", serverId = serverId, name = "welcome", type = "ANNOUNCEMENT", category = "INFORMATION", topic = "Welcome to $name!", position = 0),
            Channel(id = "ch_${serverId}_gen", serverId = serverId, name = "general", type = "TEXT", category = "TEXT", topic = "General banter & discussion", position = 1),
            Channel(id = "ch_${serverId}_voice", serverId = serverId, name = "Lobby", type = "VOICE", category = "VOICE", topic = "Voice chat room", position = 2)
        )
        channelDao.insertChannels(*defaultChannels.toTypedArray())

        // Welcome message
        val welcomeMsg = Message(
            id = "msg_" + UUID.randomUUID().toString(),
            channelId = "ch_${serverId}_gen",
            authorId = "user_me",
            authorName = "Alex Rivers",
            authorUsername = "alex_rivers",
            authorColor = 0xFF8B5CF6,
            content = "Welcome everyone to $name! Created for real friends."
        )
        messageDao.insertMessage(welcomeMsg)

        // Audit log
        moderationDao.insertAuditLog(
            AuditLogItem(
                id = UUID.randomUUID().toString(),
                serverId = serverId,
                actorName = "Alex Rivers",
                action = "CREATED_SERVER",
                details = "Created community $name with initial channels"
            )
        )

        return serverId
    }

    suspend fun joinServerByInvite(code: String): Server? {
        val server = serverDao.findServerByInviteCode(code.trim()) ?: return null
        serverDao.updateServer(server.copy(memberCount = server.memberCount + 1))
        return server
    }

    suspend fun createChannel(serverId: String, name: String, type: String, category: String, topic: String) {
        val chId = "ch_" + UUID.randomUUID().toString().take(8)
        val channel = Channel(
            id = chId,
            serverId = serverId,
            name = name.lowercase().replace(" ", "-"),
            type = type,
            category = category,
            topic = topic,
            position = 99
        )
        channelDao.insertChannel(channel)
        moderationDao.insertAuditLog(
            AuditLogItem(
                id = UUID.randomUUID().toString(),
                serverId = serverId,
                actorName = "Alex Rivers",
                action = "CREATED_CHANNEL",
                details = "Created #$name ($type)"
            )
        )
    }

    suspend fun deleteChannel(channelId: String, serverId: String, channelName: String) {
        channelDao.deleteChannel(channelId)
        moderationDao.insertAuditLog(
            AuditLogItem(
                id = UUID.randomUUID().toString(),
                serverId = serverId,
                actorName = "Alex Rivers",
                action = "DELETED_CHANNEL",
                details = "Deleted #$channelName"
            )
        )
    }

    // --- Messages ---
    fun getMessagesForChannel(channelId: String): Flow<List<Message>> = messageDao.getMessagesForChannel(channelId)
    fun getMessagesForConversation(convId: String): Flow<List<Message>> = messageDao.getMessagesForConversation(convId)
    fun getPinnedMessages(channelId: String): Flow<List<Message>> = messageDao.getPinnedMessages(channelId)
    fun searchMessages(query: String): Flow<List<Message>> = messageDao.searchMessages(query)

    suspend fun sendMessage(
        channelId: String? = null,
        conversationId: String? = null,
        content: String,
        replyToId: String? = null,
        replyToAuthor: String? = null,
        replyToSnippet: String? = null,
        attachmentType: String? = null,
        attachmentName: String? = null,
        attachmentSize: String? = null
    ) {
        val currentUser = userDao.getCurrentUser().first() ?: return
        val msg = Message(
            id = "msg_" + UUID.randomUUID().toString(),
            channelId = channelId,
            conversationId = conversationId,
            authorId = currentUser.id,
            authorName = currentUser.displayName,
            authorUsername = currentUser.username,
            authorAvatar = currentUser.avatarUrl,
            authorColor = currentUser.avatarBgColor,
            content = content,
            replyToId = replyToId,
            replyToAuthor = replyToAuthor,
            replyToSnippet = replyToSnippet,
            attachmentType = attachmentType,
            attachmentName = attachmentName,
            attachmentSize = attachmentSize,
            createdAt = System.currentTimeMillis()
        )
        messageDao.insertMessage(msg)

        // If conversation, update snippet
        if (conversationId != null) {
            val textPreview = if (attachmentType != null) "📎 $attachmentName" else content
            conversationDao.updateLastMessage(conversationId, textPreview, System.currentTimeMillis())
        }
    }

    suspend fun deleteMessage(id: String) {
        messageDao.deleteMessage(id)
    }

    suspend fun togglePin(message: Message) {
        val updated = message.copy(isPinned = !message.isPinned)
        messageDao.updateMessage(updated)
    }

    // --- Reactions ---
    fun getAllReactions(): Flow<List<MessageReaction>> = reactionDao.getAllReactions()

    suspend fun toggleReaction(messageId: String, emoji: String) {
        val user = userDao.getCurrentUser().first() ?: return
        val currentReactions = reactionDao.getReactionsForMessage(messageId).first()
        val existing = currentReactions.find { it.userId == user.id && it.emoji == emoji }
        if (existing != null) {
            reactionDao.deleteReaction(messageId, user.id, emoji)
        } else {
            reactionDao.insertReaction(
                MessageReaction(
                    id = "${messageId}_${user.id}_$emoji",
                    messageId = messageId,
                    userId = user.id,
                    userName = user.displayName,
                    emoji = emoji
                )
            )
        }
    }

    // --- Friends ---
    fun getAllFriendships(): Flow<List<Friendship>> = friendshipDao.getAllFriendships()

    suspend fun sendFriendRequest(targetUsername: String): Boolean {
        val clean = targetUsername.removePrefix("@").trim()
        val matching = userDao.searchUsers(clean).first().find { it.username.equals(clean, ignoreCase = true) }
        if (matching != null && !matching.isCurrentUser) {
            val friendship = Friendship(
                id = "fr_" + UUID.randomUUID().toString().take(8),
                requesterId = "user_me",
                addresseeId = matching.id,
                status = "PENDING_SENT"
            )
            friendshipDao.insertFriendship(friendship)
            return true
        }
        return false
    }

    suspend fun acceptFriendRequest(friendshipId: String) {
        friendshipDao.updateStatus(friendshipId, "ACCEPTED")
    }

    suspend fun declineFriendRequest(friendshipId: String) {
        friendshipDao.deleteFriendship(friendshipId)
    }

    suspend fun blockUser(friendshipId: String) {
        friendshipDao.updateStatus(friendshipId, "BLOCKED")
    }

    suspend fun unblockUser(friendshipId: String) {
        friendshipDao.deleteFriendship(friendshipId)
    }

    // --- Conversations (DMs & Groups) ---
    fun getAllConversations(): Flow<List<Conversation>> = conversationDao.getAllConversations()
    fun getConversation(id: String): Flow<Conversation?> = conversationDao.getConversationById(id)

    suspend fun startDirectMessageWith(user: User): String {
        // Check if existing conversation with recipient
        val existing = conversationDao.getAllConversations().first().find { it.recipientUserId == user.id }
        if (existing != null) {
            return existing.id
        }
        val convId = "dm_" + UUID.randomUUID().toString().take(8)
        val conv = Conversation(
            id = convId,
            type = "DIRECT",
            name = user.displayName,
            iconEmoji = "💬",
            recipientUserId = user.id,
            lastMessage = "Started a conversation with ${user.displayName}",
            lastMessageTime = System.currentTimeMillis()
        )
        conversationDao.insertConversation(conv)
        return convId
    }

    suspend fun createGroupChat(name: String, memberNames: List<String>): String {
        val convId = "grp_" + UUID.randomUUID().toString().take(8)
        val conv = Conversation(
            id = convId,
            type = "GROUP",
            name = name,
            iconEmoji = "👥",
            lastMessage = "Group created with ${memberNames.joinToString(", ")}",
            lastMessageTime = System.currentTimeMillis()
        )
        conversationDao.insertConversation(conv)
        return convId
    }

    // --- Notifications ---
    fun getAllNotifications(): Flow<List<NotificationItem>> = notificationDao.getAllNotifications()
    fun getUnreadNotificationsCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun markAllNotificationsRead() {
        notificationDao.markAllAsRead()
    }

    // --- Voice Channels ---
    fun connectToVoice(channel: Channel, server: Server) {
        val currentUser = VoiceParticipantState(
            userId = "user_me",
            name = "Alex (You)",
            avatarBgColor = 0xFF8B5CF6,
            isSpeaking = false,
            isMuted = false
        )
        val mockPeers = listOf(
            VoiceParticipantState("user_samir", "Samir", 0xFFF97316, isSpeaking = true, isMuted = false),
            VoiceParticipantState("user_sarah", "Sarah", 0xFFEC4899, isSpeaking = false, isMuted = true),
            VoiceParticipantState("user_john", "John", 0xFF06B6D4, isSpeaking = false, isMuted = false)
        )
        _voiceState.value = ActiveVoiceState(
            isConnected = true,
            channelId = channel.id,
            channelName = channel.name,
            serverId = server.id,
            serverName = server.name,
            isMuted = false,
            isDeafened = false,
            isSpeakerOn = true,
            participants = listOf(currentUser) + mockPeers
        )
    }

    fun toggleVoiceMute() {
        val current = _voiceState.value
        val newMute = !current.isMuted
        val updatedParticipants = current.participants.map {
            if (it.userId == "user_me") it.copy(isMuted = newMute) else it
        }
        _voiceState.value = current.copy(isMuted = newMute, participants = updatedParticipants)
    }

    fun toggleVoiceDeafen() {
        val current = _voiceState.value
        val newDeafen = !current.isDeafened
        _voiceState.value = current.copy(isDeafened = newDeafen)
    }

    fun toggleVoiceSpeaker() {
        val current = _voiceState.value
        _voiceState.value = current.copy(isSpeakerOn = !current.isSpeakerOn)
    }

    fun leaveVoice() {
        _voiceState.value = ActiveVoiceState(isConnected = false)
    }

    // --- Moderation ---
    fun getReports(serverId: String): Flow<List<ModerationReport>> = moderationDao.getReportsForServer(serverId)
    fun getAuditLogs(serverId: String): Flow<List<AuditLogItem>> = moderationDao.getAuditLogsForServer(serverId)

    suspend fun submitReport(serverId: String, reporter: String, target: String, reason: String) {
        moderationDao.insertReport(
            ModerationReport(
                id = UUID.randomUUID().toString(),
                serverId = serverId,
                reporterName = reporter,
                reportedTarget = target,
                reason = reason,
                status = "PENDING"
            )
        )
    }

    suspend fun performModerationAction(serverId: String, action: String, details: String) {
        moderationDao.insertAuditLog(
            AuditLogItem(
                id = UUID.randomUUID().toString(),
                serverId = serverId,
                actorName = "Alex (Admin)",
                action = action,
                details = details
            )
        )
    }

    // --- Initial Seeding ---
    private suspend fun seedInitialDataIfNeeded() {
        val existing = userDao.getCurrentUser().first()
        if (existing != null) return

        // 1. Current User
        val me = User(
            id = "user_me",
            username = "alex_rivers",
            displayName = "Alex Rivers",
            avatarUrl = "",
            bio = "Crafting real digital spaces. Gamer, builder, night owl 🦉",
            status = "ONLINE",
            customStatus = "Gaming with friends 🎮",
            joinedDate = "September 2026",
            isCurrentUser = true,
            avatarBgColor = 0xFF8B5CF6
        )

        // 2. Real Friends
        val samir = User(
            id = "user_samir",
            username = "samir_4821",
            displayName = "Samir",
            bio = "Founder of Gaming Friends. Always up for a match 🔥",
            status = "ONLINE",
            customStatus = "Playing Valorant with squad 🎮",
            joinedDate = "August 2026",
            avatarBgColor = 0xFFF97316
        )
        val sarah = User(
            id = "user_sarah",
            username = "sarah_art",
            displayName = "Sarah Chen",
            bio = "Visual designer, anime enthusiast & matcha latte lover 🍵",
            status = "ONLINE",
            customStatus = "Sketching new UI themes ✨",
            joinedDate = "August 2026",
            avatarBgColor = 0xFFEC4899
        )
        val john = User(
            id = "user_john",
            username = "john_code",
            displayName = "John Miller",
            bio = "Software engineer, open source lover, coffee addict ☕",
            status = "IDLE",
            customStatus = "Writing Kotlin coroutines",
            joinedDate = "September 2026",
            avatarBgColor = 0xFF06B6D4
        )
        val maya = User(
            id = "user_maya",
            username = "maya_beats",
            displayName = "Maya Ortiz",
            bio = "Music producer & DJ. Check out my new soundcloud set! 🎧",
            status = "DND",
            customStatus = "In Studio recording vocal stems 🔴",
            joinedDate = "September 2026",
            avatarBgColor = 0xFF10B981
        )
        val liam = User(
            id = "user_liam",
            username = "liam_adventures",
            displayName = "Liam Vance",
            bio = "Backpacking the Pacific Crest Trail 🌲",
            status = "OFFLINE",
            customStatus = "Off grid in Yosemite",
            joinedDate = "July 2026",
            avatarBgColor = 0xFF64748B
        )
        val elena = User(
            id = "user_elena",
            username = "elena_music",
            displayName = "Elena Rostova",
            bio = "Indie game soundtrack composer",
            status = "ONLINE",
            customStatus = "Composing boss fight theme 🎹",
            joinedDate = "September 2026",
            avatarBgColor = 0xFFE11D48
        )
        val marcus = User(
            id = "user_marcus",
            username = "marcus_dev",
            displayName = "Marcus Thorne",
            bio = "Android enthusiast exploring Jetpack Compose",
            status = "IDLE",
            customStatus = "Optimizing Room queries",
            joinedDate = "September 2026",
            avatarBgColor = 0xFF3B82F6
        )

        userDao.insertUsers(me, samir, sarah, john, maya, liam, elena, marcus)

        // 3. Friendships
        val f1 = Friendship("fr_1", "user_me", "user_samir", "ACCEPTED")
        val f2 = Friendship("fr_2", "user_me", "user_sarah", "ACCEPTED")
        val f3 = Friendship("fr_3", "user_me", "user_john", "ACCEPTED")
        val f4 = Friendship("fr_4", "user_me", "user_maya", "ACCEPTED")
        val f5 = Friendship("fr_5", "user_me", "user_liam", "ACCEPTED")
        val f6 = Friendship("fr_6", "user_elena", "user_me", "PENDING_INCOMING")
        val f7 = Friendship("fr_7", "user_me", "user_marcus", "PENDING_SENT")
        friendshipDao.insertFriendships(f1, f2, f3, f4, f5, f6, f7)

        // 4. Communities / Servers
        val s1 = Server(
            id = "srv_gaming",
            ownerId = "user_samir",
            name = "Gaming Friends",
            description = "The official squad server for daily multiplayer sessions, clips & banter.",
            iconEmoji = "🎮",
            iconColor = 0xFF7C3AED,
            privacy = "PUBLIC",
            memberCount = 127,
            inviteCode = "SAMIR-X7K29Q"
        )
        val s2 = Server(
            id = "srv_dev",
            ownerId = "user_me",
            name = "Dev Collective",
            description = "Builders, designers and engineers sharing architectures & side projects.",
            iconEmoji = "⚡",
            iconColor = 0xFF06B6D4,
            privacy = "PUBLIC",
            memberCount = 48,
            inviteCode = "SAMIR-DEV99X"
        )
        val s3 = Server(
            id = "srv_study",
            ownerId = "user_sarah",
            name = "Study Lounge",
            description = "Quiet co-working, pomodoro study voice rooms and knowledge sharing.",
            iconEmoji = "📚",
            iconColor = 0xFF10B981,
            privacy = "PRIVATE",
            memberCount = 19,
            inviteCode = "SAMIR-STUDY3"
        )
        serverDao.insertServers(s1, s2, s3)

        // 5. Channels
        val s1Channels = listOf(
            Channel("ch_g_welcome", "srv_gaming", "welcome", "ANNOUNCEMENT", "INFORMATION", "Welcome to Gaming Friends! Read the rules below.", 0),
            Channel("ch_g_rules", "srv_gaming", "rules", "ANNOUNCEMENT", "INFORMATION", "Be respectful, no spam, keep voice channels civil.", 1),
            Channel("ch_g_general", "srv_gaming", "general", "TEXT", "TEXT", "General chat for all squad members", 2),
            Channel("ch_g_gaming", "srv_gaming", "gaming", "TEXT", "TEXT", "LFG, match clips, strategies & game nights", 3),
            Channel("ch_g_memes", "srv_gaming", "memes", "TEXT", "TEXT", "Only top-tier gaming memes allowed", 4),
            Channel("ch_g_voice_lobby", "srv_gaming", "Lobby", "VOICE", "VOICE", "Casual voice hangout", 5),
            Channel("ch_g_voice_ranked", "srv_gaming", "Ranked Squad", "VOICE", "VOICE", "Competitive matches voice", 6),
            Channel("ch_g_voice_chill", "srv_gaming", "Chill & LoFi", "VOICE", "VOICE", "Low volume music & chatting", 7)
        )
        val s2Channels = listOf(
            Channel("ch_d_welcome", "srv_dev", "announcements", "ANNOUNCEMENT", "INFORMATION", "Updates from the developer community", 0),
            Channel("ch_d_general", "srv_dev", "general", "TEXT", "TEXT", "General tech & developer chat", 1),
            Channel("ch_d_android", "srv_dev", "android-compose", "TEXT", "TEXT", "Kotlin, Compose & Room architecture", 2),
            Channel("ch_d_voice", "srv_dev", "Co-Working Space", "VOICE", "VOICE", "Silent screenshare & pairing voice", 3)
        )
        val s3Channels = listOf(
            Channel("ch_s_general", "srv_study", "general", "TEXT", "TEXT", "Study sessions scheduling", 0),
            Channel("ch_s_voice", "srv_study", "Pomodoro Room", "VOICE", "VOICE", "Focus music & study presence", 1)
        )
        channelDao.insertChannels(*(s1Channels + s2Channels + s3Channels).toTypedArray())

        // 6. Messages in #general of Gaming Friends
        val m1 = Message(
            id = "msg_1",
            channelId = "ch_g_general",
            authorId = "user_samir",
            authorName = "Samir",
            authorUsername = "samir_4821",
            authorColor = 0xFFF97316,
            content = "Hey everyone 👋 Who's playing tonight? We need 2 more for the tournament squad!",
            createdAt = System.currentTimeMillis() - 3600000 * 3
        )
        val m2 = Message(
            id = "msg_2",
            channelId = "ch_g_general",
            authorId = "user_me",
            authorName = "Alex Rivers",
            authorUsername = "alex_rivers",
            authorColor = 0xFF8B5CF6,
            content = "I'm available at 8 PM! Practicing my aim routine right now 🎯",
            replyToId = "msg_1",
            replyToAuthor = "Samir",
            replyToSnippet = "Who's playing tonight? We need 2 more...",
            createdAt = System.currentTimeMillis() - 3600000 * 2
        )
        val m3 = Message(
            id = "msg_3",
            channelId = "ch_g_general",
            authorId = "user_sarah",
            authorName = "Sarah Chen",
            authorUsername = "sarah_art",
            authorColor = 0xFFEC4899,
            content = "Count me in! Just exported the new clan logo banner, check this preview out!",
            attachmentType = "IMAGE",
            attachmentName = "samir_clan_logo_dark.png",
            attachmentSize = "1.4 MB",
            createdAt = System.currentTimeMillis() - 3600000
        )
        val m4 = Message(
            id = "msg_4",
            channelId = "ch_g_general",
            authorId = "user_john",
            authorName = "John Miller",
            authorUsername = "john_code",
            authorColor = 0xFF06B6D4,
            content = "Yeah! Let's get that victory today 😂🏆",
            createdAt = System.currentTimeMillis() - 1800000
        )
        val mPinned = Message(
            id = "msg_pin",
            channelId = "ch_g_general",
            authorId = "user_samir",
            authorName = "Samir",
            authorUsername = "samir_4821",
            authorColor = 0xFFF97316,
            content = "📌 Tournament starts Saturday at 8 PM sharp! Be in the 🔊 Ranked Squad voice channel 15 min early.",
            isPinned = true,
            createdAt = System.currentTimeMillis() - 86400000
        )
        messageDao.insertMessages(m1, m2, m3, m4, mPinned)

        // Reactions on message 1
        val r1 = MessageReaction("r_1", "msg_1", "user_sarah", "Sarah", "🔥")
        val r2 = MessageReaction("r_2", "msg_1", "user_john", "John", "🔥")
        val r3 = MessageReaction("r_3", "msg_1", "user_me", "Alex", "🔥")
        val r4 = MessageReaction("r_4", "msg_1", "user_maya", "Maya", "❤️")
        val r5 = MessageReaction("r_5", "msg_4", "user_me", "Alex", "😂")
        reactionDao.insertReactions(r1, r2, r3, r4, r5)

        // 7. Conversations (DMs and Group Chat)
        val dmSamir = Conversation(
            id = "dm_samir",
            type = "DIRECT",
            name = "Samir",
            iconEmoji = "🎮",
            recipientUserId = "user_samir",
            lastMessage = "Sure 🔥 See you in the voice room at 8",
            lastMessageTime = System.currentTimeMillis() - 900000,
            unreadCount = 1
        )
        val dmSarah = Conversation(
            id = "dm_sarah",
            type = "DIRECT",
            name = "Sarah Chen",
            iconEmoji = "🎨",
            recipientUserId = "user_sarah",
            lastMessage = "Send me that photo from yesterday 😂",
            lastMessageTime = System.currentTimeMillis() - 7200000,
            unreadCount = 0
        )
        val dmJohn = Conversation(
            id = "dm_john",
            type = "DIRECT",
            name = "John Miller",
            iconEmoji = "💻",
            recipientUserId = "user_john",
            lastMessage = "See you tomorrow for the coding review!",
            lastMessageTime = System.currentTimeMillis() - 86400000,
            unreadCount = 0
        )
        val grpSquad = Conversation(
            id = "grp_gaming",
            type = "GROUP",
            name = "Weekend Gaming Squad",
            iconEmoji = "👾",
            lastMessage = "Alex: I'm picking the champion this time",
            lastMessageTime = System.currentTimeMillis() - 1200000,
            unreadCount = 2
        )
        conversationDao.insertConversations(dmSamir, dmSarah, dmJohn, grpSquad)

        // Messages in dm_samir
        val dmM1 = Message(
            id = "dm_msg_1",
            conversationId = "dm_samir",
            authorId = "user_me",
            authorName = "Alex Rivers",
            authorUsername = "alex_rivers",
            authorColor = 0xFF8B5CF6,
            content = "Hey Samir! Are we still on for the stream later?",
            createdAt = System.currentTimeMillis() - 1800000
        )
        val dmM2 = Message(
            id = "dm_msg_2",
            conversationId = "dm_samir",
            authorId = "user_samir",
            authorName = "Samir",
            authorUsername = "samir_4821",
            authorColor = 0xFFF97316,
            content = "Sure 🔥 See you in the voice room at 8",
            createdAt = System.currentTimeMillis() - 900000
        )
        messageDao.insertMessages(dmM1, dmM2)

        // 8. Notifications
        val n1 = NotificationItem(
            id = "notif_1",
            title = "New Message",
            body = "Samir sent you a direct message: \"Sure 🔥 See you in voice at 8\"",
            type = "MESSAGE",
            targetId = "dm_samir",
            isRead = false,
            createdAt = System.currentTimeMillis() - 900000
        )
        val n2 = NotificationItem(
            id = "notif_2",
            title = "Mention in #gaming",
            body = "Samir mentioned you: \"@alex_rivers are you free tonight?\"",
            type = "MENTION",
            targetId = "ch_g_gaming",
            isRead = false,
            createdAt = System.currentTimeMillis() - 3600000
        )
        val n3 = NotificationItem(
            id = "notif_3",
            title = "Friend Request",
            body = "Elena Rostova (@elena_music) wants to be real friends with you.",
            type = "FRIEND_REQUEST",
            targetId = "fr_6",
            isRead = false,
            createdAt = System.currentTimeMillis() - 7200000
        )
        val n4 = NotificationItem(
            id = "notif_4",
            title = "Community Invite",
            body = "Samir invited you to join \"Gaming Friends\"",
            type = "INVITE",
            targetId = "srv_gaming",
            isRead = true,
            createdAt = System.currentTimeMillis() - 86400000
        )
        notificationDao.insertNotifications(n1, n2, n3, n4)

        // 9. Initial Audit Log
        val al1 = AuditLogItem(
            id = "log_1",
            serverId = "srv_gaming",
            actorName = "Samir",
            action = "CREATED_CHANNEL",
            details = "Created #memes text channel",
            timestamp = System.currentTimeMillis() - 86400000 * 2
        )
        val al2 = AuditLogItem(
            id = "log_2",
            serverId = "srv_gaming",
            actorName = "Samir",
            action = "PINNED_MESSAGE",
            details = "Pinned tournament schedule announcement in #general",
            timestamp = System.currentTimeMillis() - 86400000
        )
        moderationDao.insertAuditLog(al1)
        moderationDao.insertAuditLog(al2)
    }
}
