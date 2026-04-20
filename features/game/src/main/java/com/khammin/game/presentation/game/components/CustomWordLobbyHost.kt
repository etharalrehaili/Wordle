package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.theme.GameDesignTheme.colors
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
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 32.dp),
    ) {
        // Room code
        item { RoomCodeCard(roomId = roomId) }

        // My profile card
        item {
            ProfileEditCard(
                myName = myName,
                avatarColor = avatarColor,
                avatarEmoji = avatarEmoji,
                avatarUrl = avatarUrl,
                onSave = onUpdateProfile,
            )
        }

        // Word input
        item {
            OutlinedTextField(
                value = customWord,
                onValueChange = { value ->
                    customWord = value.filter { it.isLetter() }.take(6).uppercase()
                },
                label = { Text("Your secret word (4–6 letters)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.buttonTeal,
                    unfocusedBorderColor = colors.border,
                    focusedLabelColor    = colors.buttonTeal,
                    unfocusedLabelColor  = colors.body.copy(alpha = 0.5f),
                    focusedTextColor     = colors.title,
                    unfocusedTextColor   = colors.title,
                    cursorColor          = colors.buttonTeal,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType   = KeyboardType.Text,
                    imeAction      = ImeAction.Done,
                ),
            )
        }

        // Players header
        item {
            Text(
                text          = "Players (${waitingPlayers.size + 1}/4)",
                color         = colors.body.copy(alpha = 0.6f),
                fontSize      = 12.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier      = Modifier.fillMaxWidth(),
            )
        }

        // Players list
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
                    // Host row (me)
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

        // Start button
        item {
            Button(
                onClick  = { onStart(customWord) },
                enabled  = waitingPlayers.isNotEmpty() && customWord.length in 4..6,
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
