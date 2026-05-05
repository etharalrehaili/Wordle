package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
fun RandomWordLobbyHost(
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    avatarUrl: String? = null,
    waitingPlayers: List<WaitingPlayer>,
    roomId: String,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allReady     = waitingPlayers.isNotEmpty() && waitingPlayers.all { it.isReady }
    val canStart     = allReady
    val totalPlayers = waitingPlayers.size + 1

    LazyColumn(
        modifier            = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding      = PaddingValues(top = 16.dp, bottom = 32.dp),
    ) {

        // ── Room code ──────────────────────────────────────────────
        item { RoomCodeCard(roomId = roomId) }

        // ── Player list ────────────────────────────────────────────
        item {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
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
                            modifier           = Modifier.size(16.dp),
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
                            text       = stringResource(R.string.lobby_waiting_ready),
                            color      = colors.logoOrange.copy(alpha = 0.8f),
                            fontSize   = 12.sp,
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
                            modifier              = Modifier
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
                onClick  = onStart,
                enabled  = canStart,
                variant  = if (canStart) GameButtonVariant.Primary else GameButtonVariant.Muted,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!canStart) {

                Spacer(Modifier.height(6.dp))

                val hint = when {
                    waitingPlayers.isEmpty() -> stringResource(R.string.lobby_hint_need_players)
                    !allReady                -> stringResource(R.string.lobby_hint_wait_ready)
                    else                     -> ""
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
