package com.wordle.authentication.presentation.login

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.core.presentation.theme.WordleColors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun LoginScreen(
    email: String,
    password: String,
    emailError: String?,
    passwordError: String?,
    isLoading: Boolean,
    uiEffect: SharedFlow<AuthEffect>,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onBack: Action,
    onLoginClick: Action,
    onNavigateToHome: Action,
) {
    val colors = LocalWordleColors.current
    var passwordVisible by remember { mutableStateOf(false) }
    var snackbarState by remember { mutableStateOf<SnackbarState?>(null) }

    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                AuthEffect.NavigateToHome ->
                    onNavigateToHome()
                is AuthEffect.ShowError ->
                    snackbarState = SnackbarState(effect.message, SnackbarType.ERROR)
                else -> Unit
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        GameTopBar(
            title              = "Login",
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

            // ── Email ─────────────────────────────────────────────────────────
            Text("Email", color = colors.body, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = email,
                onValueChange = onEmailChanged,
                placeholder   = { Text("your@email.com", color = colors.body.copy(alpha = 0.5f)) },
                leadingIcon   = { Icon(Icons.Filled.Email, null, tint = colors.body) },
                singleLine    = true,
                isError       = emailError != null,
                modifier      = Modifier.fillMaxWidth(),
                colors        = textFieldColors(colors),
                shape         = RoundedCornerShape(12.dp),
            )
            if (emailError != null) {
                Text(
                    text     = emailError,
                    color    = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Password ──────────────────────────────────────────────────────
            Text("Password", color = colors.body, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value                = password,
                onValueChange        = onPasswordChanged,
                placeholder          = { Text("••••••••", color = colors.body.copy(alpha = 0.5f)) },
                leadingIcon          = { Icon(Icons.Filled.Lock, null, tint = colors.body) },
                trailingIcon         = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = colors.body
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
                Text(
                    text     = passwordError,
                    color    = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Login Button ──────────────────────────────────────────────────
            GameButton(
                label           = "Login",
                icon            = Icons.Filled.Login,
                backgroundColor = colors.correct,
                onClick         = onLoginClick,
                modifier        = Modifier.fillMaxWidth()
            )
        }
    }

    if (snackbarState != null) {
        CustomSnackbarHost(
            state     = snackbarState!!,
            onDismiss = { snackbarState = null },
        )
    }
}

// ── TextField colors helper ───────────────────────────────────────────────────

@Composable
private fun textFieldColors(colors: WordleColors) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = colors.title,
        unfocusedBorderColor = colors.border,
        focusedTextColor     = colors.title,
        unfocusedTextColor   = colors.title,
        cursorColor          = colors.title,
    )

// ── Previews ──────────────────────────────────────────────────────────────────

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
    )
}