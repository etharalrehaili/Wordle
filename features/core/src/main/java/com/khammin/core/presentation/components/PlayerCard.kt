package com.khammin.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@Composable
fun PlayerCard(
    modifier: Modifier = Modifier,
    rank: Int,
    name: String,
    points: Int,
    avatarUrl: String?,
    borderColor: Color? = null,
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.buttonTaupe.copy(alpha = 0.25f))
            .border(
                width = 1.dp,
                color = borderColor ?: colors.border,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Rank badge ────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background((borderColor ?: colors.border).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format(java.util.Locale.US, "%d", rank),
                color      = borderColor ?: colors.body,
                fontSize   = 13.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ── Avatar + Name ─────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.weight(1f)
        ) {
            PlayerAvatar(
                name      = name,
                avatarUrl = avatarUrl,
                modifier  = Modifier.size(40.dp),
                fontSize  = 13.sp,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text       = name,
                color      = colors.title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
        }

        // ── Points pill ───────────────────────────────────────────
        Box(
            modifier         = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background((borderColor ?: colors.buttonTeal).copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "%,d".format(points),
                color      = borderColor ?: colors.buttonTeal,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
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