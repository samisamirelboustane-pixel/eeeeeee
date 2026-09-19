package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Server::class,
        Channel::class,
        Message::class,
        MessageReaction::class,
        Friendship::class,
        Conversation::class,
        NotificationItem::class,
        ModerationReport::class,
        AuditLogItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SamirDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun serverDao(): ServerDao
    abstract fun channelDao(): ChannelDao
    abstract fun messageDao(): MessageDao
    abstract fun reactionDao(): ReactionDao
    abstract fun friendshipDao(): FriendshipDao
    abstract fun conversationDao(): ConversationDao
    abstract fun notificationDao(): NotificationDao
    abstract fun moderationDao(): ModerationDao

    companion object {
        @Volatile
        private var INSTANCE: SamirDatabase? = null

        fun getInstance(context: Context): SamirDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SamirDatabase::class.java,
                    "samir_chat_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
