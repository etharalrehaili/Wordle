package com.khammin.core.presentation.components.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.core.R
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.theme.GameDesignTheme.colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRoomBottomSheet(
    onJoin: (String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    var roomCode by remember { mutableStateOf("") }

    // Local validation (instant, no network)
    val containsArabic = roomCode.any { it in '\u0600'..'\u06FF' }
    val isValid = roomCode.trim().length == 6 && !containsArabic

    // Combined error: local takes priority over server error
    val displayError = when {
        containsArabic -> stringResource(R.string.join_room_error_arabic)
        else           -> errorMessage  // from ViewModel (not found / already used)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.background,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(colors.buttonPink, colors.buttonTeal)
                        )
                    )
            )

            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 36.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colors.buttonTeal.copy(alpha = 0.25f),
                                    colors.buttonPink.copy(alpha = 0.10f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.MeetingRoom,
                        contentDescription = null,
                        tint               = colors.buttonTeal,
                        modifier           = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text          = stringResource(R.string.join_room_title),
                    color         = colors.title,
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    textAlign     = TextAlign.Center,
                    letterSpacing = 0.3.sp,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text       = stringResource(R.string.join_room_subtitle),
                    color      = colors.body.copy(alpha = 0.75f),
                    fontSize   = 14.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value         = roomCode,
                    onValueChange = { if (it.length <= 6) roomCode = it.uppercase() },
                    placeholder   = {
                        Text(
                            text  = "ABC123",
                            color = colors.body.copy(alpha = 0.35f),
                        )
                    },
                    singleLine    = true,
                    textStyle     = androidx.compose.ui.text.TextStyle(
                        fontSize      = 24.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 8.sp,
                        textAlign     = TextAlign.Center,
                        color         = colors.title,
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction      = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isValid) onJoin(roomCode.trim()) }
                    ),
                    colors  = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = colors.buttonTeal,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor     = colors.title,
                        unfocusedTextColor   = colors.title,
                        cursorColor          = colors.buttonTeal,
                    ),
                    shape    = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    isError  = errorMessage != null,
                )

                if (displayError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = displayError,
                        color     = colors.buttonPink,
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.buttonTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color       = colors.title,
                            modifier    = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                        )
                    }
                } else {
                    GameButton(
                        label           = stringResource(R.string.join_room_join),
                        backgroundColor = if (isValid) colors.buttonTeal else colors.border,
                        contentColor    = colors.title,
                        showBorder      = false,
                        onClick         = { if (isValid) onJoin(roomCode.trim()) },
                        modifier        = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}