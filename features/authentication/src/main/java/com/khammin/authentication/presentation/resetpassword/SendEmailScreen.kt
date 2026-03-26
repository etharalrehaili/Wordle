package com.khammin.authentication.presentation.resetpassword

import UiText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.sp
import com.khammin.authentication.presentation.contract.AuthEffect
import com.khammin.authentication.R
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.CustomSnackbarHost
import com.khammin.core.presentation.components.DotsLoadingIndicator
import com.khammin.core.presentation.components.SnackbarState
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.FieldError
import com.khammin.core.presentation.components.text.FieldLabel
import com.khammin.core.presentation.components.text.textFieldColors
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SendEmailScreen(
    email: String,
    emailError   : UiText?,
    isLoading: Boolean,
    uiEffect: SharedFlow<AuthEffect>,
    isEmailEditable: Boolean = true,
    onEmailChanged: (String) -> Unit,
    onBack: Action,
    onSendEmailClick: Action
) {
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                is AuthEffect.ShowError -> snackbarState =
                    SnackbarState(effect.message.resolve(context), SnackbarType.ERROR)
                AuthEffect.ResetPasswordEmailSent -> snackbarState =
                    SnackbarState(context.getString(R.string.reset_password_email_sent), SnackbarType.SUCCESS)
                else -> Unit
            }
        }
    }

    ResetPasswordContent(
        email              = email,
        emailError         = emailError,
        isLoading          = isLoading,
        onEmailChanged     = onEmailChanged,
        onBack             = onBack,
        onResetPasswordClick = onSendEmailClick,
        isEmailEditable = isEmailEditable
    )

    if (snackbarState != null) {
        CustomSnackbarHost(
            state     = snackbarState!!,
            onDismiss = { snackbarState = null },
        )
    }
}

@Composable
fun ResetPasswordContent(
    email: String,
    emailError: UiText?,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    isEmailEditable: Boolean = true,
    onBack: Action,
    onResetPasswordClick: Action,
) {
    val context = LocalContext.current
    val resolvedEmailError    = emailError?.resolve(context)
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────
            GameTopBar(
                title              = stringResource(R.string.reset_password_title),
                startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
                onStartIconClicked = onBack,
                modifier           = Modifier.fillMaxWidth(),
                containerColor     = Color.Transparent,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
            ) {

                Spacer(Modifier.height(16.dp))

                // ── Email ─────────────────────────────────────────────
                FieldLabel(stringResource(R.string.reset_password_email))

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value         = email,
                    onValueChange = { if (isEmailEditable) onEmailChanged(it) },
                    readOnly      = !isEmailEditable,
                    placeholder   = { Text(stringResource(R.string.reset_password_email_placeholder), color = colors.body.copy(alpha = 0.35f), fontSize = 14.sp) },
                    leadingIcon   = {
                        Icon(
                            Icons.Filled.Email,
                            null,
                            tint     = if (!isEmailEditable) colors.body.copy(alpha = 0.35f)
                            else if (email.isNotEmpty()) colors.buttonTeal
                            else colors.body.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine    = true,
                    isError       = emailError != null,
                    modifier      = Modifier.fillMaxWidth(),
                    colors = if (!isEmailEditable) {
                        textFieldColors(colors).copy(
                            focusedTextColor            = colors.body.copy(alpha = 0.35f),
                            unfocusedTextColor          = colors.body.copy(alpha = 0.35f),
                            focusedIndicatorColor       = colors.body.copy(alpha = 0.20f),
                            unfocusedIndicatorColor     = colors.body.copy(alpha = 0.20f),
                            focusedLeadingIconColor     = colors.body.copy(alpha = 0.35f),
                            cursorColor                 = Color.Transparent,
                        )
                    } else textFieldColors(colors),
                    shape         = RoundedCornerShape(16.dp),
                )
                if (resolvedEmailError != null) FieldError(resolvedEmailError)

                Spacer(Modifier.height(32.dp))

                // ── Send Email button ──────────────────────────────────────
                if (isLoading) {
                    Box(
                        modifier         = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        DotsLoadingIndicator()
                    }
                } else {
                    GameButton(
                        label           = stringResource(R.string.reset_password_send_email_button),
                        backgroundColor = colors.buttonTeal,
                        contentColor    = colors.title,
                        showBorder      = false,
                        onClick         = {
                            focusManager.clearFocus()
                            onResetPasswordClick()
                        },
                        modifier        = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewResetPasswordScreenDark() {
    SendEmailScreen(
        email                = "ahmed@email.com",
        isLoading            = false,
        emailError           = null,
        uiEffect             = MutableSharedFlow(),
        onBack               = {},
        onEmailChanged       = {},
        onSendEmailClick = {},
    )
}