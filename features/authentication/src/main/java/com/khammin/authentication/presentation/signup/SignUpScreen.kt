package com.khammin.authentication.presentation.signup

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.khammin.core.presentation.components.buttons.GameButtonVariant
import com.khammin.core.presentation.components.enums.SnackbarType
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.components.text.textFieldColors
import com.khammin.core.presentation.preview.GameDarkBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SignUpScreen(
    email: String,
    password: String,
    confirmPassword: String,
    isLoading: Boolean,
    emailError: UiText?,
    passwordError: UiText?,
    confirmPasswordError: UiText?,
    uiEffect: SharedFlow<AuthEffect>,
    onBack: Action,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSignUpClick: Action,
    onNavigateToLogin: Action
) {
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    var navigateAfterSnackbar by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                AuthEffect.SignUpSuccess -> {
                    snackbarState = SnackbarState(
                        context.getString(R.string.signup_success),
                        SnackbarType.SUCCESS
                    )
                    navigateAfterSnackbar = true
                }
                is AuthEffect.ShowError ->
                    snackbarState = SnackbarState(effect.message.resolve(context), SnackbarType.ERROR)
                else -> Unit
            }
        }
    }

    SignUpContent(
        email                    = email,
        password                 = password,
        confirmPassword          = confirmPassword,
        isLoading                = isLoading,
        emailError               = emailError,
        passwordError            = passwordError,
        confirmPasswordError     = confirmPasswordError,
        onBack                   = onBack,
        onEmailChanged           = onEmailChanged,
        onPasswordChanged        = onPasswordChanged,
        onConfirmPasswordChanged = onConfirmPasswordChanged,
        onSignUpClick            = onSignUpClick,
        onNavigateToLogin        = onNavigateToLogin,
    )

    if (snackbarState != null) {
        CustomSnackbarHost(
            state     = snackbarState!!,
            onDismiss = {
                snackbarState = null
                if (navigateAfterSnackbar) {
                    navigateAfterSnackbar = false
                    onNavigateToLogin()
                }
            },
        )
    }
}

@Composable
fun SignUpContent(
    email: String,
    password: String,
    confirmPassword: String,
    isLoading: Boolean,
    emailError: UiText?,
    passwordError: UiText?,
    confirmPasswordError: UiText?,
    onBack: Action,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSignUpClick: Action,
    onNavigateToLogin: Action
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val resolvedEmailError           = emailError?.resolve(context)
    val resolvedPasswordError        = passwordError?.resolve(context)
    val resolvedConfirmPasswordError = confirmPasswordError?.resolve(context)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            GameTopBar(
                title              = stringResource(R.string.signup_title),
                startIcon          = Icons.AutoMirrored.Filled.ArrowBack,
                onStartIconClicked = onBack,
                showBackground     = false,
                modifier           = Modifier.fillMaxWidth().statusBarsPadding(),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {

                // ── Email ─────────────────────────────────────────────────────
                Text(stringResource(R.string.signup_email_label), color = colors.body, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value         = email,
                    onValueChange = onEmailChanged,
                    placeholder   = { Text((stringResource(R.string.signup_email_placeholder)), color = colors.body.copy(alpha = 0.5f)) },
                    leadingIcon   = {
                        Icon(
                            Icons.Filled.Email,
                            null,
                            tint     = if (email.isNotEmpty()) colors.logoBlue else colors.body.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine    = true,
                    isError       = emailError != null,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = textFieldColors(colors),
                    shape         = RoundedCornerShape(12.dp),
                )
                if (resolvedEmailError != null) {
                    Text(
                        text     = resolvedEmailError,
                        color    = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Password ──────────────────────────────────────────────────
                Text(stringResource(R.string.signup_password_label), color = colors.body, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value                = password,
                    onValueChange        = onPasswordChanged,
                    placeholder          = { Text("••••••••", color = colors.body.copy(alpha = 0.5f)) },
                    leadingIcon          = {
                        Icon(
                            Icons.Filled.Lock,
                            null,
                            tint     = if (password.isNotEmpty()) colors.logoBlue else colors.body.copy(alpha = 0.4f),
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
                    shape                = RoundedCornerShape(12.dp),
                )
                if (resolvedPasswordError != null) {
                    Text(
                        text     = resolvedPasswordError,
                        color    = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Confirm Password ──────────────────────────────────────────
                Text(stringResource(R.string.signup_confirm_password_label), color = colors.body, fontSize = 13.sp, fontWeight = FontWeight.Medium)

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value                = confirmPassword,
                    onValueChange        = onConfirmPasswordChanged,
                    placeholder          = { Text("••••••••", color = colors.body.copy(alpha = 0.5f)) },
                    leadingIcon          = {
                        Icon(
                            Icons.Filled.Lock,
                            null,
                            tint     = if (confirmPassword.isNotEmpty()) colors.logoBlue else colors.body.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon         = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector        = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint               = colors.body.copy(alpha = 0.4f),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine           = true,
                    isError              = confirmPasswordError != null,
                    modifier             = Modifier.fillMaxWidth(),
                    colors               = textFieldColors(colors),
                    shape                = RoundedCornerShape(12.dp),
                )
                if (resolvedConfirmPasswordError != null) {
                    Text(
                        text     = resolvedConfirmPasswordError,
                        color    = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ── SignUp button ──────────────────────────────────────
                if (isLoading) {
                    Box(
                        modifier         = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        DotsLoadingIndicator()
                    }
                } else {
                    GameButton(
                        label           = stringResource(R.string.signup_button),
                        onClick         = onSignUpClick,
                        variant  = GameButtonVariant.Primary,
                        modifier        = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    WordleText(
                        text     = stringResource(R.string.signup_already_have_account),
                        color    = colors.body.copy(alpha = 0.5f),
                        fontSize = GameDesignTheme.typography.labelSmall,
                    )
                    Spacer(Modifier.width(4.dp))
                    WordleText(
                        text       = stringResource(R.string.signup_login_link),
                        color      = colors.logoBlue,
                        fontSize   = GameDesignTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@GameDarkBackgroundPreview
@Composable
private fun PreviewSignUpScreenDark() {
    SignUpScreen(
        email                    = "",
        password                 = "",
        confirmPassword          = "",
        isLoading                = false,
        emailError               = null,
        passwordError            = null,
        confirmPasswordError     = null,
        uiEffect                 = MutableSharedFlow(),
        onBack                   = {},
        onEmailChanged           = {},
        onPasswordChanged        = {},
        onConfirmPasswordChanged = {},
        onSignUpClick            = {},
        onNavigateToLogin        = {},
    )
}