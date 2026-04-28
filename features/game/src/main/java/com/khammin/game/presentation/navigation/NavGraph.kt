package com.khammin.game.presentation.navigation

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.khammin.game.presentation.home.vm.HomeViewModel
import com.khammin.game.presentation.profile.screen.ProfileScreen
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.game.presentation.challenge.screen.ChallengesScreen
import com.khammin.game.presentation.game.screen.GameScreen
import com.khammin.game.presentation.game.screen.MultiplayerGameScreen
import com.khammin.game.presentation.home.screen.HomeScreen
import com.khammin.game.presentation.leaderboard.screen.LeaderboardScreen
import com.khammin.game.presentation.settings.screen.SettingsScreen
import com.khammin.game.presentation.settings.vm.SettingsViewModel
import com.khammin.game.presentation.profile.contract.ProfileIntent
import com.khammin.game.presentation.profile.vm.ProfileViewModel
import com.khammin.core.presentation.components.bottomsheets.LanguageSelectionBottomSheet
import com.khammin.game.presentation.preferences.contract.PreferencesIntent
import com.khammin.game.presentation.preferences.vm.PreferencesViewModel
import com.khammin.game.presentation.settings.contract.SettingsIntent
import com.khammin.game.presentation.support.screen.SupportScreen
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.navGraph(
    navController: NavHostController,
    onThemeChanged: (AppColorTheme) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    currentLanguage: () -> AppLanguage,
    currentTheme: () -> AppColorTheme
) {

    composable<Route.HomeScreen> {

        HomeScreen(
            onPlayClick = { length ->
                navController.navigate(Route.GameScreen(length, currentLanguage().code)) {
                    launchSingleTop = true
                }
            },
            onMultiplayerClick = { roomId, isHost, userId, isCustomWord, isLobbyMode ->
                navController.navigate(Route.MultiplayerGameScreen(roomId, isHost, userId, isCustomWord, isLobbyMode)) {
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
            wordLength = route.wordLength,
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
            isCustomWord    = route.isCustomWord,
            isLobbyMode     = route.isLobbyMode,
        )
    }

    composable<Route.ChallengeScreen> {
        val context = LocalContext.current
        val homeViewModel: HomeViewModel = hiltViewModel()

        val webClientId = remember {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else null
        }

        val googleSignInClient = remember(webClientId) {
            webClientId?.let { clientId ->
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build()
                GoogleSignIn.getClient(context, gso)
            }
        }

        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken -> homeViewModel.signInWithGoogle(idToken) }
            } catch (_: ApiException) { }
        }

        ChallengesScreen(
            onClose = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onSignInWithGoogle = {
                googleSignInClient?.let { googleSignInLauncher.launch(it.signInIntent) }
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
        val context = LocalContext.current
        val viewModel: ProfileViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val homeViewModel: HomeViewModel = hiltViewModel()

        val webClientId = remember {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else null
        }

        val googleSignInClient = remember(webClientId) {
            webClientId?.let { clientId ->
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build()
                GoogleSignIn.getClient(context, gso)
            }
        }

        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken -> homeViewModel.signInWithGoogle(idToken) }
            } catch (_: ApiException) { }
        }

        ProfileScreen(
            uiState            = state,
            uiEffect           = viewModel.uiEffect,
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
            onSignInWithGoogle = { googleSignInClient?.let { googleSignInLauncher.launch(it.signInIntent) } },
            onRefresh          = { viewModel.refresh() },
        )
    }

    composable<Route.SettingsScreen> {
        val viewModel: SettingsViewModel = hiltViewModel()
        val settingsUiState by viewModel.uiState.collectAsStateWithLifecycle()
        val preferencesViewModel: PreferencesViewModel = hiltViewModel()
        val preferencesUiState by preferencesViewModel.uiState.collectAsStateWithLifecycle()
        var showLanguageSheet by remember { mutableStateOf(false) }

        SettingsScreen(
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            },
            onSignOutClick  = { viewModel.onEvent(SettingsIntent.OnSignOutClick) },
            onLanguageClick = { showLanguageSheet = true },
            onSupportClick  = {
                navController.navigate(Route.SupportScreen) {
                    launchSingleTop = true
                }
            },
            isGuest          = settingsUiState.isGuest,
            uiEffect         = viewModel.uiEffect,
            onSignOutSuccess = {
                navController.navigate(Route.HomeScreen) {
                    popUpTo(0) { inclusive = true }
                }
            },
        )

        if (showLanguageSheet) {
            LanguageSelectionBottomSheet(
                selectedLanguage   = preferencesUiState.selectedLanguage,
                onLanguageSelected = { lang ->
                    preferencesViewModel.onEvent(PreferencesIntent.ChangeLanguage(lang))
                    onLanguageChanged(lang)
                },
                onDismiss = { showLanguageSheet = false },
            )
        }
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
        val userId: String = "",
        val isCustomWord: Boolean = false,
        val isLobbyMode: Boolean = false,
    ) : Route

    @Serializable
    data object ChallengeScreen : Route

    @Serializable
    data object LeaderboardScreen : Route

    @Serializable
    data object ProfileScreen : Route

    @Serializable
    data object SettingsScreen : Route

    @Serializable
    data object SupportScreen : Route
}