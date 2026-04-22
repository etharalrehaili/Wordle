package com.khammin.game.presentation.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.R as CoreRes
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.presentation.game.contract.OpponentProgress
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private val confettiPalette = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFF4ECDC4),
    Color(0xFF45B7D1), Color(0xFF96CEB4), Color(0xFFFF9FF3),
    Color(0xFF54A0FF), Color(0xFFFFA502), Color(0xFF2ED573), Color(0xFFFF4757),
)

private data class LeaderboardPlayer(
    val name: String,
    val avatarColor: Long?,
    val avatarEmoji: String?,
    val sessionPoints: Int,
    val isMe: Boolean,
)

@Composable
fun GuestGameOverLobby(
    modifier: Modifier = Modifier,
    isWin: Boolean,
    targetWord: String,
    opponentsProgress: Map<String, OpponentProgress>,
    wordLength: Int,
    roundNumber: Int = 1,
    myName: String = "You",
    myAvatarColor: Long? = null,
    myAvatarEmoji: String? = null,
    myUserId: String = "",
    myGuessCount: Int = 0,
    myTotalPoints: Int = 0,
    sessionPoints: Map<String, Int> = emptyMap(),
    playAgainVotes: List<String> = emptyList(),
    hasVotedPlayAgain: Boolean,
    onVotePlayAgain: () -> Unit,
) {
    val accentColor = if (isWin) colors.buttonTeal else colors.buttonPink

    // Build ranked leaderboard
    val meEntry = LeaderboardPlayer(
        name          = myName.ifBlank { "You" },
        avatarColor   = myAvatarColor,
        avatarEmoji   = myAvatarEmoji,
        sessionPoints = myTotalPoints,
        isMe          = true,
    )
    val opponentEntries = opponentsProgress.entries.map { (userId, progress) ->
        LeaderboardPlayer(
            name          = progress.name,
            avatarColor   = progress.avatarColor,
            avatarEmoji   = progress.avatarEmoji,
            sessionPoints = sessionPoints[userId] ?: progress.totalPoints,
            isMe          = false,
        )
    }
    val ranked = (listOf(meEntry) + opponentEntries).sortedByDescending { it.sessionPoints }

    // Voter info map: userId → (name, avatarColor, avatarEmoji)
    val voterInfo: Map<String, Triple<String, Long?, String?>> = buildMap {
        put(myUserId, Triple(myName.ifBlank { "You" }, myAvatarColor, myAvatarEmoji))
        opponentsProgress.forEach { (id, p) ->
            put(id, Triple(p.name, p.avatarColor, p.avatarEmoji))
        }
    }

    LazyColumn(
        modifier            = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Result header ─────────────────────────────────────────────────────
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text          = "Round $roundNumber",
                    color         = colors.buttonTeal,
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )

                Text(
                    text       = if (isWin) stringResource(CoreRes.string.spectator_result_you_guessed)
                    else       stringResource(CoreRes.string.result_lose_title),
                    color      = accentColor,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center,
                )
                if (targetWord.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = stringResource(CoreRes.string.result_the_word_was),
                        color     = colors.body.copy(alpha = 0.55f),
                        fontSize  = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        targetWord.forEach { letter ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text       = letter.toString(),
                                    color      = accentColor,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Session leaderboard ───────────────────────────────────────────────
        item {
            Text(
                text          = "Leaderboard",
                color         = colors.body.copy(alpha = 0.5f),
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier      = Modifier.fillMaxWidth(),
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
            ) {
                ranked.forEachIndexed { index, player ->
                    val rank = index + 1
                    val rankLabel = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "$rank." }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (player.isMe) Modifier.background(colors.buttonTeal.copy(alpha = 0.07f)) else Modifier)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text      = rankLabel,
                                fontSize  = if (rank <= 3) 20.sp else 13.sp,
                                modifier  = Modifier.width(28.dp),
                                textAlign = TextAlign.Center,
                            )
                            when {
                                player.avatarColor != null && player.avatarEmoji != null ->
                                    EmojiAvatar(color = player.avatarColor, emoji = player.avatarEmoji, size = 32)
                                player.avatarColor != null -> {
                                    val c = Color(player.avatarColor)
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(CircleShape)
                                            .background(c.copy(alpha = 0.20f))
                                            .border(1.dp, c.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) { Text(player.name.take(1).uppercase(), color = c, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                                }
                                else -> Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape)
                                        .background(colors.buttonTeal.copy(alpha = 0.15f))
                                        .border(1.dp, colors.buttonTeal.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) { Text(player.name.take(1).uppercase(), color = colors.buttonTeal, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                            }
                            Text(
                                text       = player.name,
                                color      = colors.title,
                                fontSize   = 14.sp,
                                fontWeight = if (player.isMe) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (player.isMe) {
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                        .background(colors.buttonPink.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) { Text("You", color = colors.buttonPink, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                            }
                            Text("${player.sessionPoints} pts", color = colors.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ── Play again vote section ───────────────────────────────────────────
        item {
            PlayAgainVoteSection(
                hasVotedPlayAgain = hasVotedPlayAgain,
                playAgainVotes    = playAgainVotes,
                voterInfo         = voterInfo,
                myUserId          = myUserId,
                onVotePlayAgain   = onVotePlayAgain,
            )
        }

        // ── Waiting indicator ─────────────────────────────────────────────────
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {

                Text(
                    text      = "Waiting for host to start the next round…",
                    color     = colors.body.copy(alpha = 0.45f),
                    fontSize  = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PlayAgainVoteSection(
    hasVotedPlayAgain: Boolean,
    playAgainVotes: List<String>,
    voterInfo: Map<String, Triple<String, Long?, String?>>,
    myUserId: String,
    onVotePlayAgain: () -> Unit,
) {
    // Confetti state
    val confettiFired = remember { mutableStateOf(false) }
    val confettiAngles = remember { List(10) { (0..359).random().toFloat() } }
    val confettiAnimations = remember { List(10) { Animatable(0f) } }

    LaunchedEffect(hasVotedPlayAgain) {
        if (hasVotedPlayAgain && !confettiFired.value) {
            confettiFired.value = true
            confettiAnimations.forEach { it.snapTo(0f) }
            confettiAnimations.forEach { anim ->
                launch {
                    anim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
                }
            }
        }
    }

    // Pulsing animation (only active when not voted)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.05f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.55f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Vote button / voted state
        Box(contentAlignment = Alignment.Center) {
            // Confetti canvas
            if (confettiFired.value) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    confettiAnimations.forEachIndexed { i, anim ->
                        val p = anim.value
                        val angleRad = Math.toRadians(confettiAngles[i].toDouble())
                        val radius = 90.dp.toPx() * p
                        val x = center.x + cos(angleRad).toFloat() * radius
                        val y = center.y + sin(angleRad).toFloat() * radius
                        drawCircle(
                            color  = confettiPalette[i % confettiPalette.size].copy(alpha = (1f - p).coerceIn(0f, 1f)),
                            radius = 5.dp.toPx() * (1f - p * 0.4f),
                            center = Offset(x, y),
                        )
                    }
                }
            }

            if (hasVotedPlayAgain) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colors.buttonTeal.copy(alpha = 0.15f))
                            .border(2.dp, colors.buttonTeal, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✓", color = colors.buttonTeal, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(
                        text       = "Voted to play again!",
                        color      = colors.buttonTeal,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text     = "Cancel vote",
                        color    = colors.body.copy(alpha = 0.45f),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onVotePlayAgain)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            } else {
                Button(
                    onClick  = onVotePlayAgain,
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(52.dp)
                        .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.buttonTeal.copy(alpha = glowAlpha),
                    ),
                ) {
                    Text(
                        text       = "Play Again?",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = colors.background,
                    )
                }
            }
        }

        // Voter avatars row
        if (playAgainVotes.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text     = if (playAgainVotes.size == 1) "1 player voted" else "${playAgainVotes.size} players voted",
                    color    = colors.body.copy(alpha = 0.55f),
                    fontSize = 11.sp,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    playAgainVotes.forEach { voterId ->
                        val info = voterInfo[voterId]
                        VoterBubble(
                            name        = info?.first ?: "?",
                            avatarColor = info?.second,
                            avatarEmoji = info?.third,
                        )
                    }
                }
            }
        }
    }
}
