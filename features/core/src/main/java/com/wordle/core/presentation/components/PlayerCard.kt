package com.wordle.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.presentation.theme.LocalWordleColors

@Composable
fun PlayerCard(
    modifier: Modifier = Modifier,
    rank: Int,
    name: String,
    points: Int,
    avatarUrl: String?,
    borderColor: Color? = null,
) {

    val colors = LocalWordleColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(width = 1.dp, color = borderColor ?: colors.border, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Rank
        Text(
            text = rank.toString(),
            color = colors.body,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Avatar + Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            PlayerAvatar(
                name      = name,
                avatarUrl = avatarUrl,
                modifier  = Modifier.size(44.dp),
                fontSize  = 14.sp,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = name,
                color = colors.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Points
        Text(
            text = "%,d".format(points),
            color = colors.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0A07)
@Composable
private fun PlayerCardPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        PlayerCard(rank = 1, name = "Ahmed Al-Rashid", points = 24500, avatarUrl = null)
        Spacer(modifier = Modifier.height(6.dp))
        PlayerCard(rank = 2, name = "Sarah Jenkins", points = 11820, avatarUrl = null)
        Spacer(modifier = Modifier.height(6.dp))
        PlayerCard(rank = 3, name = "Omar Hassan", points = 9340, avatarUrl = null)
    }
}