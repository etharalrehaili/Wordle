package com.wordle.core.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.presentation.components.enums.Types
import com.wordle.core.presentation.theme.LocalWordleColors
import kotlinx.coroutines.delay

sealed class SquareContent {
    data class Letter(val char: Char) : SquareContent()
    data class Label(val text: String) : SquareContent()
    data class IconContent(
        val icon: ImageVector,
        val contentDescription: String? = null
    ) : SquareContent()
    object Empty : SquareContent()
}

@Composable
fun Square(
    content: SquareContent = SquareContent.Empty,
    type: Types = Types.DEFAULT,
    isActive: Boolean = false,
    isKey: Boolean = false,
    width: Dp? = null,
    height: Dp = if (isKey) 56.dp else 62.dp,
    modifier: Modifier = Modifier
) {
    val hasContent = content !is SquareContent.Empty
    val colors = LocalWordleColors.current

    // Animated background
    val backgroundColor by animateColorAsState(
        targetValue = when (type) {
            Types.CORRECT -> colors.correct
            Types.PRESENT -> colors.present
            Types.ABSENT  -> colors.absent
            Types.DEFAULT -> if (isKey) colors.key else colors.background
        },
        animationSpec = tween(durationMillis = 300),
        label = "bgColor"
    )

    // Border
    val borderColor = when {
        isActive                                      -> colors.activeTile
        !isKey && hasContent && type == Types.DEFAULT -> colors.borderActive
        !isKey && type != Types.DEFAULT               -> Color.Transparent
        !isKey                                        -> colors.border
        else                                          -> Color.Transparent
    }
    val borderWidth = if (isActive) 3.dp else 2.dp

    // Pop-in scale animation when a letter is typed
    val letterChar = (content as? SquareContent.Letter)?.char
    var scaleTarget by remember(letterChar) { mutableStateOf(1f) }
    LaunchedEffect(letterChar) {
        if (letterChar != null) {
            scaleTarget = 1.08f
            delay(100)
            scaleTarget = 1f
        }
    }
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(100),
        label = "scale"
    )

    val shape = RoundedCornerShape(if (isKey) 8.dp else 4.dp)
    val sizeModifier = if (width != null) Modifier.size(width, height) else Modifier.size(height)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(sizeModifier)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape)
    ) {
        when (content) {
            is SquareContent.Letter -> Text(
                text = content.char.uppercaseChar().toString(),
                color = colors.title,
                fontSize = (height.value * 0.42f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp
            )

            is SquareContent.Label -> Text(
                text = content.text,
                color = colors.title,
                fontSize = (height.value * 0.24f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp
            )

            is SquareContent.IconContent -> Icon(
                imageVector = content.icon,
                contentDescription = content.contentDescription,
                tint = colors.title,
                modifier = Modifier.size(height * 0.4f)
            )

            SquareContent.Empty -> Unit
        }
    }
}

// ─── Convenience overloads ────────────────────────────────────────────────────

/** Shorthand for a letter tile (original API — fully backwards-compatible). */
@Composable
fun Square(
    letter: Char?,
    type: Types = Types.DEFAULT,
    isActive: Boolean = false,
    isKey: Boolean = false,
    size: Dp = if (isKey) 56.dp else 62.dp,
    modifier: Modifier = Modifier
) = Square(
    content = if (letter != null && letter != ' ') SquareContent.Letter(letter)
    else SquareContent.Empty,
    type = type,
    isActive = isActive,
    isKey = isKey,
    height = size,
    modifier = modifier
)

/** Shorthand for an icon key (e.g. backspace). */
@Composable
fun Square(
    icon: ImageVector,
    contentDescription: String? = null,
    type: Types = Types.DEFAULT,
    isKey: Boolean = true,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) = Square(
    content = SquareContent.IconContent(icon, contentDescription),
    type = type,
    isKey = isKey,
    height = size,
    modifier = modifier
)

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "Board – Correct")
@Composable
private fun PreviewCorrect() =
    Square(content = SquareContent.Letter('S'), type = Types.CORRECT)

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "Board – Present")
@Composable
private fun PreviewPresent() =
    Square(content = SquareContent.Letter('T'), type = Types.PRESENT)

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "Board – Absent")
@Composable
private fun PreviewAbsent() =
    Square(content = SquareContent.Letter('N'), type = Types.ABSENT)

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "Board – Active input")
@Composable
private fun PreviewActive() =
    Square(content = SquareContent.Letter('R'), type = Types.DEFAULT, isActive = true)

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "Key – Letter")
@Composable
private fun PreviewKeyLetter() =
    Square(content = SquareContent.Letter('A'), type = Types.DEFAULT, isKey = true)

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "Key – Correct")
@Composable
private fun PreviewKeyCorrect() =
    Square(content = SquareContent.Letter('S'), type = Types.CORRECT, isKey = true)

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "Key – ENTER label")
@Composable
private fun PreviewKeyEnter() =
    Square(
        content = SquareContent.Label("ENTER"),
        type = Types.DEFAULT,
        isKey = true,
        width = 88.dp,
        height = 56.dp
    )

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "Key – Backspace icon")
@Composable
private fun PreviewKeyBackspace() =
    Square(
        icon = Icons.AutoMirrored.Filled.Backspace,
        contentDescription = "Delete"
    )