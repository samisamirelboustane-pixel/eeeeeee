package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserFlow(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 0")
    fun getAllOtherUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(vararg users: User)

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY createdAt ASC")
    fun getAllServers(): Flow<List<Server>>

    @Query("SELECT * FROM servers WHERE id = :id")
    fun getServerById(id: String): Flow<Server?>

    @Query("SELECT * FROM servers WHERE inviteCode = :code LIMIT 1")
    suspend fun findServerByInviteCode(code: String): Server?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: Server)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(vararg servers: Server)

    @Update
    suspend fun updateServer(server: Server)

    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun deleteServer(id: String)
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE serverId = :serverId ORDER BY position ASC, name ASC")
    fun getChannelsForServer(serverId: String): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE id = :id")
    fun getChannelById(id: String): Flow<Channel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(vararg channels: Channel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel)

    @Query("DELETE FROM channels WHERE id = :id")
    suspend fun deleteChannel(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE channelId = :channelId ORDER BY createdAt ASC")
    fun getMessagesForChannel(channelId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE channelId = :channelId AND isPinned = 1 ORDER BY createdAt DESC")
    fun getPinnedMessages(channelId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(vararg messages: Message)

    @Update
    suspend fun updateMessage(message: Message)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchMessages(query: String): Flow<List<Message>>
}

@Dao
interface ReactionDao {
    @Query("SELECT * FROM message_reactions WHERE messageId = :messageId")
    fun getReactionsForMessage(messageId: String): Flow<List<MessageReaction>>

    @Query("SELECT * FROM message_reactions")
    fun getAllReactions(): Flow<List<MessageReaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaction(reaction: MessageReaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReactions(vararg reactions: MessageReaction)

    @Query("DELETE FROM message_reactions WHERE messageId = :messageId AND userId = :userId AND emoji = :emoji")
    suspend fun deleteReaction(messageId: String, userId: String, emoji: String)
}

@Dao
interface FriendshipDao {
    @Query("SELECT * FROM friendships")
    fun getAllFriendships(): Flow<List<Friendship>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendship(friendship: Friendship)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendships(vararg friendships: Friendship)

    @Query("UPDATE friendships SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM friendships WHERE id = :id")
    suspend fun deleteFriendship(id: String)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getConversationById(id: String): Flow<Conversation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(vararg conversations: Conversation)

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageTime = :time WHERE id = :id")
    suspend fun updateLastMessage(id: String, lastMessage: String, time: Long)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(vararg notifications: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

@Dao
interface ModerationDao {
    @Query("SELECT * FROM moderation_reports WHERE serverId = :serverId ORDER BY timestamp DESC")
    fun getReportsForServer(serverId: String): Flow<List<ModerationReport>>

    @Query("SELECT * FROM audit_logs WHERE serverId = :serverId ORDER BY timestamp DESC")
    fun getAuditLogsForServer(serverId: String): Flow<List<AuditLogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ModerationReport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogItem)
}
