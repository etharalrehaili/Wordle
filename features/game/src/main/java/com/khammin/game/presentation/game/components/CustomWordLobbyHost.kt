package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.buttons.GameButtonVariant
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R
import com.khammin.game.presentation.game.contract.WaitingPlayer
import java.util.Locale

@Composable
fun CustomWordLobbyHost(
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    avatarUrl: String? = null,
    waitingPlayers: List<WaitingPlayer>,
    roomId: String,
    onStart: (word: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var customWord by remember { mutableStateOf("") }

    val allReady  = waitingPlayers.isNotEmpty() && waitingPlayers.all { it.isReady }
    val canStart  = allReady && customWord.length in 4..6
    val totalPlayers = waitingPlayers.size + 1

    LazyColumn(
        modifier            = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding      = PaddingValues(top = 16.dp, bottom = 32.dp),
    ) {

        // ── Room code ──────────────────────────────────────────────
        item { RoomCodeCard(roomId = roomId) }

        // ── Custom word input ──────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.logoBlue.copy(alpha = 0.06f))
                    .border(1.dp, colors.logoBlue.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.logoBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Edit,
                            contentDescription = null,
                            tint               = colors.logoBlue,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    WordleText(
                        text       = stringResource(R.string.custom_word_hint),
                        color      = colors.logoBlue,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                OutlinedTextField(
                    value         = customWord,
                    onValueChange = { value ->
                        customWord = value.filter { it.isLetter() }.take(6).uppercase()
                    },
                    placeholder  = {
                        Text(
                            text  = "ABCDEF",
                            color = colors.body.copy(alpha = 0.3f),
                        )
                    },
                    singleLine   = true,
                    modifier     = Modifier.fillMaxWidth(),
                    textStyle    = androidx.compose.ui.text.TextStyle(
                        fontSize      = 22.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 6.sp,
                        textAlign     = TextAlign.Center,
                        color         = colors.title,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
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
                    shape = RoundedCornerShape(12.dp),
                )

                if (customWord.isNotEmpty()) {
                    val len = customWord.length
                    val isValidLen = len in 4..6
                    WordleText(
                        text      = "${String.format(Locale.US, "%d", len)} / 6 · ${if (isValidLen) stringResource(R.string.custom_word_valid) else stringResource(R.string.custom_word_invalid_length)}",
                        color     = if (isValidLen) colors.logoGreen else colors.logoPink,
                        fontSize  = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // ── Player list ────────────────────────────────────────────
        item {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Groups,
                            contentDescription = null,
                            tint               = colors.body.copy(alpha = 0.5f),
                            modifier           = Modifier.size(16.dp)
                        )
                        WordleText(
                            text       = String.format(Locale.US, stringResource(R.string.custom_word_players_count), totalPlayers),
                            color      = colors.body.copy(alpha = 0.6f),
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (waitingPlayers.isNotEmpty() && !allReady) {
                        WordleText(
                            text      = stringResource(R.string.lobby_waiting_ready),
                            color     = colors.logoOrange.copy(alpha = 0.8f),
                            fontSize  = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
                ) {
                    LobbyPlayerRow(
                        name        = myName.ifBlank { stringResource(R.string.lobby_you) },
                        badge       = stringResource(R.string.lobby_badge_host),
                        badgeColor  = colors.logoBlue,
                        avatarColor = avatarColor,
                        avatarEmoji = avatarEmoji,
                        avatarUrl   = avatarUrl,
                    )
                    if (waitingPlayers.isEmpty()) {
                        HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            WordleText(
                                text      = stringResource(R.string.multiplayer_waiting_for_players),
                                color     = colors.body.copy(alpha = 0.4f),
                                fontSize  = 13.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        waitingPlayers.forEach { player ->
                            HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                            LobbyPlayerRow(
                                name        = player.name,
                                badge       = if (player.isReady)
                                    stringResource(R.string.lobby_badge_ready)
                                else
                                    stringResource(R.string.lobby_badge_not_ready),
                                badgeColor  = if (player.isReady) colors.logoGreen else colors.logoPink,
                                avatarColor  = player.avatarColor,
                                avatarEmoji  = player.avatarEmoji,
                                avatarUrl    = player.avatarUrl,
                                isAfk        = player.isAfk,
                                afkCountdown = player.afkCountdown,
                            )
                        }
                    }
                }
            }
        }

        // ── Start button ───────────────────────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            GameButton(
                label    = stringResource(R.string.lobby_start_game),
                onClick  = { onStart(customWord) },
                enabled  = canStart,
                variant  = if (canStart) GameButtonVariant.Primary else GameButtonVariant.Muted,
                modifier = Modifier.fillMaxWidth()
            )
            if (!canStart) {
                Spacer(Modifier.height(6.dp))
                val hint = when {
                    customWord.length !in 4..6 -> stringResource(R.string.lobby_hint_enter_word)
                    waitingPlayers.isEmpty()   -> stringResource(R.string.lobby_hint_need_players)
                    !allReady                  -> stringResource(R.string.lobby_hint_wait_ready)
                    else                       -> ""
                }
                if (hint.isNotEmpty()) {
                    WordleText(
                        text      = hint,
                        color     = colors.body.copy(alpha = 0.45f),
                        fontSize  = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
