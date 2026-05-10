package com.khammin.game.presentation.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
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
fun RandomWordLobbyGuest(
    modifier: Modifier = Modifier,
    hostName: String,
    hostAvatarColor: Long? = null,
    hostAvatarEmoji: String? = null,
    hostAvatarUrl: String? = null,
    hostIsAfk: Boolean = false,
    hostAfkCountdown: Int? = null,
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    avatarUrl: String? = null,
    otherPlayers: List<WaitingPlayer>,
    isReady: Boolean = false,
    onSetReady: () -> Unit = {},
) {
    val totalPlayers = 1 + 1 + otherPlayers.size

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    LazyColumn(
        modifier            = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding      = PaddingValues(top = 16.dp, bottom = 32.dp),
    ) {

        // ── Waiting header ─────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WordleText(
                    text       = stringResource(R.string.multiplayer_waiting_for_host),
                    color      = colors.logoOrange,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign  = TextAlign.Center,
                )
                WordleText(
                    text      = stringResource(R.string.lobby_guest_waiting_subtitle),
                    color     = colors.body.copy(alpha = 0.55f),
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── Player list ────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    WordleText(
                        text = String.format(
                            Locale.US,
                            stringResource(R.string.custom_word_players_count),
                            totalPlayers
                        ),
                        color = colors.body.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LobbyPlayerRow(
                        name = hostName.ifBlank { stringResource(R.string.lobby_badge_host) },
                        badge = stringResource(R.string.lobby_badge_host),
                        badgeColor = colors.logoBlue,
                        avatarColor = hostAvatarColor,
                        avatarEmoji = hostAvatarEmoji,
                        avatarUrl = hostAvatarUrl,
                        isAfk = hostIsAfk,
                        afkCountdown = hostAfkCountdown,
                    )
                    LobbyPlayerRow(
                        name = myName.ifBlank { stringResource(R.string.lobby_you) },
                        badge = if (isReady) stringResource(R.string.lobby_badge_ready)
                        else stringResource(R.string.lobby_badge_not_ready),
                        badgeColor = if (isReady) colors.logoGreen else colors.logoPink,
                        avatarColor = avatarColor,
                        avatarEmoji = avatarEmoji,
                        avatarUrl = avatarUrl,
                        isAfk = false,
                    )
                    otherPlayers.forEach { player ->
                        LobbyPlayerRow(
                            name = player.name,
                            badge = if (player.isReady)
                                stringResource(R.string.lobby_badge_ready)
                            else
                                stringResource(R.string.lobby_badge_not_ready),
                            badgeColor = if (player.isReady) colors.logoGreen else colors.logoPink,
                            avatarColor = player.avatarColor,
                            avatarEmoji = player.avatarEmoji,
                            avatarUrl = player.avatarUrl,
                            isAfk = player.isAfk,
                            afkCountdown = player.afkCountdown,
                        )
                    }
                }
            }
        }

        // ── Ready button ───────────────────────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            GameButton(
                label    = if (isReady) stringResource(R.string.lobby_ready_done) else stringResource(R.string.lobby_ready),
                onClick  = onSetReady,
                variant  = if (isReady) GameButtonVariant.Muted else GameButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!isReady) {
                Spacer(Modifier.height(6.dp))
                WordleText(
                    text      = stringResource(R.string.lobby_hint_tap_ready),
                    color     = colors.body.copy(alpha = 0.45f),
                    fontSize  = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    } // end CompositionLocalProvider
}
