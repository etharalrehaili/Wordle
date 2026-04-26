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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.R
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.GameDesignTheme

private const val CLASSIC_UNLOCK_THRESHOLD = 20
private const val HARD_UNLOCK_THRESHOLD    = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordLengthSelectionBottomSheet(
    easyWordsSolved: Int = 0,
    classicWordsSolved: Int = 0,
    onLengthSelected: (Int) -> Unit = {},
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val classicUnlocked = easyWordsSolved    >= CLASSIC_UNLOCK_THRESHOLD
    val hardUnlocked    = classicWordsSolved >= HARD_UNLOCK_THRESHOLD

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.background,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {

        // ── Top accent strip ──────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(brush = colors.logoStripBrush)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 16.dp),
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

                Spacer(Modifier.height(12.dp))

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
                    color    = colors.body.copy(alpha = 0.75f),
                    fontSize = GameDesignTheme.typography.labelMedium,
                )

                Spacer(Modifier.height(16.dp))

                // ── Cards ─────────────────────────────────────────────────
                data class LengthOption(
                    val length: Int,
                    val accentColor: androidx.compose.ui.graphics.Color,
                    val tagRes: Int,
                    val isUnlocked: Boolean,
                    val progressCurrent: Int,
                    val progressRequired: Int,
                    val requirementRes: Int,
                )

                val options = listOf(
                    LengthOption(
                        length          = 4,
                        accentColor     = colors.logoBlue,
                        tagRes          = R.string.word_length_easy,
                        isUnlocked      = true,
                        progressCurrent = 0,
                        progressRequired = 0,
                        requirementRes  = R.string.word_length_easy
                    ),
                    LengthOption(
                        length          = 5,
                        accentColor     = colors.logoGreen,
                        tagRes          = R.string.word_length_classic,
                        isUnlocked      = classicUnlocked,
                        progressCurrent  = easyWordsSolved.coerceAtMost(CLASSIC_UNLOCK_THRESHOLD),
                        progressRequired = CLASSIC_UNLOCK_THRESHOLD,
                        requirementRes   = R.string.word_length_classic_requirement
                    ),
                    LengthOption(
                        length          = 6,
                        accentColor     = colors.logoPink,
                        tagRes          = R.string.word_length_hard,
                        isUnlocked      = hardUnlocked,
                        progressCurrent  = classicWordsSolved.coerceAtMost(HARD_UNLOCK_THRESHOLD),
                        progressRequired = HARD_UNLOCK_THRESHOLD,
                        requirementRes   = R.string.word_length_hard_requirement
                    ),
                )

                options.forEach { option ->
                    val (length, accentColor, tag, isUnlocked, progressCurrent, progressRequired) = option

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isUnlocked) 1f else 0.85f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(accentColor.copy(alpha = 0.1f))
                            .border(
                                width = 1.5.dp,
                                color = accentColor.copy(alpha = 0.30f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .then(
                                if (!isUnlocked) {
                                    Modifier.drawBehind {
                                        drawLine(
                                            color       = accentColor.copy(alpha = 0.4f),
                                            start       = androidx.compose.ui.geometry.Offset(0f, size.height),
                                            end         = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                    }
                                } else Modifier
                            )
                            .then(
                                if (isUnlocked) Modifier.clickable { onLengthSelected(length) }
                                else Modifier
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                            // Tag + lock/arrow icon
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(accentColor.copy(alpha = 0.3f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    WordleText(
                                        text          = stringResource(tag),
                                        color         = accentColor,
                                        fontSize      = GameDesignTheme.typography.labelSmall,
                                        fontWeight    = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                    )
                                }

                                if (isUnlocked) {
                                    Icon(
                                        imageVector        = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                                        contentDescription = null,
                                        tint               = accentColor,
                                        modifier           = Modifier.size(14.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector        = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint               = accentColor,
                                        modifier           = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Letter squares preview
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                repeat(length) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
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

                                WordleText(
                                    text       = "$length",
                                    color      = accentColor,
                                    fontSize   = GameDesignTheme.typography.displayMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }

                            // Progress bar + requirement text for locked modes
                            if (!isUnlocked) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    WordleText(
                                        text       = stringResource(option.requirementRes, progressCurrent, progressRequired),
                                        color      = colors.body,
                                        fontSize   = GameDesignTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    LinearProgressIndicator(
                                        progress        = { progressCurrent.toFloat() / progressRequired },
                                        modifier        = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color           = accentColor,
                                        trackColor      = accentColor.copy(alpha = 0.2f),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
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