package com.wordle.game.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordle.onboarding.OnboardingScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.wordle.game.presentation.profile.screen.ProfileScreen
import com.wordle.game.presentation.profile.vm.ProfileViewModel
import com.wordle.authentication.presentation.contract.AuthIntent
import com.wordle.authentication.presentation.login.LoginScreen
import com.wordle.authentication.presentation.resetpassword.SendEmailScreen
import com.wordle.authentication.presentation.signup.SignUpScreen
import com.wordle.authentication.presentation.vm.AuthViewModel
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.game.presentation.challenge.screen.ChallengeScreen
import com.wordle.game.presentation.game.screen.GameScreen
import com.wordle.game.presentation.home.screen.HomeScreen
import com.wordle.game.presentation.leaderboard.screen.LeaderboardScreen
import com.wordle.game.presentation.settings.screen.SettingsScreen
import com.wordle.game.presentation.settings.vm.SettingsViewModel
import com.wordle.game.presentation.profile.contract.ProfileIntent
import com.wordle.game.presentation.settings.contract.SettingsIntent
import com.wordle.game.presentation.settings.screen.ChangeEmailScreen
import com.wordle.game.presentation.settings.screen.SupportScreen
import kotlinx.serialization.Serializable

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.navGraph(
    navController: NavHostController,
    onThemeChanged: (AppColorTheme) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    currentLanguage: () -> AppLanguage,
    currentTheme: () -> AppColorTheme
) {

    composable<Route.OnboardingScreen> {
        OnboardingScreen(
            onNavigateToHome = {
                navController.navigate(Route.HomeScreen) {
                    popUpTo(Route.OnboardingScreen) { inclusive = true }
                }
            }
        )
    }

    composable<Route.HomeScreen> {

        HomeScreen(
            onPlayClick = { length ->
                navController.navigate(Route.GameScreen(length)) {
                    launchSingleTop = true
                }
            },
            onChallengeClick = {
                navController.navigate(Route.ChallengeScreen) {
                    launchSingleTop = true
                }
            },
            onLeaderboardClick = {
                navController.navigate(Route.LeaderboardScreen) {
                    launchSingleTop = true
                }
            },
            onProfileClick = {
                navController.navigate(Route.ProfileScreen) {
                    launchSingleTop = true
                }
            },
            onLoginWithEmail = {
                navController.navigate(Route.LoginScreen) {
                    launchSingleTop = true
                }
            },
            onSignUpClick = {
                navController.navigate(Route.SignUpScreen) {
                    launchSingleTop = true
                }
            },
            onThemeChanged    = onThemeChanged,
            onLanguageChanged = onLanguageChanged,
        )
    }
    composable<Route.GameScreen> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.GameScreen>()
        GameScreen(
            onClose = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            currentLanguage = currentLanguage(),
            wordLength      = route.wordLength,
        )
    }

    composable<Route.ChallengeScreen> {
        ChallengeScreen(
            onClose = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            currentLanguage = currentLanguage(),
        )
    }

    composable<Route.LeaderboardScreen> {
        LeaderboardScreen(
            onClose = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            currentLanguage  = currentLanguage(),
        )
    }

    composable<Route.ProfileScreen> {
        val viewModel: ProfileViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        ProfileScreen(
            uiState            = state,
            uiEffect = viewModel.uiEffect,
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onEditProfileClick = { viewModel.onEvent(ProfileIntent.OnEditProfileClick) },
            onSaveProfileClick = { viewModel.onEvent(ProfileIntent.OnSaveProfileClick) },
            onCancelEditClick  = { viewModel.onEvent(ProfileIntent.OnCancelEditClick) },
            onNameChanged      = { viewModel.onEvent(ProfileIntent.OnNameChanged(it)) },
            onAvatarChanged    = { viewModel.onEvent(ProfileIntent.OnAvatarChanged(it)) },
            onSettingsClick    = { navController.navigate(Route.SettingsScreen) },
        )
    }

    composable<Route.SettingsScreen> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        SettingsScreen(
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onChangeEmailClick    = { navController.navigate(Route.ChangeEmailScreen) },
            onChangePasswordClick = { navController.navigate(Route.ResetPasswordScreen) },
            onSupportClick = { navController.navigate(Route.SupportScreen) },
            onSignOutClick        = { viewModel.onEvent(SettingsIntent.OnSignOutClick) },
            uiEffect              = viewModel.uiEffect,
            onSignOutSuccess      = {
                navController.navigate(Route.HomeScreen) {
                    popUpTo(0) { inclusive = true }
                }
            },
        )
    }

    composable<Route.SupportScreen> {
        SupportScreen(
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        )
    }

    composable<Route.LoginScreen> {
        val viewModel: AuthViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LoginScreen(
            email             = state.email,
            password          = state.password,
            emailError        = state.emailError,
            passwordError     = state.passwordError,
            isLoading         = state.isLoading,
            uiEffect          = viewModel.uiEffect,
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onEmailChanged    = { viewModel.onEvent(AuthIntent.OnEmailChanged(it)) },
            onPasswordChanged = { viewModel.onEvent(AuthIntent.OnPasswordChanged(it)) },
            onLoginClick      = { viewModel.onEvent(AuthIntent.OnLoginClick) },
            onNavigateToHome  = {
                navController.navigate(Route.HomeScreen) {
                    popUpTo(Route.LoginScreen) { inclusive = true }
                }
            },
            onNavigateToSignUp= {
                navController.navigate(Route.SignUpScreen) {
                    popUpTo(Route.LoginScreen) { inclusive = true }
                }
            },
            onForgotPasswordClick = { navController.navigate(Route.ResetPasswordScreen) },
        )
    }

    composable<Route.ResetPasswordScreen> {
        val viewModel: AuthViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        // Determine if coming from settings (user is logged in)
        val isLoggedIn = com.google.firebase.auth.FirebaseAuth
            .getInstance().currentUser != null

        SendEmailScreen(
            email            = state.email,
            emailError       = state.emailError,
            isLoading        = state.isLoading,
            uiEffect         = viewModel.uiEffect,
            isEmailEditable  = !isLoggedIn,
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onEmailChanged   = { viewModel.onEvent(AuthIntent.OnEmailChanged(it)) },
            onSendEmailClick = { viewModel.onEvent(AuthIntent.OnSendEmailClicked) },
        )
    }

    composable<Route.ChangeEmailScreen> {
        val viewModel: AuthViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        ChangeEmailScreen(
            email             = state.newEmail,
            emailError        = state.newEmailError,
            isLoading         = state.isLoading,
            uiEffect          = viewModel.uiEffect,
            password          = state.reAuthPassword,
            passwordError     = state.reAuthPasswordError,
            onEmailChanged    = { viewModel.onEvent(AuthIntent.OnNewEmailChanged(it)) },
            onPasswordChanged = { viewModel.onEvent(AuthIntent.OnReAuthPasswordChanged(it)) },
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onChangeEmailClick = { viewModel.onEvent(AuthIntent.OnChangeEmailClicked) },
        )
    }

    composable<Route.SignUpScreen> {
        val viewModel: AuthViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        SignUpScreen(
            email                    = state.email,
            password                 = state.password,
            confirmPassword          = state.confirmPassword,
            isLoading                = state.isLoading,
            emailError               = state.emailError,
            passwordError            = state.passwordError,
            confirmPasswordError     = state.confirmPasswordError,
            uiEffect                 = viewModel.uiEffect,
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onEmailChanged           = { viewModel.onEvent(AuthIntent.OnEmailChanged(it)) },
            onPasswordChanged        = { viewModel.onEvent(AuthIntent.OnPasswordChanged(it)) },
            onConfirmPasswordChanged = { viewModel.onEvent(AuthIntent.OnConfirmPasswordChanged(it)) },
            onSignUpClick            = { viewModel.onEvent(AuthIntent.OnSignUpClick) },
            onNavigateToLogin        = {
                navController.navigate(Route.LoginScreen) {
                    popUpTo(Route.SignUpScreen) { inclusive = true }
                }
            },
        )
    }

}

@Serializable
sealed interface Route {
    @Serializable
    data object HomeScreen : Route

    @Serializable
    data class GameScreen(val wordLength: Int) : Route

    @Serializable
    data object ChallengeScreen : Route

    @Serializable
    data object LeaderboardScreen : Route

    @Serializable
    data object OnboardingScreen : Route

    @Serializable
    data object ProfileScreen : Route

    @Serializable
    data object SettingsScreen : Route

    @Serializable
    data object LoginScreen : Route

    @Serializable
    data object SignUpScreen : Route

    @Serializable
    data object ResetPasswordScreen : Route

    @Serializable
    data object ChangeEmailScreen : Route

    @Serializable
    data object SupportScreen : Route
}