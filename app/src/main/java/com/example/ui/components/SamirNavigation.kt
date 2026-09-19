package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ActiveVoiceState
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainNavTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamirTopBar(
    title: String = "SAMIR",
    subtitle: String? = null,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (title == "SAMIR") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.horizontalGradient(listOf(SamirFlame, SamirPrimary)))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "REAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            navigationIcon?.invoke()
        },
        actions = {
            if (actions != null) {
                actions()
            } else {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("top_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun SamirBottomNav(
    selectedTab: MainNavTab,
    unreadMessagesCount: Int,
    unreadNotifCount: Int,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Home
        NavigationBarItem(
            selected = selectedTab == MainNavTab.HOME,
            onClick = { onTabSelected(MainNavTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.testTag("tab_home"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        // Messages
        NavigationBarItem(
            selected = selectedTab == MainNavTab.MESSAGES,
            onClick = { onTabSelected(MainNavTab.MESSAGES) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadMessagesCount > 0) {
                            Badge(
                                containerColor = SamirFlame,
                                contentColor = Color.White
                            ) {
                                Text("$unreadMessagesCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (selectedTab == MainNavTab.MESSAGES) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Messages"
                    )
                }
            },
            label = { Text("Messages", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.testTag("tab_messages"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        // Friends
        NavigationBarItem(
            selected = selectedTab == MainNavTab.FRIENDS,
            onClick = { onTabSelected(MainNavTab.FRIENDS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.FRIENDS) Icons.Filled.People else Icons.Outlined.PeopleOutline,
                    contentDescription = "Friends"
                )
            },
            label = { Text("Friends", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.testTag("tab_friends"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        // Notifications
        NavigationBarItem(
            selected = selectedTab == MainNavTab.NOTIFICATIONS,
            onClick = { onTabSelected(MainNavTab.NOTIFICATIONS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadNotifCount > 0) {
                            Badge(
                                containerColor = SamirPrimary,
                                contentColor = Color.White
                            ) {
                                Text("$unreadNotifCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (selectedTab == MainNavTab.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
                        contentDescription = "Notifications"
                    )
                }
            },
            label = { Text("Alerts", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.testTag("tab_notifications"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        // Profile
        NavigationBarItem(
            selected = selectedTab == MainNavTab.PROFILE,
            onClick = { onTabSelected(MainNavTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == MainNavTab.PROFILE) Icons.Filled.Person else Icons.Outlined.PersonOutline,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.testTag("tab_profile"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun VoiceActiveFloatingBar(
    voiceState: ActiveVoiceState,
    onBarClick: () -> Unit,
    onMuteToggle: () -> Unit,
    onDeafenToggle: () -> Unit,
    onLeaveVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = voiceState.isConnected,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable(onClick = onBarClick)
                .testTag("active_voice_bar"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF102820),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, SamirStatusOnline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SamirStatusOnline.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Voice Connected",
                        tint = SamirStatusOnline,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔊 ${voiceState.channelName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${voiceState.serverName} • ${voiceState.participants.size} connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = SamirStatusOnline,
                        fontSize = 11.sp
                    )
                }

                // Voice quick actions
                IconButton(
                    onClick = onMuteToggle,
                    modifier = Modifier.size(38.dp).testTag("voice_quick_mute")
                ) {
                    Icon(
                        imageVector = if (voiceState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (voiceState.isMuted) "Unmute" else "Mute",
                        tint = if (voiceState.isMuted) SamirError else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDeafenToggle,
                    modifier = Modifier.size(38.dp).testTag("voice_quick_deafen")
                ) {
                    Icon(
                        imageVector = if (voiceState.isDeafened) Icons.Default.HeadsetOff else Icons.Default.Headset,
                        contentDescription = if (voiceState.isDeafened) "Undeafen" else "Deafen",
                        tint = if (voiceState.isDeafened) SamirError else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onLeaveVoice,
                    modifier = Modifier.size(38.dp).testTag("voice_quick_leave")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Disconnect",
                        tint = SamirError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
