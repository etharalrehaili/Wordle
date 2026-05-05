package com.khammin.core.presentation.components.bottomsheets

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.ConfettiLayer
import com.khammin.core.presentation.components.buttons.GameButtonVariant
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeResultBottomSheet(
    isWin: Boolean,
    targetWord: String,
    sheetState: SheetState,
    onDismiss: Action,
) {
    var countdown by remember { mutableStateOf(secondsUntilMidnight()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            countdown = secondsUntilMidnight()
        }
    }

    val resultColor = if (isWin) colors.logoGreen else colors.logoPink

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.background,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // ── Confetti overlay (win only) ───────────────────────────
            if (isWin) {
                ConfettiLayer(modifier = Modifier.matchParentSize())
            }

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Top accent strip ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(brush = colors.logoStripBrush)
                )

                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .padding(top = 36.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Icon ─────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        resultColor.copy(alpha = 0.25f),
                                        resultColor.copy(alpha = 0.08f),
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = if (isWin) Icons.Outlined.EmojiEvents
                            else Icons.Outlined.SentimentDissatisfied,
                            contentDescription = null,
                            tint               = resultColor,
                            modifier           = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Title ─────────────────────────────────────────
                    Text(
                        text          = if (isWin) stringResource(R.string.result_win_title)
                        else stringResource(R.string.result_lose_title),
                        color         = colors.title,
                        fontSize      = 22.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        textAlign     = TextAlign.Center,
                        letterSpacing = 0.3.sp,
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text      = stringResource(R.string.result_the_word_was),
                        color     = colors.body.copy(alpha = 0.45f),
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Answer tiles ──────────────────────────────────
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        targetWord.forEach { letter ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier         = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(resultColor.copy(alpha = 0.15f))
                                    .border(
                                        width = 1.dp,
                                        color = resultColor.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Text(
                                    text       = letter.toString(),
                                    color      = resultColor,
                                    fontSize   = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Countdown ─────────────────────────────────────
                    Text(
                        text      = stringResource(R.string.challenge_next_word_label),
                        color     = colors.body.copy(alpha = 0.5f),
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text       = countdown.toHhMmSs(),
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color      = colors.title,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(Modifier.height(28.dp))

                    // ── Close button ──────────────────────────────────
                    GameButton(
                        label    = stringResource(R.string.result_close),
                        onClick  = onDismiss,
                        variant  = GameButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun secondsUntilMidnight(): Long {
    val now       = LocalDateTime.now()
    val midnight  = now.toLocalDate().plusDays(1).atStartOfDay()
    return Duration.between(now, midnight).seconds
}

/** Formats a raw second count as "HH:MM:SS". */
private fun Long.toHhMmSs(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return "%02d:%02d:%02d".format(h, m, s)
}