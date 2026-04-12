package com.khammin.core.presentation.components.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.khammin.core.R
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.theme.GameDesignTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomWordBottomSheet(
    isLoading: Boolean = false,
    loadingType: String? = null,
    onRandomWord: () -> Unit = {},
    onCustomWord: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val colors = GameDesignTheme.colors
    val typography = GameDesignTheme.typography
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var customWord by remember { mutableStateOf("") }
    var error      by remember { mutableStateOf<String?>(null) }

    fun dismissKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    fun submitCustomWord() {
        when {
            customWord.length < 4 -> error = "Word must be at least 4 letters"
            customWord.length > 6 -> error = "Word must be at most 6 letters"
            !isLoading            -> onCustomWord(customWord)
        }
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
                .navigationBarsPadding()
                // intercept every touch down before children handle it — clears focus
                // without consuming the event, so child clicks still work normally
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        dismissKeyboard()
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(colors.buttonTeal, colors.buttonPink)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // Header
                WordleText(
                    text       = stringResource(R.string.create_room_word_title),
                    color      = colors.title,
                    fontSize   = typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center,
                )

                WordleText(
                    text      = stringResource(R.string.create_room_word_subtitle),
                    color     = colors.body.copy(alpha = 0.75f),
                    fontSize  = typography.labelMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(4.dp))

                // ── Random word card ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.buttonTeal.copy(alpha = 0.1f))
                        .border(1.5.dp, colors.buttonTeal.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .clickable(enabled = !isLoading) { onRandomWord() }
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.buttonTeal.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isLoading && loadingType == "random") {
                                CircularProgressIndicator(
                                    color     = colors.buttonTeal,
                                    modifier  = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector        = Icons.Outlined.Casino,
                                    contentDescription = null,
                                    tint               = colors.buttonTeal,
                                    modifier           = Modifier.size(22.dp),
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            WordleText(
                                text       = stringResource(R.string.create_room_random_title),
                                color      = colors.title,
                                fontSize   = typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            WordleText(
                                text     = stringResource(R.string.create_room_random_subtitle),
                                color    = colors.body.copy(alpha = 0.7f),
                                fontSize = typography.labelSmall,
                            )
                        }
                    }
                }

                // Divider with "or"
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(colors.border)
                    )
                    WordleText(
                        text     = stringResource(R.string.create_room_or),
                        color    = colors.body.copy(alpha = 0.5f),
                        fontSize = typography.labelSmall,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(colors.border)
                    )
                }

                // ── Custom word card ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.buttonPink.copy(alpha = 0.1f))
                        .border(1.5.dp, colors.buttonPink.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.buttonPink.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isLoading && loadingType == "custom") {
                                    CircularProgressIndicator(
                                        color       = colors.buttonPink,
                                        modifier    = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        imageVector        = Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint               = colors.buttonPink,
                                        modifier           = Modifier.size(22.dp),
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                WordleText(
                                    text       = stringResource(R.string.create_room_custom_title),
                                    color      = colors.title,
                                    fontSize   = typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                WordleText(
                                    text     = stringResource(R.string.create_room_custom_subtitle),
                                    color    = colors.body.copy(alpha = 0.7f),
                                    fontSize = typography.labelSmall,
                                )
                            }
                        }

                        OutlinedTextField(
                            value         = customWord,
                            onValueChange = { value ->
                                val filtered = value.filter { it.isLetter() }.take(6)
                                customWord = filtered.uppercase()
                                error = null
                            },
                            placeholder   = {
                                WordleText(
                                    text     = stringResource(R.string.create_room_custom_hint),
                                    color    = colors.body.copy(alpha = 0.4f),
                                    fontSize = typography.labelMedium,
                                )
                            },
                            textStyle  = TextStyle(color = colors.title),
                            singleLine = true,
                            isError    = error != null,
                            supportingText = {
                                val msg = error ?: stringResource(R.string.create_room_custom_length_hint)
                                WordleText(
                                    text     = msg,
                                    color    = if (error != null) colors.absent else colors.body.copy(alpha = 0.55f),
                                    fontSize = typography.labelSmall,
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                keyboardType   = KeyboardType.Text,
                                imeAction      = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    dismissKeyboard()
                                    submitCustomWord()
                                }
                            ),
                            colors  = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = colors.buttonPink,
                                unfocusedBorderColor    = colors.border,
                                focusedTextColor        = colors.title,
                                unfocusedTextColor      = colors.title,
                                cursorColor             = colors.buttonPink,
                                errorBorderColor        = colors.absent,
                                errorTextColor          = colors.title,
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                errorContainerColor     = Color.Transparent,
                            ),
                            shape    = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        AnimatedVisibility(
                            visible = customWord.isNotEmpty(),
                            enter   = expandVertically() + fadeIn(),
                            exit    = shrinkVertically() + fadeOut(),
                        ) {
                            GameButton(
                                label = if (isLoading && loadingType == "custom") stringResource(R.string.multiplayer_mode_creating)
                                else stringResource(R.string.create_room_custom_action),
                                backgroundColor = colors.buttonPink,
                                contentColor    = colors.title,
                                showBorder      = false,
                                onClick         = { submitCustomWord() },
                                modifier        = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
