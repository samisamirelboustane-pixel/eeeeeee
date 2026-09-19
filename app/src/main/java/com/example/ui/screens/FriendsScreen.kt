package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Friendship
import com.example.data.model.User
import com.example.ui.components.AvatarWithStatus
import com.example.ui.theme.*

@Composable
fun FriendsScreen(
    friends: List<User>,
    friendships: List<Friendship>,
    allOtherUsers: List<User>,
    onAddFriendClick: () -> Unit,
    onMessageFriend: (User) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Online, 2: Requests, 3: Blocked

    val acceptedFriends = remember(friends, friendships) {
        val acceptedIds = friendships.filter { it.status == "ACCEPTED" }
            .flatMap { listOf(it.requesterId, it.addresseeId) }
            .filter { it != "user_me" }
            .toSet()
        friends.filter { acceptedIds.contains(it.id) }
    }

    val onlineFriends = remember(acceptedFriends) {
        acceptedFriends.filter { it.status.uppercase() == "ONLINE" || it.status.uppercase() == "IDLE" }
    }

    val incomingRequests = remember(friendships, allOtherUsers) {
        friendships.filter { it.status == "PENDING_INCOMING" }.mapNotNull { f ->
            val u = allOtherUsers.find { it.id == f.requesterId }
            if (u != null) Pair(f, u) else null
        }
    }

    val sentRequests = remember(friendships, allOtherUsers) {
        friendships.filter { it.status == "PENDING_SENT" }.mapNotNull { f ->
            val u = allOtherUsers.find { it.id == f.addresseeId }
            if (u != null) Pair(f, u) else null
        }
    }

    val blockedUsers = remember(friendships, allOtherUsers) {
        friendships.filter { it.status == "BLOCKED" }.mapNotNull { f ->
            val u = allOtherUsers.find { it.id == f.addresseeId || it.id == f.requesterId && it.id != "user_me" }
            if (u != null) Pair(f, u) else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("friends_screen")
    ) {
        // Add Friend Top Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SamirPrimary.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, SamirPrimary.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Add Real Friends",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SamirPrimaryLight
                    )
                    Text(
                        text = "No fake users or bots. Only real connections.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddFriendClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SamirPrimary),
                    modifier = Modifier.testTag("add_friend_banner_btn")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SamirPrimary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All (${acceptedFriends.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Online (${onlineFriends.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    val count = incomingRequests.size
                    Text(
                        text = if (count > 0) "Requests ($count)" else "Requests",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (count > 0) SamirFlame else MaterialTheme.colorScheme.onSurface
                    )
                }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Blocked", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> FriendList(acceptedFriends, onMessageFriend, onBlockUser)
                1 -> FriendList(onlineFriends, onMessageFriend, onBlockUser)
                2 -> {
                    // Requests Tab
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        if (incomingRequests.isEmpty() && sentRequests.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No pending friend requests.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (incomingRequests.isNotEmpty()) {
                            item {
                                Text(
                                    text = "INCOMING REQUESTS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SamirPrimary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(incomingRequests) { (f, user) ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarWithStatus(
                                            displayName = user.displayName,
                                            avatarBgColor = user.avatarBgColor,
                                            status = user.status,
                                            size = 44.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(user.displayName, fontWeight = FontWeight.Bold)
                                            Text("@${user.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(
                                            onClick = { onAcceptRequest(f.id) },
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(SamirStatusOnline)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { onDeclineRequest(f.id) },
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Decline", tint = SamirError)
                                        }
                                    }
                                }
                            }
                        }

                        if (sentRequests.isNotEmpty()) {
                            item {
                                Text(
                                    text = "SENT REQUESTS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SamirSecondaryLight,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                            items(sentRequests) { (f, user) ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarWithStatus(
                                            displayName = user.displayName,
                                            avatarBgColor = user.avatarBgColor,
                                            status = user.status,
                                            size = 44.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(user.displayName, fontWeight = FontWeight.Bold)
                                            Text("@${user.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "Pending",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Blocked tab
                    if (blockedUsers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No blocked users.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(blockedUsers) { (f, user) ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarWithStatus(
                                            displayName = user.displayName,
                                            avatarBgColor = user.avatarBgColor,
                                            status = "OFFLINE",
                                            size = 44.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(user.displayName, fontWeight = FontWeight.Bold)
                                            Text("@${user.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        OutlinedButton(
                                            onClick = { onUnblockUser(f.id) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Unblock", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendList(
    friends: List<User>,
    onMessageFriend: (User) -> Unit,
    onBlockUser: (String) -> Unit
) {
    if (friends.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No friends to display.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(friends) { friend ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SamirSurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("friend_card_${friend.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarWithStatus(
                            displayName = friend.displayName,
                            avatarBgColor = friend.avatarBgColor,
                            status = friend.status,
                            size = 46.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "@${friend.username} • ${friend.customStatus.ifEmpty { friend.status }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = { onMessageFriend(friend) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SamirPrimary.copy(alpha = 0.2f))
                                .testTag("message_friend_${friend.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Message",
                                tint = SamirPrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
