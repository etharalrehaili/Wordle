package com.wordle.core.presentation.components.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.buttons.GameButton
import com.wordle.core.presentation.theme.LocalWordleColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignOutConfirmationBottomSheet(
    onDismiss: Action = {},
    onConfirm: Action = {},
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
                                    colors.error.copy(alpha = 0.20f),
                                    colors.error.copy(alpha = 0.05f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint               = colors.error,
                        modifier           = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Heading ───────────────────────────────────────────
                Text(
                    text          = "Sign Out",
                    color         = colors.title,
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    textAlign     = TextAlign.Center,
                    letterSpacing = 0.3.sp,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text       = "Are you sure you want to sign out?",
                    color      = colors.body.copy(alpha = 0.75f),
                    fontSize   = 14.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(32.dp))

                // ── Buttons ───────────────────────────────────────────
                GameButton(
                    label           = "Yes, Sign Out",
                    backgroundColor = colors.error,
                    contentColor    = Color.White,
                    showBorder      = false,
                    onClick         = onConfirm,
                    modifier        = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                GameButton(
                    label           = "Cancel",
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