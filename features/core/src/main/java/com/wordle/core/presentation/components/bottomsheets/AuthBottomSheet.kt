package com.wordle.core.presentation.components.bottomsheets

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.EmojiEvents
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.buttons.GameButton
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.theme.LocalWordleColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    onDismiss: Action = {},
    onLoginWithEmail: Action = {},
    onLoginWithGoogle: Action = {},
    onSignUpClick: Action = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val colors = LocalWordleColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.surface,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Icon ────────────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colors.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint               = colors.correct,
                    modifier           = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Title ────────────────────────────────────────────────────────
            Text(
                text       = "Login to take a challenge",
                color      = colors.title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign  = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "Sign in to compete with players\naround the world",
                color     = colors.body,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            // ── With Email & Google ──────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameButton(
                    label           = "Using Email",
                    icon            = Icons.Filled.Email,
                    backgroundColor = colors.key,
                    contentColor    = colors.title,
                    onClick         = onLoginWithEmail,
                    modifier        = Modifier.weight(1f)
                )

                GameButton(
                    label           = "Using Google",
                    icon            = Icons.Filled.Email,
                    backgroundColor = colors.background,
                    contentColor    = colors.title,
                    onClick         = onLoginWithGoogle,
                    modifier        = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Sign up prompt ───────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = "Don't have an account? ",
                    color    = colors.body,
                    fontSize = 14.sp,
                )
                Text(
                    text     = "Sign up",
                    color    = colors.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onSignUpClick)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@GameDarkBackgroundPreview
@Composable
private fun PreviewAuthBottomSheetDark() {
    AuthBottomSheet(
        onDismiss         = {},
        onLoginWithEmail  = {},
        onLoginWithGoogle = {},
        onSignUpClick     = {},
    )
}