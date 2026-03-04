package com.wordle.core.presentation.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.vector.ImageVector
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.core.presentation.components.enums.Types
import com.wordle.core.presentation.theme.LocalWordleColors

private val ROW_1 = listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P')
private val ROW_2 = listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L')
private val ROW_3 = listOf('Z', 'X', 'C', 'V', 'B', 'N', 'M')

private val AR_ROW_1 = listOf('ض', 'ص', 'ث', 'ق', 'ف', 'غ', 'ع', 'ه', 'خ', 'ح')
private val AR_ROW_2 = listOf('ش', 'س', 'ي', 'ب', 'ل', 'ا', 'ت', 'ن', 'م')
private val AR_ROW_3 = listOf('ئ', 'ء', 'ؤ', 'ر', 'و', 'ز', 'ظ', 'ط', 'د')

private val KEY_HEIGHT = 58.dp
private val KEY_SHAPE  = RoundedCornerShape(8.dp)

@Composable
fun GameKeyboard(
    keyStates: Map<Char, Types> = emptyMap(),
    onKey: (Char) -> Unit = {},
    onEnter: Action,
    onBackspace: Action,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {

    val colors = LocalWordleColors.current

    val row1 = if (language == AppLanguage.ARABIC) AR_ROW_1 else ROW_1
    val row2 = if (language == AppLanguage.ARABIC) AR_ROW_2 else ROW_2
    val row3 = if (language == AppLanguage.ARABIC) AR_ROW_3 else ROW_3

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
//            .background(colors.background)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        // ── Row 1: Q W E R T Y U I O P ───────────────────────────────────────
        KeyRow {
            row1.forEach { letter ->
                LetterKey(
                    letter = letter,
                    type = keyStates[letter] ?: Types.DEFAULT,
                    onClick = { onKey(letter) }
                )
            }
        }

        // ── Row 2: A S D F G H J K L ─────────────────────────────────────────
        KeyRow {
            row2.forEach { letter ->
                LetterKey(
                    letter = letter,
                    type = keyStates[letter] ?: Types.DEFAULT,
                    onClick = { onKey(letter) }
                )
            }
        }

        // ── Row 3: ENTER  Z X C V B N M  ⌫ ────────────────────────────────────
        KeyRow {
            // ENTER — 1.5× wide
            LabelKey(
                label = "ENTER",
                weight = 1.5f,
                onClick = onEnter
            )

            row3.forEach { letter ->
                LetterKey(
                    letter = letter,
                    type = keyStates[letter] ?: Types.DEFAULT,
                    onClick = { onKey(letter) }
                )
            }

            // Backspace — 1.5× wide
            IconKey(
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                weight = 1.5f,
                onClick = onBackspace
            )
        }
    }
}

// ─── Row container ────────────────────────────────────────────────────────────

@Composable
private fun KeyRow(content: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()  // ← fills screen width; weight() divides this
    ) { content() }
}

// ─── Key variants ─────────────────────────────────────────────────────────────

/** Standard letter key — takes equal weight in the row. */
@Composable
private fun RowScope.LetterKey(
    letter: Char,
    type: Types,
    onClick: () -> Unit
) {

    val colors = LocalWordleColors.current

    val bgColor = when (type) {
        Types.CORRECT -> colors.correct
        Types.PRESENT -> colors.present
        Types.ABSENT  -> colors.absent
        Types.DEFAULT -> colors.key
    }

    KeyContainer(
        bgColor = bgColor,
        weight = 1f,
        onClick = onClick
    ) {
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

/** Text label key (ENTER). */
@Composable
private fun RowScope.LabelKey(
    label: String,
    weight: Float,
    onClick: () -> Unit
) {

    val colors = LocalWordleColors.current

    KeyContainer(
        bgColor = colors.key,
        weight = weight,
        onClick = onClick
    ) {
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
    onClick: () -> Unit
) {

    val colors = LocalWordleColors.current

    KeyContainer(
        bgColor = colors.key,
        weight = weight,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.title,
            modifier = Modifier
                .height(22.dp)         // fixed icon height; width scales naturally
        )
    }
}

/** Shared Box that every key variant renders inside. */
@Composable
private fun RowScope.KeyContainer(
    bgColor: Color,
    weight: Float,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(weight)            // ← distributes width proportionally
            .height(KEY_HEIGHT)
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

@Preview(showBackground = true, backgroundColor = 0xFF121213, name = "GameKeyboard")
@Composable
private fun PreviewGameKeyboard() {
    GameKeyboard(
        keyStates = mapOf(
            'T' to Types.PRESENT,
            'O' to Types.CORRECT,
            'S' to Types.CORRECT,
            'A' to Types.ABSENT,
            'D' to Types.ABSENT,
            'N' to Types.ABSENT,
            'L' to Types.ABSENT,
        ),
        onEnter = {},
        onBackspace = {}
    )
}