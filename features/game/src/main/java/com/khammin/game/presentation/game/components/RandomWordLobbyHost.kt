package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.presentation.game.contract.WaitingPlayer

@Composable
fun RandomWordLobbyHost(
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    avatarUrl: String? = null,
    waitingPlayers: List<WaitingPlayer>,
    roomId: String,
    onStart: () -> Unit,
    onUpdateProfile: (name: String, color: Long?, emoji: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
    ) {
        item { RoomCodeCard(roomId = roomId) }

        item {
            ProfileEditCard(
                myName      = myName,
                avatarColor = avatarColor,
                avatarEmoji = avatarEmoji,
                avatarUrl   = avatarUrl,
                onSave      = onUpdateProfile,
            )
        }

        item {
            Text(
                text          = "Players (${waitingPlayers.size + 1}/3)",
                color         = colors.body.copy(alpha = 0.6f),
                fontSize      = 12.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier      = Modifier.fillMaxWidth(),
            )
        }

        if (waitingPlayers.isEmpty()) {
            item {
                Text(
                    text     = "Waiting for players to join…",
                    color    = colors.body.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                ) {
                    LobbyPlayerRow(
                        name        = myName.ifBlank { "You" },
                        badge       = "HOST",
                        badgeColor  = colors.buttonTeal,
                        avatarColor = avatarColor,
                        avatarEmoji = avatarEmoji,
                        avatarUrl   = avatarUrl,
                    )
                    waitingPlayers.forEach { player ->
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

        item {
            Button(
                onClick  = onStart,
                enabled  = waitingPlayers.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = colors.buttonTeal,
                    disabledContainerColor = colors.buttonTeal.copy(alpha = 0.3f),
                )
            ) {
                Text(
                    text       = "Start Game",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colors.background,
                )
            }
        }
    }
}
