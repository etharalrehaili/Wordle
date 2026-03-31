package com.khammin.core.presentation.components.bottomsheets

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.PlayerAvatar
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random
import com.khammin.core.R

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val radius: Float,
    val speed: Float,
    val angle: Float,
    val tilt: Float,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameResultBottomSheet(
    isWin: Boolean,
    targetWord: String,
    myName: String = "You",
    myAvatarUrl: String? = null,
    opponentName: String = "Guest",
    opponentAvatarUrl: String? = null,
    opponentLeft: Boolean = false,
    opponentFailed: Boolean = false,
    onDismiss: () -> Unit,
    onPlayAgain: (() -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.background,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(if (isWin) colors.buttonTeal else colors.buttonPink)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                // Confetti layer
                if (isWin) {
                    ConfettiLayer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    )
                }

                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .padding(top = 36.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated trophy/emoji
                    AnimatedEmoji(isWin = isWin)

                    Spacer(Modifier.height(16.dp))

                    // Result title with scale-in animation
                    AnimatedResultTitle(isWin = isWin)

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = when {
                            opponentLeft   -> stringResource(R.string.multiplayer_result_opponent_left, opponentName)
                            opponentFailed -> stringResource(R.string.multiplayer_result_opponent_failed, opponentName)
                            isWin          -> stringResource(R.string.multiplayer_result_great_job)
                            else           -> stringResource(R.string.multiplayer_result_better_luck)
                        },
                        color      = colors.body.copy(alpha = 0.75f),
                        fontSize   = 14.sp,
                        textAlign  = TextAlign.Center,
                        lineHeight = 20.sp,
                    )

                    Spacer(Modifier.height(20.dp))

                    // Target word tiles with staggered animation
                    AnimatedWordTiles(
                        word  = targetWord,
                        color = if (isWin) colors.buttonTeal else colors.buttonPink
                    )

                    Spacer(Modifier.height(20.dp))

                    // Players comparison row
                    PlayersResultRow(
                        isWin             = isWin,
                        myName            = myName,
                        myAvatarUrl       = myAvatarUrl,
                        opponentName      = opponentName,
                        opponentAvatarUrl = opponentAvatarUrl,
                    )

                    Spacer(Modifier.height(28.dp))

                    if (onPlayAgain != null) {
                        GameButton(
                            label = stringResource(R.string.multiplayer_result_play_again),
                            backgroundColor = if (isWin) colors.buttonTeal else colors.buttonPink,
                            contentColor    = colors.title,
                            showBorder      = false,
                            onClick         = onPlayAgain,
                            modifier        = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    GameButton(
                        label = stringResource(R.string.multiplayer_result_back_home),
                        backgroundColor = Color.Transparent,
                        contentColor    = colors.title,
                        showBorder      = true,
                        borderColor     = colors.border,
                        onClick         = onDismiss,
                        modifier        = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ─── Confetti ─────────────────────────────────────────────────────────────────

@Composable
private fun ConfettiLayer(modifier: Modifier = Modifier) {
    val confettiColors = listOf(
        Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0),
        Color(0xFF2196F3), Color(0xFFF44336), Color(0xFF00BCD4)
    )

    val particles = remember {
        List(60) {
            ConfettiParticle(
                x      = Random.nextFloat(),
                y      = Random.nextFloat() * -1f,
                color  = confettiColors.random(),
                radius = Random.nextFloat() * 5f + 3f,
                speed  = Random.nextFloat() * 2f + 1.5f,
                angle  = Random.nextFloat() * 360f,
                tilt   = Random.nextFloat() * 10f - 5f,
            )
        }
    }

    val time by produceState(0f) {
        while (true) {
            withFrameMillis { value = it / 1000f }
        }
    }

    Box(modifier = modifier.drawBehind {
        particles.forEach { p ->
            val x     = (p.x + sin(time * p.speed * 0.3f + p.tilt) * 0.05f) * size.width
            val y     = ((p.y + time * p.speed * 0.08f) % 1.2f) * size.height
            drawCircle(
                color  = p.color.copy(alpha = 0.85f),
                radius = p.radius,
                center = Offset(x, y)
            )
        }
    })
}

// ─── Animated emoji ───────────────────────────────────────────────────────────

@Composable
private fun AnimatedEmoji(isWin: Boolean) {
    val scale by animateFloatAsState(
        targetValue  = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "emojiScale"
    )
    Text(
        text     = if (isWin) "🏆" else "😔",
        fontSize = 56.sp,
        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
    )
}

// ─── Animated title ───────────────────────────────────────────────────────────

@Composable
private fun AnimatedResultTitle(isWin: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); visible = true }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label         = "titleAlpha"
    )
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 20f,
        animationSpec = tween(400),
        label         = "titleOffset"
    )

    Text(
        text = if (isWin) stringResource(R.string.multiplayer_result_you_won)
        else stringResource(R.string.multiplayer_result_you_lost),
        color         = (if (isWin) colors.buttonTeal else colors.buttonPink).copy(alpha = alpha),
        fontSize      = 28.sp,
        fontWeight    = FontWeight.ExtraBold,
        textAlign     = TextAlign.Center,
        letterSpacing = 0.3.sp,
        modifier      = Modifier.graphicsLayer { translationY = offsetY }
    )
}

// ─── Animated word tiles ──────────────────────────────────────────────────────

@Composable
private fun AnimatedWordTiles(word: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        word.forEachIndexed { index, char ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(350L + index * 80L); visible = true }

            val rotY by animateFloatAsState(
                targetValue   = if (visible) 0f else 90f,
                animationSpec = tween(300),
                label         = "tileRot$index"
            )
            val alpha by animateFloatAsState(
                targetValue   = if (visible) 1f else 0f,
                animationSpec = tween(300),
                label         = "tileAlpha$index"
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer { rotationY = rotY; this.alpha = alpha }
                    .clip(RoundedCornerShape(8.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = char.toString(),
                    color      = Color.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

// ─── Players result row ───────────────────────────────────────────────────────

@Composable
private fun PlayersResultRow(
    isWin: Boolean,
    myName: String,
    myAvatarUrl: String?,
    opponentName: String,
    opponentAvatarUrl: String?,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(500); visible = true }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label         = "playersAlpha"
    )

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Me
        PlayerResultItem(
            name      = myName,
            avatarUrl = myAvatarUrl,
            isWinner  = isWin,
            label = if (isWin) stringResource(R.string.multiplayer_result_won_label)
            else stringResource(R.string.multiplayer_result_lost_label),
            labelColor = if (isWin) colors.buttonTeal else colors.buttonPink,
        )

        Text(
            text = stringResource(R.string.multiplayer_result_vs),
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.body.copy(alpha = 0.5f)
        )

        // Opponent
        PlayerResultItem(
            name      = opponentName,
            avatarUrl = opponentAvatarUrl,
            isWinner  = !isWin,
            label     = if (!isWin) stringResource(R.string.multiplayer_result_won_label)
            else stringResource(R.string.multiplayer_result_lost_label),
            labelColor = if (!isWin) colors.buttonTeal else colors.buttonPink,
        )
    }
}

@Composable
private fun PlayerResultItem(
    name: String,
    avatarUrl: String?,
    isWinner: Boolean,
    label: String,
    labelColor: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(colors.key)
        ) {
            PlayerAvatar(
                name      = name,
                avatarUrl = avatarUrl,
                modifier  = Modifier.fillMaxSize(),
                fontSize  = 20.sp
            )
        }

        Text(
            text       = name,
            color      = colors.title,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines   = 1,
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(labelColor.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 3.dp)
        ) {
            Text(
                text       = label,
                color      = labelColor,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}