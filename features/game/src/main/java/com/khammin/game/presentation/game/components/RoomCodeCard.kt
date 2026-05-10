package com.khammin.game.presentation.game.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.R as CoreRes
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@Composable
fun RoomCodeCard(roomId: String) {
    val context = LocalContext.current
    val shortCode = roomId.take(6).uppercase()
    val clipLabel = stringResource(CoreRes.string.room_code_copied_label)
    val toastMsg  = stringResource(CoreRes.string.room_code_copied_toast)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            stringResource(CoreRes.string.multiplayer_room_code),
            color      = colors.body.copy(alpha = 0.5f),
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager
                    clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText(clipLabel, shortCode)
                    )
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = shortCode,
                color      = colors.title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
        }

        Text(
            text     = stringResource(CoreRes.string.multiplayer_room_tap_copy),
            color    = colors.body.copy(alpha = 0.35f),
            fontSize = 9.sp,
        )
    }
}

@GameLightBackgroundPreview
@Composable
fun RoomCodeCardPreview() {
    RoomCodeCard(roomId = "abc123xyz")
}