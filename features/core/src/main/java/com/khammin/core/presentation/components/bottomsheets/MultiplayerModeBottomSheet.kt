package com.khammin.core.presentation.components.bottomsheets

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
import androidx.compose.material.icons.outlined.Groups
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.R
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerModeBottomSheet(
    onCreateRoom: Action,
    onJoinRoom: Action,
    onDismiss: Action,
    isLoading: Boolean = false,
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
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(colors.buttonPink, colors.buttonTeal)
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
                // Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colors.buttonPink.copy(alpha = 0.25f),
                                    colors.buttonTeal.copy(alpha = 0.10f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Groups,
                        contentDescription = null,
                        tint               = colors.buttonPink,
                        modifier           = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text          = stringResource(R.string.multiplayer_mode_title),
                    color         = colors.title,
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    textAlign     = TextAlign.Center,
                    letterSpacing = 0.3.sp,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text      = stringResource(R.string.multiplayer_mode_subtitle),
                    color     = colors.body.copy(alpha = 0.75f),
                    fontSize  = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(32.dp))

                GameButton(
                    label           = if (isLoading) stringResource(R.string.multiplayer_mode_creating) else stringResource(R.string.multiplayer_mode_create_room),
                    backgroundColor = if (isLoading) colors.border else colors.buttonPink,
                    contentColor    = colors.title,
                    showBorder      = false,
                    onClick         = { if (!isLoading) onCreateRoom() },
                    modifier        = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                GameButton(
                    label           = stringResource(R.string.multiplayer_mode_join_room),
                    backgroundColor = Color.Transparent,
                    contentColor    = colors.title.copy(alpha = if (isLoading) 0.4f else 1f),
                    showBorder      = true,
                    borderColor     = colors.buttonPink,
                    onClick         = { if (!isLoading) onJoinRoom() },
                    modifier        = Modifier.fillMaxWidth()
                )

            }
        }
    }
}