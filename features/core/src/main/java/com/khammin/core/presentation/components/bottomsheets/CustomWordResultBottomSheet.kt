package com.khammin.core.presentation.components.bottomsheets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.R
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWordResultBottomSheet(
    opponentName: String,
    targetWord: String,
    opponentGuessedCorrectly: Boolean,
    opponentLeft: Boolean = false,
    isOwnWin: Boolean = false,
    onPlayAgain: (() -> Unit)? = null,
    playAgainVoteCount: Int = 0,
    totalGuests: Int = 0,
    onBackHome: () -> Unit,
    onDismiss: () -> Unit = onBackHome,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val accentColor = if (opponentGuessedCorrectly || isOwnWin) colors.buttonTeal else colors.buttonPink

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
            // ── Top accent strip ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accentColor)
            )

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 36.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Emoji ─────────────────────────────────────────────────────
                val emojiScale by animateFloatAsState(
                    targetValue   = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium
                    ),
                    label = "emojiScale"
                )
                Text(
                    text     = when {
                        isOwnWin || opponentGuessedCorrectly -> "🎉"
                        opponentLeft                         -> "🚪"
                        else                                 -> "🤔"
                    },
                    fontSize = 56.sp,
                    modifier = Modifier.graphicsLayer { scaleX = emojiScale; scaleY = emojiScale }
                )

                Spacer(Modifier.height(16.dp))

                // ── Title ─────────────────────────────────────────────────────
                var titleVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { kotlinx.coroutines.delay(150); titleVisible = true }
                val titleAlpha by animateFloatAsState(
                    targetValue   = if (titleVisible) 1f else 0f,
                    animationSpec = tween(400),
                    label         = "titleAlpha"
                )

                Text(
                    text = when {
                        isOwnWin                 -> stringResource(R.string.spectator_result_you_guessed)
                        opponentLeft             -> stringResource(R.string.spectator_result_left, opponentName)
                        opponentGuessedCorrectly -> stringResource(R.string.spectator_result_guessed, opponentName)
                        else                     -> stringResource(R.string.spectator_result_failed, opponentName)
                    },
                    color         = accentColor.copy(alpha = titleAlpha),
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    textAlign     = TextAlign.Center,
                    lineHeight    = 30.sp,
                    letterSpacing = 0.3.sp,
                )

                Spacer(Modifier.height(20.dp))

                // ── Word tiles ────────────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    targetWord.forEachIndexed { index, char ->
                        var tileVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(350L + index * 80L)
                            tileVisible = true
                        }
                        val rotY by animateFloatAsState(
                            targetValue   = if (tileVisible) 0f else 90f,
                            animationSpec = tween(300),
                            label         = "tileRot$index"
                        )
                        val tileAlpha by animateFloatAsState(
                            targetValue   = if (tileVisible) 1f else 0f,
                            animationSpec = tween(300),
                            label         = "tileAlpha$index"
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .graphicsLayer { rotationY = rotY; this.alpha = tileAlpha }
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = char.toString(),
                                color      = accentColor,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Vote count (host only) ────────────────────────────────────
                if (onPlayAgain != null && playAgainVoteCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.1f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                    ) {
                        Text(
                            text       = "👋  Votes for play again: $playAgainVoteCount / $totalGuests",
                            color      = accentColor,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign  = TextAlign.Center,
                        )
                    }
                }

                // ── Buttons ───────────────────────────────────────────────────
                if (onPlayAgain != null) {
                    GameButton(
                        label           = stringResource(R.string.result_play_again),
                        backgroundColor = accentColor,
                        contentColor    = Color.White,
                        showBorder      = false,
                        onClick         = onPlayAgain,
                        modifier        = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }
                GameButton(
                    label           = stringResource(R.string.spectator_result_back_home),
                    backgroundColor = if (onPlayAgain != null) colors.surface else accentColor,
                    contentColor    = if (onPlayAgain != null) colors.body else Color.White,
                    showBorder      = onPlayAgain != null,
                    onClick         = onBackHome,
                    modifier        = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
