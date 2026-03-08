package com.wordle.core.presentation.components.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.presentation.theme.LocalWordleColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordleInfoBottomSheet(
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {

    val colors = LocalWordleColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = null,
        shape = RoundedCornerShape(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Text(
                text = "How To Play",
                color = colors.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            )

            HorizontalDivider(color = colors.divider, thickness = 1.dp)

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Rules ─────────────────────────────────────────────────────
                Text(
                    text = "Guess the WORDLE in 6 tries.",
                    color = colors.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                RuleItem("Each guess must be a valid 5-letter word.")
                RuleItem("The color of the tiles will change to show how close your guess was.")

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = colors.divider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Examples",
                    color = colors.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                // ── Example 1: CORRECT ────────────────────────────────────────
                ExampleRow(
                    letters   = listOf('W', 'E', 'A', 'R', 'Y'),
                    highlight = 0,
                    color     = colors.correct
                )
                HighlightCaption(
                    letter = 'W',
                    description = " is in the word and in the correct spot."
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Example 2: PRESENT ────────────────────────────────────────
                ExampleRow(
                    letters   = listOf('P', 'I', 'L', 'L', 'S'),
                    highlight = 1,
                    color     = colors.present
                )
                HighlightCaption(
                    letter = 'I',
                    description = " is in the word but in the wrong spot."
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Example 3: ABSENT ─────────────────────────────────────────
                ExampleRow(
                    letters   = listOf('V', 'A', 'G', 'U', 'E'),
                    highlight = 3,
                    color     = colors.absent
                )
                HighlightCaption(
                    letter = 'U',
                    description = " is not in the word in any spot."
                )

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = colors.divider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "A new WORDLE will be available each day!",
                    color = colors.body,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ─── Private helpers ──────────────────────────────────────────────────────────

@Composable
private fun RuleItem(text: String) {

    val colors = LocalWordleColors.current

    Row(verticalAlignment = Alignment.Top) {
        Text(text = "•", color = colors.body, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp, top = 1.dp))
        Text(text = text, color = colors.body, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun ExampleRow(
    letters: List<Char>,
    highlight: Int,
    color: Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        letters.forEachIndexed { index, letter ->
            val isHighlighted = index == highlight
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = if (isHighlighted) color else Color(0xFF1A2535),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = letter.toString(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HighlightCaption(letter: Char, description: String) {

    val colors = LocalWordleColors.current

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.title)) {
                append(letter.toString())
            }
            withStyle(SpanStyle(color = colors.body)) {
                append(description)
            }
        },
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121213)
@Composable
private fun PreviewWordleInfoBottomSheet() {
    WordleInfoBottomSheet()
}