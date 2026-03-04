package com.wordle.game.presentation.navigation

import com.wordle.onboarding.OnboardingScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.game.presentation.ChallengeScreen
import com.wordle.game.presentation.GameScreen
import com.wordle.game.presentation.HomeScreen
import com.wordle.game.presentation.LeaderboardScreen
import kotlinx.serialization.Serializable

fun NavGraphBuilder.navGraph(
    navController: NavHostController,
    onThemeChanged: (AppColorTheme) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    currentLanguage: () -> AppLanguage
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
            onPlayClick = { navController.navigate(Route.GameScreen) },
            onChallengeClick = { navController.navigate(Route.ChallengeScreen)},
            onLeaderboardClick = { navController.navigate(Route.LeaderboardScreen)},
            onThemeChanged = onThemeChanged,
            onLanguageChanged = onLanguageChanged
        )
    }

    composable<Route.GameScreen> {
        GameScreen(
            onClose = { navController.popBackStack() },
            currentLanguage = currentLanguage(),
        )
    }

    composable<Route.ChallengeScreen> {
        ChallengeScreen(
            onClose = { navController.popBackStack() }
        )
    }

    composable<Route.LeaderboardScreen> {
        LeaderboardScreen(
            onClose = { navController.popBackStack() }
        )
    }
}

@Serializable
sealed interface Route {
    @Serializable
    data object HomeScreen : Route

    @Serializable
    data object GameScreen : Route

    @Serializable
    data object ChallengeScreen : Route

    @Serializable
    data object LeaderboardScreen : Route

    @Serializable
    data object OnboardingScreen : Route
}

