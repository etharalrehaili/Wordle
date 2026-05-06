package com.khammin.core.presentation.components.bottomsheets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.PlayerAvatar
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random
import com.khammin.core.R
import com.khammin.core.presentation.components.ConfettiLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameMultiplayerResultBottomSheet(
    isWin: Boolean,
    targetWord: String,
    myName: String = "You",
    myAvatarUrl: String? = null,
    opponentName: String = "Guest",
    opponentAvatarUrl: String? = null,
    opponentLeft: Boolean = false,
    opponentFailed: Boolean = false,
    onDismiss: () -> Unit,
    onBackHome: () -> Unit = onDismiss,
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
                            onClick         = onPlayAgain,
                            modifier        = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    GameButton(
                        label = stringResource(R.string.multiplayer_result_back_home),
                        onClick         = onBackHome,
                        modifier        = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
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
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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