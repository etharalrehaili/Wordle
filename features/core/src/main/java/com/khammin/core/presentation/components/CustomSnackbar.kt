package com.khammin.core.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
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


private data class SnackbarStyle(
    val iconBackground: Color,
    val icon: ImageVector,
    val progressColor: Color,
)

private fun snackbarStyleFor(type: SnackbarType) = when (type) {
    SnackbarType.SUCCESS -> SnackbarStyle(
        iconBackground = Color(0xFF4CAF50),
        icon            = Icons.Default.Check,
        progressColor  = Color(0xFF4CAF50),
    )
    SnackbarType.ERROR -> SnackbarStyle(
        iconBackground = Color(0xFFF44336),
        icon            = Icons.Default.Close,
        progressColor  = Color(0xFFF44336),
    )
    SnackbarType.WARNING -> SnackbarStyle(
        iconBackground = Color(0xFFFF9800),
        icon            = Icons.Default.Warning,
        progressColor  = Color(0xFFFF9800),
    )
}

@Composable
fun CustomSnackbar(
    message: String,
    type: SnackbarType,
    durationMillis: Int = 4_000,
    onDismiss: () -> Unit = {},
) {
    val style = snackbarStyleFor(type)

    var started by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec  = tween(durationMillis = durationMillis, easing = LinearEasing),
        finishedListener = { onDismiss() },
        label          = "snackbar_progress",
    )

    LaunchedEffect(Unit) {
        started = true
    }

    Surface(
        modifier      = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape         = RoundedCornerShape(12.dp),
        color         = Color.White,
        shadowElevation = 6.dp,
        tonalElevation  = 0.dp,
    ) {
        Column {
            // ── Content row ──────────────────────────────────────────────────
            Row(
                modifier        = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon bubble
                Box(
                    modifier        = Modifier
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

                // Message
                Text(
                    text       = message,
                    color      = Color(0xFF1A1A1A),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.weight(1f),
                )

                Spacer(Modifier.width(8.dp))

                // Dismiss button
                IconButton(
                    onClick  = onDismiss,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint               = Color(0xFF9E9E9E),
                        modifier           = Modifier.size(16.dp),
                    )
                }
            }

            // ── Progress bar ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFFE0E0E0)),
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