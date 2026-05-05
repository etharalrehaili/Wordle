package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.buttons.GameButtonVariant
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfDisconnectedBottomSheet(onGoHome: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onGoHome,
        sheetState       = sheetState,
        containerColor   = colors.background,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(brush = colors.logoStripBrush)
            )

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colors.logoBlue.copy(alpha = 0.20f),
                                    colors.logoBlue.copy(alpha = 0.08f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.WifiOff,
                        contentDescription = null,
                        tint               = colors.logoBlue,
                        modifier           = Modifier.size(36.dp)
                    )
                }

                Text(
                    text       = stringResource(R.string.self_disconnected_title),
                    color      = colors.title,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                )
                Text(
                    text      = stringResource(R.string.self_disconnected_subtitle),
                    color     = colors.body.copy(alpha = 0.6f),
                    fontSize  = 14.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                GameButton(
                    label    = stringResource(R.string.self_disconnected_go_home),
                    onClick  = onGoHome,
                    variant  = GameButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
