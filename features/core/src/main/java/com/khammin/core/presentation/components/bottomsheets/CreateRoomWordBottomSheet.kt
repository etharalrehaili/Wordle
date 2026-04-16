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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.khammin.core.R
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.theme.GameDesignTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomWordBottomSheet(
    isLoading: Boolean = false,
    loadingType: String? = null,
    onRandomWord: () -> Unit = {},
    onCustomWord: () -> Unit = {},
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val colors = GameDesignTheme.colors
    val typography = GameDesignTheme.typography

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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(colors.buttonTeal, colors.buttonPink)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // Header
                WordleText(
                    text       = stringResource(R.string.create_room_word_title),
                    color      = colors.title,
                    fontSize   = typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center,
                )

                WordleText(
                    text      = stringResource(R.string.create_room_word_subtitle),
                    color     = colors.body.copy(alpha = 0.75f),
                    fontSize  = typography.labelMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(4.dp))

                // ── Random word card ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.buttonTeal.copy(alpha = 0.1f))
                        .border(1.5.dp, colors.buttonTeal.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .clickable(enabled = !isLoading) { onRandomWord() }
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.buttonTeal.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isLoading && loadingType == "random") {
                                CircularProgressIndicator(
                                    color       = colors.buttonTeal,
                                    modifier    = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector        = Icons.Outlined.Casino,
                                    contentDescription = null,
                                    tint               = colors.buttonTeal,
                                    modifier           = Modifier.size(22.dp),
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            WordleText(
                                text       = stringResource(R.string.create_room_random_title),
                                color      = colors.title,
                                fontSize   = typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            WordleText(
                                text     = stringResource(R.string.create_room_random_subtitle),
                                color    = colors.body.copy(alpha = 0.7f),
                                fontSize = typography.labelSmall,
                            )
                        }
                    }
                }

                // Divider with "or"
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(colors.border)
                    )
                    WordleText(
                        text     = stringResource(R.string.create_room_or),
                        color    = colors.body.copy(alpha = 0.5f),
                        fontSize = typography.labelSmall,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(colors.border)
                    )
                }

                // ── Custom word card ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.buttonPink.copy(alpha = 0.1f))
                        .border(1.5.dp, colors.buttonPink.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .clickable(enabled = !isLoading) { onCustomWord() }
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.buttonPink.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isLoading && loadingType == "custom") {
                                CircularProgressIndicator(
                                    color       = colors.buttonPink,
                                    modifier    = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector        = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint               = colors.buttonPink,
                                    modifier           = Modifier.size(22.dp),
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            WordleText(
                                text       = stringResource(R.string.create_room_custom_title),
                                color      = colors.title,
                                fontSize   = typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            WordleText(
                                text     = stringResource(R.string.create_room_custom_subtitle),
                                color    = colors.body.copy(alpha = 0.7f),
                                fontSize = typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
