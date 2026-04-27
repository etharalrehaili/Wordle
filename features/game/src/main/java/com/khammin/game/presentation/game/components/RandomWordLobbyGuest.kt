package com.khammin.game.presentation.game.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
    myName: String,
    avatarColor: Long?,
    avatarEmoji: String?,
    avatarUrl: String? = null,
    otherPlayers: List<WaitingPlayer>,
    isReady: Boolean = false,
    onSetReady: () -> Unit = {},
) {
    val totalPlayers = 1 + 1 + otherPlayers.size

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 0.85f,
        targetValue   = 1.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

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
                    .background(colors.logoOrange.copy(alpha = 0.06f))
                    .border(1.dp, colors.logoOrange.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(pulseScale)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.HourglassEmpty,
                        contentDescription = null,
                        tint               = colors.logoOrange.copy(alpha = pulseAlpha),
                        modifier           = Modifier.size(24.dp),
                    )
                }
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

                    if (!isReady) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(colors.logoPink.copy(alpha = pulseAlpha)),
                            )
                            WordleText(
                                text       = stringResource(R.string.lobby_badge_not_ready),
                                color      = colors.logoPink.copy(alpha = 0.8f),
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
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
                        name        = hostName.ifBlank { stringResource(R.string.lobby_badge_host) },
                        badge       = stringResource(R.string.lobby_badge_host),
                        badgeColor  = colors.logoBlue,
                        avatarColor = hostAvatarColor,
                        avatarEmoji = hostAvatarEmoji,
                        avatarUrl   = hostAvatarUrl,
                    )
                    HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                    LobbyPlayerRow(
                        name        = myName.ifBlank { stringResource(R.string.lobby_you) },
                        badge       = if (isReady) stringResource(R.string.lobby_badge_ready) else stringResource(R.string.lobby_badge_not_ready),
                        badgeColor  = if (isReady) colors.logoGreen else colors.logoPink,
                        avatarColor = avatarColor,
                        avatarEmoji = avatarEmoji,
                        avatarUrl   = avatarUrl,
                    )
                    otherPlayers.forEach { player ->
                        HorizontalDivider(color = colors.border, thickness = 0.5.dp)
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
}
