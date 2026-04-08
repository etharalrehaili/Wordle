package com.khammin.game.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khammin.onboarding.OnboardingScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.khammin.game.presentation.profile.screen.ProfileScreen
import com.khammin.authentication.presentation.contract.AuthIntent
import com.khammin.authentication.presentation.login.LoginScreen
import com.khammin.authentication.presentation.resetpassword.SendEmailScreen
import com.khammin.authentication.presentation.signup.SignUpScreen
import com.khammin.authentication.presentation.vm.AuthViewModel
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.game.presentation.challenge.screen.ChallengeScreen
import com.khammin.game.presentation.game.screen.GameScreen
import com.khammin.game.presentation.game.screen.MultiplayerGameScreen
import com.khammin.game.presentation.home.screen.HomeScreen
import com.khammin.game.presentation.leaderboard.screen.LeaderboardScreen
import com.khammin.game.presentation.settings.screen.SettingsScreen
import com.khammin.game.presentation.settings.vm.SettingsViewModel
import com.khammin.game.presentation.profile.contract.ProfileIntent
import com.khammin.game.presentation.profile.vm.ProfileViewModel
import com.khammin.game.presentation.settings.contract.SettingsIntent
import com.khammin.game.presentation.settings.screen.SupportScreen
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
                navController.navigate(Route.GameScreen(length, currentLanguage().code)) {
                    launchSingleTop = true
                }
            },
            onMultiplayerClick = { roomId, isHost, userId ->
                navController.navigate(Route.MultiplayerGameScreen(roomId, isHost, userId)) {
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
        val gameLanguage = AppLanguage.entries.find { it.code == route.gameLanguage }
            ?: currentLanguage()
        GameScreen(
            onClose = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            currentLanguage = gameLanguage,
            wordLength      = route.wordLength,
        )
    }

    composable<Route.MultiplayerGameScreen> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.MultiplayerGameScreen>()
        MultiplayerGameScreen(
            onClose = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            currentLanguage = currentLanguage(),
            roomId          = route.roomId,
            isHost          = route.isHost,
            userId          = route.userId,
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

        SettingsScreen(
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
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
        val isLoggedIn = FirebaseAuth
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
    data class GameScreen(val wordLength: Int, val gameLanguage: String) : Route

    @Serializable
    data class MultiplayerGameScreen(
        val roomId: String = "",
        val isHost: Boolean = false,
        val userId: String = ""
    ) : Route

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
    data object SupportScreen : Route
}