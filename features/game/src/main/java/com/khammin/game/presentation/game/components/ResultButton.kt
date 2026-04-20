package com.khammin.game.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.R as CoreRes

@Composable
fun ResultButton(isWin: Boolean, onClick: () -> Unit) {
    val accent = if (isWin) colors.buttonTeal else colors.buttonPink
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = "🏆", fontSize = 22.sp)
            Text(
                text       = stringResource(CoreRes.string.multiplayer_result_title),
                color      = accent,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@GameLightBackgroundPreview
@Composable
fun ResultButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ResultButton(isWin = true, onClick = {})
        ResultButton(isWin = false, onClick = {})
    }
}