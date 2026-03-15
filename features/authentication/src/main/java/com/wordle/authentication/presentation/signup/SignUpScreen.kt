package com.wordle.authentication.presentation.signup

import androidx.compose.runtime.Composable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.authentication.presentation.contract.AuthEffect
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.CustomSnackbarHost
import com.wordle.core.presentation.components.SnackbarState
import com.wordle.core.presentation.components.SnackbarType
import com.wordle.core.presentation.components.buttons.GameButton
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.preview.GameLightBackgroundPreview
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.core.presentation.theme.WordleColors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SignUpScreen(
    email: String,
    password: String,
    confirmPassword: String,
    isLoading: Boolean,
    emailError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    uiEffect: SharedFlow<AuthEffect>,
    onBack: Action,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onSignUpClick: Action,
    onNavigateToLogin: Action,
) {
    val colors = LocalWordleColors.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }
    var navigateAfterSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                AuthEffect.SignUpSuccess -> {
                    snackbarState = SnackbarState("Account created successfully", SnackbarType.SUCCESS)
                    navigateAfterSnackbar = true
                }
                is AuthEffect.ShowError ->
                    snackbarState = SnackbarState(effect.message, SnackbarType.ERROR)
                else -> Unit
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            GameTopBar(
                title              = "Create Account",
                startIcon          = Icons.Filled.ArrowBack,
                onStartIconClicked = onBack,
                modifier           = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {

                // ── Email ─────────────────────────────────────────────────────
                Text("Email", color = colors.body, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = email,
                    onValueChange = onEmailChanged,
                    placeholder   = { Text("your@email.com", color = colors.body.copy(alpha = 0.5f)) },
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
                    shape         = RoundedCornerShape(12.dp),
                )
                if (emailError != null) {
                    Text(emailError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(Modifier.height(20.dp))

                // ── Password ──────────────────────────────────────────────────
                Text("Password", color = colors.body, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value                = password,
                    onValueChange        = onPasswordChanged,
                    placeholder          = { Text("••••••••", color = colors.body.copy(alpha = 0.5f)) },
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
                    shape                = RoundedCornerShape(12.dp),
                )
                if (passwordError != null) {
                    Text(passwordError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(Modifier.height(20.dp))

                // ── Confirm Password ──────────────────────────────────────────
                Text("Confirm Password", color = colors.body, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value                = confirmPassword,
                    onValueChange        = onConfirmPasswordChanged,
                    placeholder          = { Text("••••••••", color = colors.body.copy(alpha = 0.5f)) },
                    leadingIcon          = {
                        Icon(
                            Icons.Filled.Lock,
                            null,
                            tint     = if (confirmPassword.isNotEmpty()) colors.buttonTeal else colors.body.copy(alpha = 0.4f),
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
                if (confirmPasswordError != null) {
                    Text(confirmPasswordError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(Modifier.height(32.dp))

                // ── SignUp button ──────────────────────────────────────
                if (isLoading) {
                    Box(
                        modifier          = Modifier.fillMaxWidth(),
                        contentAlignment  = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color       = colors.buttonTeal,
                            strokeWidth = 2.dp,
                            modifier    = Modifier.size(36.dp)
                        )
                    }
                } else {
                    GameButton(
                        label           = "Sign Up",
                        backgroundColor = colors.buttonTeal,
                        contentColor    = colors.title,
                        showBorder      = false,
                        onClick         = onSignUpClick,
                        modifier        = Modifier.fillMaxWidth()
                    )
                }
            }
        }

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
}

// ── TextField colors helper ───────────────────────────────────────────────────

@Composable
private fun textFieldColors(colors: WordleColors) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = colors.buttonTeal,
        unfocusedBorderColor = colors.border,
        focusedTextColor     = colors.title,
        unfocusedTextColor   = colors.title,
        cursorColor          = colors.buttonTeal,
        errorBorderColor     = MaterialTheme.colorScheme.error,
    )

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