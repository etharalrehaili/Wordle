package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.presentation.game.contract.WaitingPlayer

@Composable
fun CustomWordLobbyGuest(
    modifier: Modifier = Modifier,
    hostName: String,
    hostAvatarColor: Long? = null,
    hostAvatarEmoji: String? = null,
    hostAvatarUrl: String? = null,
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    avatarUrl: String? = null,
    otherPlayers: List<WaitingPlayer>,
    onUpdateProfile: (name: String, color: Long?, emoji: String?) -> Unit,
) {
    val totalPlayers = 1 + 1 + otherPlayers.size // host + me + others

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 32.dp),
    ) {
        item {
            Text(
                text       = "Waiting for host to start…",
                color      = colors.title,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
            )
        }

        // My profile card
        item {
            ProfileEditCard(
                myName      = myName,
                avatarColor = avatarColor,
                avatarEmoji = avatarEmoji,
                avatarUrl   = avatarUrl,
                onSave      = onUpdateProfile,
            )
        }

        // Players header
        item {
            Text(
                text          = "Players ($totalPlayers/4)",
                color         = colors.body.copy(alpha = 0.6f),
                fontSize      = 12.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier      = Modifier.fillMaxWidth(),
            )
        }

        // Players list
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
            ) {
                // Host row
                LobbyPlayerRow(
                    name        = hostName.ifBlank { "Host" },
                    badge       = "HOST",
                    badgeColor  = colors.buttonTeal,
                    avatarColor = hostAvatarColor,
                    avatarEmoji = hostAvatarEmoji,
                    avatarUrl   = hostAvatarUrl,
                )
                // Me
                LobbyPlayerRow(
                    name        = myName.ifBlank { "You" },
                    badge       = "You",
                    badgeColor  = colors.buttonPink,
                    avatarColor = avatarColor,
                    avatarEmoji = avatarEmoji,
                    avatarUrl   = avatarUrl,
                )
                // Other guests
                otherPlayers.forEach { player ->
                    LobbyPlayerRow(
                        name        = player.name,
                        avatarColor = player.avatarColor,
                        avatarEmoji = player.avatarEmoji,
                        avatarUrl   = player.avatarUrl,
                    )
                }
            }
        }
    }
}
