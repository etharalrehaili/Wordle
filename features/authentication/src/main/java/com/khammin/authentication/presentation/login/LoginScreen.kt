package com.khammin.authentication.presentation.login

import UiText
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khammin.authentication.R
import com.khammin.authentication.presentation.contract.AuthEffect
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.CustomSnackbarHost
import com.khammin.core.presentation.components.DotsLoadingIndicator
import com.khammin.core.presentation.components.SnackbarState
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.FieldError
import com.khammin.core.presentation.components.text.FieldLabel
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.components.text.textFieldColors
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun LoginScreen(
    email: String,
    password: String,
    emailError: UiText?,
    passwordError: UiText?,
    isLoading: Boolean,
    uiEffect: SharedFlow<AuthEffect>,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onBack: Action,
    onLoginClick: Action,
    onNavigateToSignUp: Action,
    onNavigateToHome: Action,
    onForgotPasswordClick: Action
) {
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                AuthEffect.NavigateToHome -> onNavigateToHome()
                is AuthEffect.ShowError   -> snackbarState = SnackbarState(effect.message.resolve(context), SnackbarType.ERROR)
                else                      -> Unit
            }
        }
    }

    LoginContent(
        email              = email,
        password           = password,
        emailError         = emailError,
        passwordError      = passwordError,
        isLoading          = isLoading,
        onEmailChanged     = onEmailChanged,
        onPasswordChanged  = onPasswordChanged,
        onBack             = onBack,
        onLoginClick       = onLoginClick,
        onNavigateToSignUp = onNavigateToSignUp,
        onForgotPasswordClick = onForgotPasswordClick
    )

    if (snackbarState != null) {
        CustomSnackbarHost(
            state     = snackbarState!!,
            onDismiss = { snackbarState = null },
        )
    }
}

@Composable
fun LoginContent(
    email: String,
    password: String,
    emailError: UiText?,
    passwordError: UiText?,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onBack: Action,
    onLoginClick: Action,
    onNavigateToSignUp: Action,
    onForgotPasswordClick: Action
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val resolvedEmailError    = emailError?.resolve(context)
    val resolvedPasswordError = passwordError?.resolve(context)
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        // ── Top bar ───────────────────────────────────────────────
        GameTopBar(
            title              = stringResource(R.string.login_title),
            startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
            onStartIconClicked = onBack,
            showBackground     = false,
            containerColor     = Color.Transparent,
            modifier           = Modifier.fillMaxWidth().statusBarsPadding()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {

            // ── Email ─────────────────────────────────────────────
            FieldLabel(stringResource(R.string.login_email_label))

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value         = email,
                onValueChange = onEmailChanged,
                placeholder   = { Text(stringResource(R.string.login_email_placeholder), color = colors.body.copy(alpha = 0.35f), fontSize = 14.sp) },
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
            FieldLabel(stringResource(R.string.login_password_label))

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value                = password,
                onValueChange        = onPasswordChanged,
                placeholder          = { Text("••••••••", color = colors.body.copy(alpha = 0.35f), fontSize = 14.sp) },
                leadingIcon          = {
                    Icon(
                        Icons.Filled.Lock,
                        null,
                        tint     = if (password.isNotEmpty()) colors.buttonTeal else colors.body.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon         = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector        = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint               = colors.body.copy(alpha = 0.4f),
                            modifier           = Modifier.size(20.dp)
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

            Spacer(Modifier.height(16.dp))

            // Forgot Password
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                WordleText(
                    text       = stringResource(R.string.login_forgot_password),
                    color      = colors.buttonPink,
                    fontSize   = GameDesignTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Login button ──────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier         = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DotsLoadingIndicator()
                }
            } else {
                GameButton(
                    label           = stringResource(R.string.login_button),
                    backgroundColor = colors.buttonTeal,
                    contentColor    = colors.title,
                    showBorder      = false,
                    onClick         = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    modifier        = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // Don't have account
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                WordleText(
                    text     = stringResource(R.string.login_no_account),
                    color    = colors.body.copy(alpha = 0.5f),
                    fontSize = GameDesignTheme.typography.labelSmall,
                )
                Spacer(modifier = Modifier.width(4.dp))
                WordleText(
                    text       = stringResource(R.string.login_sign_up_link),
                    color      = colors.buttonTeal,
                    fontSize   = GameDesignTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewLoginScreenDark() {
    LoginScreen(
        email             = "ahmed@email.com",
        password          = "password",
        isLoading         = false,
        emailError        = null,
        passwordError     = null,
        uiEffect          = MutableSharedFlow(),
        onBack            = {},
        onLoginClick      = {},
        onEmailChanged    = {},
        onPasswordChanged = {},
        onNavigateToHome  = {},
        onNavigateToSignUp = {},
        onForgotPasswordClick = {}
    )
}