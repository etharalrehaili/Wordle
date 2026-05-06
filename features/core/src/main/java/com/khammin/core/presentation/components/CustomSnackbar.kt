package com.khammin.core.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors

private data class SnackbarStyle(
    val iconBackground: Color,
    val icon: ImageVector,
    val progressColor: Color,
)

@Composable
fun CustomSnackbar(
    message: String,
    type: SnackbarType,
    durationMillis: Int = 4_000,
    onDismiss: () -> Unit = {},
) {

    val style = when (type) {
        SnackbarType.SUCCESS -> SnackbarStyle(
            iconBackground = colors.snackbarSuccess,
            icon           = Icons.Default.Check,
            progressColor  = colors.snackbarSuccess,
        )
        SnackbarType.ERROR -> SnackbarStyle(
            iconBackground = colors.snackbarError,
            icon           = Icons.Default.Close,
            progressColor  = colors.snackbarError,
        )
        SnackbarType.WARNING -> SnackbarStyle(
            iconBackground = colors.snackbarWarning,
            icon           = Icons.Default.Warning,
            progressColor  = colors.snackbarWarning,
        )
    }

    var started by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue      = if (started) 1f else 0f,
        animationSpec    = tween(durationMillis = durationMillis, easing = LinearEasing),
        finishedListener = { onDismiss() },
        label            = "snackbar_progress",
    )

    LaunchedEffect(Unit) { started = true }

    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape           = RoundedCornerShape(12.dp),
        color           = colors.surface,
        shadowElevation = 6.dp,
        tonalElevation  = 0.dp,
    ) {
        Column {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(style.iconBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = style.icon,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(20.dp),
                    )
                }

                Spacer(Modifier.width(14.dp))

                Text(
                    text       = message,
                    color      = colors.title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.weight(1f),
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick  = onDismiss,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint               = colors.body,
                        modifier           = Modifier.size(16.dp),
                    )
                }
            }

            // ── Progress bar ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(colors.divider),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(style.progressColor),
                )
            }
        }
    }
}

data class SnackbarState(val message: String, val type: SnackbarType)

@Composable
fun CustomSnackbarHost(
    state: SnackbarState,
    durationMillis: Int = 2_000,
    onDismiss: () -> Unit,
) {
    Box(
        modifier        = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 48.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        CustomSnackbar(
            message        = state.message,
            type           = state.type,
            durationMillis = durationMillis,
            onDismiss      = onDismiss,
        )
    }
}

@GameDarkBackgroundPreview
@Composable
private fun CustomSnackbarPreview() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CustomSnackbar(
            message = "Successfully submitted",
            type    = SnackbarType.SUCCESS,
        )
        CustomSnackbar(
            message = "Please fix the error !",
            type    = SnackbarType.ERROR,
        )
        CustomSnackbar(
            message = "Invalid input, check again",
            type    = SnackbarType.WARNING,
        )
    }
}