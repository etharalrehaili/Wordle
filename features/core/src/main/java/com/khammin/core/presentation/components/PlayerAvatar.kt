package com.khammin.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.khammin.core.presentation.theme.LocalWordleColors

@Composable
fun PlayerAvatar(
    name: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    fontSize: TextUnit = 14.sp,
) {
    val colors = LocalWordleColors.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(colors.surface)
            .border(width = 2.dp, color = colors.border, shape = CircleShape)
    ) {
        if (avatarUrl != null) {
            AsyncImage(
                model              = avatarUrl,
                contentDescription = "$name avatar",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        } else {
            val initials = name
                .split(" ")
                .filter { it.isNotEmpty() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
            Text(
                text       = initials,
                color      = colors.title,
                fontSize   = fontSize,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}