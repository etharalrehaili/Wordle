package com.wordle.core.presentation.components.bottomsheets

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.presentation.components.buttons.GameButton
import com.wordle.core.presentation.theme.LocalWordleColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameResultsDialog(
    title: String,
    answer: String,
    accentColor: Color,
    onRestart: () -> Unit,
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val colors = LocalWordleColors.current

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
            // ── Top accent strip ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colors.buttonPink,
                                colors.buttonTeal,
                            )
                        )
                    )
            )

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 36.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Icon ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colors.buttonPink.copy(alpha = 0.25f),
                                    colors.buttonPink.copy(alpha = 0.08f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = if (accentColor == colors.correct)
                            Icons.Outlined.EmojiEvents else Icons.Outlined.SentimentDissatisfied,
                        contentDescription = null,
                        tint               = colors.buttonPink,
                        modifier           = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Title ─────────────────────────────────────────────
                Text(
                    text          = title,
                    color         = colors.title,
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    textAlign     = TextAlign.Center,
                    letterSpacing = 0.3.sp,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text      = "The word was",
                    color     = colors.body.copy(alpha = 0.45f),
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(16.dp))

                // ── Answer tiles ──────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    answer.forEach { letter ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.buttonPink.copy(alpha = 0.15f))
                                .border(
                                    width = 1.dp,
                                    color = colors.buttonPink.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Text(
                                text       = letter.toString(),
                                color      = colors.buttonPink,
                                fontSize   = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Buttons ───────────────────────────────────────────
                GameButton(
                    label           = "Play Again",
                    backgroundColor = colors.buttonTeal,
                    contentColor    = colors.title,
                    showBorder      = false,
                    onClick         = onRestart,
                    modifier        = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                GameButton(
                    label           = "Close",
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121213)
@Composable
private fun PreviewGameOverBottomSheetLost() {
    GameResultsDialog(
        title       = "Better Luck Next Time!",
        answer      = "GHOST",
        accentColor = Color(0xFFB59F3B),
        onRestart   = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121213)
@Composable
private fun PreviewGameOverBottomSheetWon() {
    GameResultsDialog(
        title       = "Brilliant! 🎉",
        answer      = "GHOST",
        accentColor = Color(0xFF538D4E),
        onRestart   = {}
    )
}