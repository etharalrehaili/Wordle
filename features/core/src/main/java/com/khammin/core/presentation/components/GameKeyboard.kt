package com.khammin.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.core.presentation.components.enums.Types
import com.khammin.core.presentation.theme.LocalWordleColors

// ─── English layout ───────────────────────────────────────────────────────────
private val ROW_1 = listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P')
private val ROW_2 = listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L')
private val ROW_3 = listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')

// ─── Arabic layout ────────────────────────────────────────────────────────────
private val AR_ROW_1 = listOf('ج', 'ح', 'خ', 'ه', 'ع', 'غ', 'ف', 'ق', 'ث', 'ص', 'ض')
private val AR_ROW_2 = listOf('ط', 'ك', 'م', 'ن', 'ت', 'ا', 'ل', 'ب', 'ي', 'س', 'ش')
private val AR_ROW_3 = listOf('د', 'ظ', 'ز', 'و', 'ة', 'ى', 'ر', 'ؤ', 'ء', 'ذ')


private val KEY_HEIGHT_EN = 58.dp
private val KEY_HEIGHT_AR = 44.dp
private val KEY_SHAPE = RoundedCornerShape(8.dp)

@Composable
fun GameKeyboard(
    modifier: Modifier = Modifier,
    keyStates: Map<Char, Types> = emptyMap(),
    onKey: (Char) -> Unit = {},
    onBackspace: Action,
    language: AppLanguage = AppLanguage.ENGLISH,
    enabled: Boolean = true
) {
    val isArabic = language == AppLanguage.ARABIC
    val keyHeight = if (isArabic) KEY_HEIGHT_AR else KEY_HEIGHT_EN

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .alpha(if (enabled) 1f else 0.4f)
    ) {
        // ── Row 1 ─────────────────────────────────────────────────────────────
        KeyRow {
            (if (isArabic) AR_ROW_1 else ROW_1).forEach { letter ->
                LetterKey(
                    letter = letter,
                    type = keyStates[letter] ?: Types.DEFAULT,
                    keyHeight = keyHeight,
                    onClick = { onKey(letter) })
            }
        }

        // ── Row 2 ─────────────────────────────────────────────────────────────
        KeyRow {
            (if (isArabic) AR_ROW_2 else ROW_2).forEach { letter ->
                LetterKey(
                    letter = letter,
                    type = keyStates[letter] ?: Types.DEFAULT,
                    keyHeight = keyHeight,
                    onClick = { onKey(letter) })
            }
        }

        // ── Row 3 ─────────────────────────────────────────────────────────────
        KeyRow {
            if (isArabic) {
                // Backspace first → renders on the right in RTL
                IconKey(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    weight = 1.5f,
                    keyHeight = keyHeight,
                    onClick = onBackspace
                )
                AR_ROW_3.forEach { letter ->
                    LetterKey(
                        letter = letter,
                        type = keyStates[letter] ?: Types.DEFAULT,
                        keyHeight = keyHeight,
                        onClick = { onKey(letter) })
                }
            } else {
                ROW_3.forEach { letter ->
                    LetterKey(
                        letter = letter,
                        type = keyStates[letter] ?: Types.DEFAULT,
                        keyHeight = keyHeight,
                        onClick = { onKey(letter) })
                }
                IconKey(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    weight = 1.5f,
                    keyHeight = keyHeight,
                    onClick = onBackspace
                )
            }
        }
    }
}

// ─── Row container ────────────────────────────────────────────────────────────

@Composable
private fun KeyRow(content: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) { content() }
}

// ─── Key variants ─────────────────────────────────────────────────────────────

@Composable
private fun RowScope.LetterKey(
    letter: Char,
    type: Types,
    keyHeight: Dp,
    onClick: () -> Unit
) {
    val colors = LocalWordleColors.current
    val bgColor = when (type) {
        Types.CORRECT -> colors.correct
        Types.PRESENT -> colors.present
        Types.ABSENT  -> colors.absent
        Types.DEFAULT -> colors.key
    }
    KeyContainer(bgColor = bgColor, weight = 1f, keyHeight = keyHeight, onClick = onClick) {
        Text(
            text = letter.toString(),
            color = colors.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun RowScope.LabelKey(
    label: String,
    weight: Float,
    keyHeight: Dp,
    onClick: () -> Unit
) {
    val colors = LocalWordleColors.current
    KeyContainer(bgColor = colors.key, weight = weight, keyHeight = keyHeight, onClick = onClick) {
        Text(
            text = label,
            color = colors.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun RowScope.IconKey(
    icon: ImageVector,
    contentDescription: String?,
    weight: Float,
    keyHeight: Dp,
    onClick: () -> Unit
) {
    val colors = LocalWordleColors.current
    KeyContainer(bgColor = colors.key, weight = weight, keyHeight = keyHeight, onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.title,
            modifier = Modifier.height(22.dp)
        )
    }
}

@Composable
private fun RowScope.KeyContainer(
    bgColor: Color,
    weight: Float,
    keyHeight: Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(weight)
            .height(keyHeight)
            .clip(KEY_SHAPE)
            .background(bgColor, KEY_SHAPE)
            .noRippleClickable(onClick)
    ) {
        content()
    }
}

// ─── Click helper ─────────────────────────────────────────────────────────────

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = this.clickable(
    indication = null,
    interactionSource = MutableInteractionSource(),
    onClick = onClick
)

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "GameKeyboard – English")
@Composable
private fun PreviewGameKeyboardEn() {
    GameKeyboard(
        keyStates = mapOf(
            'T' to Types.PRESENT, 'O' to Types.CORRECT,
            'S' to Types.CORRECT, 'A' to Types.ABSENT,
        ),
        onBackspace = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "GameKeyboard – Arabic")
@Composable
private fun PreviewGameKeyboardAr() {
    GameKeyboard(
        keyStates = mapOf(
            'ا' to Types.CORRECT, 'ل' to Types.PRESENT, 'م' to Types.ABSENT,
        ),
        language = AppLanguage.ARABIC,
        onBackspace = {}
    )
}