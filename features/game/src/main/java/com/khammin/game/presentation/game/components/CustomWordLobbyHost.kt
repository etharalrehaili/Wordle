package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.buttons.GameButtonVariant
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R
import com.khammin.game.presentation.game.contract.WaitingPlayer

@Composable
fun CustomWordLobbyHost(
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    avatarUrl: String? = null,
    waitingPlayers: List<WaitingPlayer>,
    roomId: String,
    onStart: (word: String) -> Unit,
    onUpdateProfile: (name: String, color: Long?, emoji: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var customWord by remember { mutableStateOf("") }

    LazyColumn(
        modifier            = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding      = PaddingValues(top = 12.dp, bottom = 32.dp),
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
            OutlinedTextField(
                value         = customWord,
                onValueChange = { value ->
                    customWord = value.filter { it.isLetter() }.take(6).uppercase()
                },
                label      = { Text(stringResource(R.string.custom_word_hint)) },
                singleLine = true,
                modifier   = Modifier.fillMaxWidth(),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.logoBlue,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor    = colors.logoBlue,
                    unfocusedLabelColor  = colors.body.copy(alpha = 0.5f),
                    focusedTextColor     = colors.title,
                    unfocusedTextColor   = colors.title,
                    cursorColor          = colors.logoBlue,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType   = KeyboardType.Text,
                    imeAction      = ImeAction.Done,
                ),
            )
        }

        item {
            Text(
                text          = stringResource(R.string.custom_word_players_count, waitingPlayers.size + 1),
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
                    text     = stringResource(R.string.multiplayer_waiting_for_players),
                    color    = colors.body.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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
                        name        = myName.ifBlank { stringResource(R.string.lobby_you) },
                        badge       = stringResource(R.string.lobby_badge_host),
                        badgeColor  = colors.logoGreen,
                        avatarColor = avatarColor,
                        avatarEmoji = avatarEmoji,
                        avatarUrl   = avatarUrl,
                    )
                    waitingPlayers.forEach { player ->
                        LobbyPlayerRow(
                            name        = player.name,
                            badge       = if (player.isReady) stringResource(R.string.lobby_badge_ready) else null,
                            badgeColor  = colors.logoGreen,
                            avatarColor = player.avatarColor,
                            avatarEmoji = player.avatarEmoji,
                            avatarUrl   = player.avatarUrl,
                        )
                    }
                }
            }
        }

        val allReady = waitingPlayers.isNotEmpty() && waitingPlayers.all { it.isReady }
        val canStart = allReady && customWord.length in 4..6

        if (waitingPlayers.isNotEmpty() && !allReady) {
            item {
                Text(
                    text     = stringResource(R.string.lobby_waiting_ready),
                    color    = colors.body.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                )
            }
        }

        item {
            GameButton(
                label    = stringResource(R.string.lobby_start_game),
                onClick  = { onStart(customWord) },
                enabled  = canStart,
                variant  = if (canStart) GameButtonVariant.Primary else GameButtonVariant.Muted,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}