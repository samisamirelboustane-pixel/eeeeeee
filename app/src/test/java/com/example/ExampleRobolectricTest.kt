package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SamirDatabase
import com.example.data.model.Message
import com.example.data.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: SamirDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = SamirDatabase.getInstance(context)
    }

    @Test
    fun `read string from context verifies app name is SAMIR`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("SAMIR", appName)
    }

    @Test
    fun `user dao insert and retrieve works`() = runBlocking {
        val userDao = db.userDao()
        val testUser = User(
            id = "test_samir_user",
            username = "test_samir",
            displayName = "Samir Test",
            bio = "Real Friends Only",
            status = "ONLINE",
            isCurrentUser = false
        )
        userDao.insertUsers(testUser)

        val retrieved = userDao.getUserById("test_samir_user")
        assertNotNull(retrieved)
        assertEquals("test_samir", retrieved?.username)
        assertEquals("Samir Test", retrieved?.displayName)
    }

    @Test
    fun `message dao handles insert and channel queries`() = runBlocking {
        val msgDao = db.messageDao()
        val testMsg = Message(
            id = "test_msg_1",
            channelId = "ch_test_channel",
            authorId = "user_me",
            authorName = "Alex Rivers",
            authorUsername = "alex_rivers",
            content = "Hello real friends on SAMIR!",
            createdAt = System.currentTimeMillis()
        )
        msgDao.insertMessage(testMsg)

        val messages = msgDao.getMessagesForChannel("ch_test_channel").first()
        assertTrue(messages.isNotEmpty())
        assertEquals("Hello real friends on SAMIR!", messages.first().content)
    }
}
