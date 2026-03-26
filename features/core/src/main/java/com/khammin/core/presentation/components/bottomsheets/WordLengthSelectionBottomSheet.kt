package com.khammin.core.presentation.components.bottomsheets

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.R
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.GameDesignTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordLengthSelectionBottomSheet(
    onLengthSelected: (Int) -> Unit = {},
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.background,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

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

            // ── Main content ──────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

            // ── Pill handle ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.divider)
            )

            Spacer(Modifier.height(24.dp))

            // ── Header ────────────────────────────────────────────────
            WordleText(
                text       = stringResource(R.string.word_length_title),
                color      = colors.title,
                fontSize   = GameDesignTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
            )

            Spacer(Modifier.height(4.dp))

            WordleText(
                text     = stringResource(R.string.word_length_subtitle),
                color    = colors.body.copy(alpha = 0.5f),
                fontSize = GameDesignTheme.typography.labelMedium,
            )

            Spacer(Modifier.height(28.dp))

            // ── Cards ─────────────────────────────────────────────────
            val options = listOf(
                Triple(4, colors.buttonTaupe,  R.string.word_length_easy),
                Triple(5, colors.buttonTeal,   R.string.word_length_classic),
                Triple(6, colors.buttonPink,   R.string.word_length_hard),
            )

            options.forEach { (length, accentColor, tag) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(accentColor.copy(alpha = 0.10f))
                        .border(
                            width = 1.5.dp,
                            color = accentColor.copy(alpha = 0.30f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onLengthSelected(length) }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        // Tag + arrow
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(accentColor.copy(alpha = 0.20f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                WordleText(
                                    text       = stringResource(tag),
                                    color      = accentColor,
                                    fontSize   = GameDesignTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                )
                            }

                            Icon(
                                imageVector        = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                                contentDescription = null,
                                tint               = accentColor.copy(alpha = 0.6f),
                                modifier           = Modifier.size(14.dp)
                            )
                        }

                        // Letter squares preview
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(length) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.18f))
                                        .border(
                                            1.dp,
                                            accentColor.copy(alpha = 0.40f),
                                            RoundedCornerShape(8.dp)
                                        )
                                )
                            }

                            Spacer(Modifier.weight(1f))

                            // Large length number
                            WordleText(
                                text       = "$length",
                                color      = accentColor.copy(alpha = 0.20f),
                                fontSize   = GameDesignTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
    }

@OptIn(ExperimentalMaterial3Api::class)
@GameLightBackgroundPreview
@Composable
fun WordLengthSelectionBottomSheetPreview() {
    WordLengthSelectionBottomSheet()
}