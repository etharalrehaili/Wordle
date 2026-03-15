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
import com.wordle.authentication.presentation.contract.AuthIntent
import com.wordle.authentication.presentation.login.LoginScreen
import com.wordle.authentication.presentation.signup.SignUpScreen
import com.wordle.authentication.presentation.vm.AuthViewModel
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.game.presentation.challenge.screen.ChallengeScreen
import com.wordle.game.presentation.game.screen.GameScreen
import com.wordle.game.presentation.home.screen.HomeScreen
import com.wordle.game.presentation.leaderboard.screen.LeaderboardScreen
import com.wordle.game.presentation.profile.screen.ProfileScreen
import com.wordle.game.presentation.settings.screen.SettingsScreen
import com.wordle.game.presentation.settings.vm.SettingsViewModel
import com.wordle.game.presentation.profile.contract.ProfileIntent
import com.wordle.game.presentation.settings.contract.SettingsIntent
import com.wordle.game.presentation.profile.vm.ProfileViewModel
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
            onPlayClick = { length -> navController.navigate(Route.GameScreen(length)) },
            onChallengeClick   = { navController.navigate(Route.ChallengeScreen) },
            onLeaderboardClick = { navController.navigate(Route.LeaderboardScreen) },
            onThemeChanged     = onThemeChanged,
            onLanguageChanged  = onLanguageChanged,
            onProfileClick     = { navController.navigate(Route.ProfileScreen) },
            onLoginWithEmail   = { navController.navigate(Route.LoginScreen) },
            onSignUpClick      = { navController.navigate(Route.SignUpScreen) },
        )
    }

    composable<Route.GameScreen> { backStackEntry ->
        val route = backStackEntry.toRoute<Route.GameScreen>()
        GameScreen(
            onClose         = { navController.popBackStack() },
            currentLanguage = currentLanguage(),
            wordLength      = route.wordLength,
        )
    }

    composable<Route.ChallengeScreen> {
        ChallengeScreen(
            onClose = { navController.popBackStack() },
            currentLanguage = currentLanguage(),
        )
    }

    composable<Route.LeaderboardScreen> {
        LeaderboardScreen(
            onClose = { navController.popBackStack() }
        )
    }

    composable<Route.ProfileScreen> {
        val viewModel: ProfileViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        ProfileScreen(
            uiState            = state,
            onBack             = { navController.popBackStack() },
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
            onBack                = { navController.popBackStack() },
            onChangeEmailClick    = { viewModel.onEvent(SettingsIntent.OnChangeEmailClick) },
            onChangePasswordClick = { viewModel.onEvent(SettingsIntent.OnChangePasswordClick) },
            onSignOutClick        = { viewModel.onEvent(SettingsIntent.OnSignOutClick) },
            uiEffect              = viewModel.uiEffect,
            onSignOutSuccess      = {
                navController.navigate(Route.HomeScreen) {
                    popUpTo(0) { inclusive = true }
                }
            },
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
            onBack            = { navController.popBackStack() },
            onEmailChanged    = { viewModel.onEvent(AuthIntent.OnEmailChanged(it)) },
            onPasswordChanged = { viewModel.onEvent(AuthIntent.OnPasswordChanged(it)) },
            onLoginClick      = { viewModel.onEvent(AuthIntent.OnLoginClick) },
            onNavigateToHome  = {
                navController.navigate(Route.HomeScreen) {
                    popUpTo(Route.LoginScreen) { inclusive = true }
                }
            },
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
            onBack                   = { navController.popBackStack() },
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

}