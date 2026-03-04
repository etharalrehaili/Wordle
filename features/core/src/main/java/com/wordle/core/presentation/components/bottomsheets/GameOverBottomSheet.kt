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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.presentation.theme.LocalWordleColors

private val LetterTileColor  = Color(0xFF2196F3)
private val ShareButtonColor = Color(0xFF2A2A2C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameResultsDialog(
    title: String,
    answer: String,
    accentColor: Color,
    onRestart: () -> Unit,
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {

    val colors = LocalWordleColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.surface,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(colors.divider, RoundedCornerShape(2.dp))
            )
        },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = colors.body
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = title,
                color      = colors.title,
                fontSize   = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign  = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text          = "THE WORD WAS",
                color         = colors.body,
                fontSize      = 12.sp,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                answer.forEach { letter ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .background(LetterTileColor, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text       = letter.toString(),
                            color      = Color.White,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick  = onRestart,
                colors   = ButtonDefaults.buttonColors(containerColor = LetterTileColor),
                shape    = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text       = "PLAY AGAIN",
                    color      = Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick  = {},
                colors   = ButtonDefaults.buttonColors(containerColor = ShareButtonColor),
                shape    = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text       = "SHARE",
                    color      = Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121213)
@Composable
private fun PreviewGameOverBottomSheetLost() {
    GameResultsDialog(
        title       = "Better Luck Next Time!",
        answer      = "GHOST",
        accentColor = Color(0xFFB59F3B),
        onRestart   = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121213)
@Composable
private fun PreviewGameOverBottomSheetWon() {
    GameResultsDialog(
        title       = "Brilliant! 🎉",
        answer      = "GHOST",
        accentColor = Color(0xFF538D4E),
        onRestart   = {}
    )
}