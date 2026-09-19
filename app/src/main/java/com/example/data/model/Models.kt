package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserStatus(val label: String, val icon: String) {
    ONLINE("Online", "🟢"),
    IDLE("Idle", "🌙"),
    DND("Do Not Disturb", "🔴"),
    OFFLINE("Offline", "⚫")
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String = "",
    val bio: String = "",
    val status: String = "ONLINE",
    val customStatus: String = "",
    val joinedDate: String = "Sep 2026",
    val isCurrentUser: Boolean = false,
    val avatarBgColor: Long = 0xFF8B5CF6
)

@Entity(tableName = "servers")
data class Server(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val description: String,
    val iconUrl: String = "",
    val iconEmoji: String = "🎮",
    val iconColor: Long = 0xFF7C3AED,
    val privacy: String = "PRIVATE", // "PRIVATE" or "PUBLIC"
    val memberCount: Int = 1,
    val inviteCode: String = "SAMIR-X7K29Q",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey val id: String,
    val serverId: String,
    val name: String,
    val type: String = "TEXT", // "TEXT", "VOICE", "ANNOUNCEMENT"
    val category: String = "TEXT", // "INFORMATION", "TEXT", "VOICE"
    val topic: String = "",
    val position: Int = 0
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val channelId: String? = null,
    val conversationId: String? = null,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String = "",
    val authorColor: Long = 0xFF8B5CF6,
    val content: String,
    val replyToId: String? = null,
    val replyToAuthor: String? = null,
    val replyToSnippet: String? = null,
    val isPinned: Boolean = false,
    val attachmentType: String? = null, // "IMAGE", "VIDEO", "FILE", "AUDIO"
    val attachmentName: String? = null,
    val attachmentSize: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null
)

@Entity(tableName = "message_reactions")
data class MessageReaction(
    @PrimaryKey val id: String,
    val messageId: String,
    val userId: String,
    val userName: String,
    val emoji: String
)

@Entity(tableName = "friendships")
data class Friendship(
    @PrimaryKey val id: String,
    val requesterId: String,
    val addresseeId: String,
    val status: String = "ACCEPTED", // "ACCEPTED", "PENDING_INCOMING", "PENDING_SENT", "BLOCKED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val type: String = "DIRECT", // "DIRECT" or "GROUP"
    val name: String,
    val iconUrl: String = "",
    val iconEmoji: String = "💬",
    val recipientUserId: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val type: String = "MESSAGE", // "MESSAGE", "MENTION", "FRIEND_REQUEST", "INVITE", "VOICE"
    val targetId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "moderation_reports")
data class ModerationReport(
    @PrimaryKey val id: String,
    val serverId: String,
    val reporterName: String,
    val reportedTarget: String,
    val reason: String,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogItem(
    @PrimaryKey val id: String,
    val serverId: String,
    val actorName: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ReactionCount(
    val emoji: String,
    val count: Int,
    val userReacted: Boolean
)
