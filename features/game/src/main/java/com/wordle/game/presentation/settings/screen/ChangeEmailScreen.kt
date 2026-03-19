package com.wordle.game.presentation.settings.screen

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
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import com.wordle.authentication.R
import com.wordle.authentication.presentation.contract.AuthEffect
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.CustomSnackbarHost
import com.wordle.core.presentation.components.DotsLoadingIndicator
import com.wordle.core.presentation.components.SnackbarState
import com.wordle.core.presentation.components.buttons.GameButton
import com.wordle.core.presentation.components.enums.SnackbarType
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.components.text.FieldError
import com.wordle.core.presentation.components.text.FieldLabel
import com.wordle.core.presentation.components.text.textFieldColors
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.theme.GameDesignTheme.colors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ChangeEmailScreen(
    email: String,
    emailError: UiText?,
    isLoading: Boolean,
    uiEffect: SharedFlow<AuthEffect>,
    password: String,
    passwordError: UiText?,
    onPasswordChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onBack: Action,
    onChangeEmailClick: Action
) {

    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                is AuthEffect.ChangeEmailVerificationSent -> snackbarState =
                    SnackbarState(context.getString(R.string.change_email_successfully), SnackbarType.SUCCESS)
                is AuthEffect.ShowError -> snackbarState =
                    SnackbarState(effect.message.resolve(context), SnackbarType.ERROR)
                else -> Unit
            }
        }
    }

    ChangeEmailContent(
        email              = email,
        emailError         = emailError,
        isLoading          = isLoading,
        password           = password,
        passwordError      = passwordError,
        onPasswordChanged  = onPasswordChanged,
        onEmailChanged     = onEmailChanged,
        onBack             = onBack,
        onChangeEmailClick = onChangeEmailClick,
    )

    if (snackbarState != null) {
        CustomSnackbarHost(
            state     = snackbarState!!,
            onDismiss = { snackbarState = null },
        )
    }
}

@Composable
fun ChangeEmailContent(
    email: String,
    emailError: UiText?,
    isLoading: Boolean,
    password: String,
    passwordError: UiText?,
    onPasswordChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onBack: Action,
    onChangeEmailClick: Action,
) {
    val context = LocalContext.current
    val resolvedEmailError    = emailError?.resolve(context)
    val resolvedPasswordError = passwordError?.resolve(context)
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameTopBar(
                title              = stringResource(R.string.change_email_title),
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
                FieldLabel(stringResource(R.string.change_email_new_email))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = email,
                    onValueChange = onEmailChanged,
                    placeholder   = { Text(stringResource(R.string.change_email_new_email_placeholder), color = colors.body.copy(alpha = 0.35f), fontSize = 14.sp) },
                    leadingIcon   = {
                        Icon(
                            Icons.Filled.Email,
                            null,
                            tint     = if (email.isNotEmpty()) colors.buttonTeal else colors.body.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine    = true,
                    isError       = emailError != null,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = textFieldColors(colors),
                    shape         = RoundedCornerShape(16.dp),
                )
                if (resolvedEmailError != null) FieldError(resolvedEmailError)

                Spacer(Modifier.height(20.dp))

                // ── Password ──────────────────────────────────────────
                FieldLabel(stringResource(R.string.reauth_dialog_title))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value                = password,
                    onValueChange        = onPasswordChanged,
                    placeholder          = { Text(stringResource(R.string.reauth_dialog_placeholder), color = colors.body.copy(alpha = 0.35f), fontSize = 14.sp) },
                    leadingIcon          = {
                        Icon(
                            Icons.Filled.Lock,
                            null,
                            tint     = if (password.isNotEmpty()) colors.buttonTeal else colors.body.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = colors.body.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine           = true,
                    isError              = passwordError != null,
                    modifier             = Modifier.fillMaxWidth(),
                    colors               = textFieldColors(colors),
                    shape                = RoundedCornerShape(16.dp),
                )
                if (resolvedPasswordError != null) FieldError(resolvedPasswordError)

                Spacer(Modifier.height(32.dp))

                // ── Button ────────────────────────────────────────────
                if (isLoading) {
                    Box(
                        modifier         = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        DotsLoadingIndicator()
                    }
                } else {
                    GameButton(
                        label           = stringResource(R.string.change_email_button),
                        backgroundColor = colors.buttonTeal,
                        contentColor    = colors.title,
                        showBorder      = false,
                        onClick         = {
                            focusManager.clearFocus()
                            onChangeEmailClick()
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
private fun PreviewChangeEmailScreenDark() {
    ChangeEmailScreen(
        email             = "ahmed@email.com",
        isLoading         = false,
        emailError        = null,
        uiEffect          = MutableSharedFlow(),
        password          = "",
        passwordError     = null,
        onBack            = {},
        onEmailChanged    = {},
        onPasswordChanged = {},
        onChangeEmailClick = {},
    )
}