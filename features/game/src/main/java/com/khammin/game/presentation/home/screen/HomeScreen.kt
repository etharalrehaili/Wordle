package com.khammin.game.presentation.home.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khammin.core.alias.Action
import com.khammin.core.alias.IntAction
import java.time.Duration
import java.time.LocalDateTime
import com.khammin.core.alias.LanguageAction
import com.khammin.core.alias.ThemeAction
import com.khammin.core.presentation.components.GradientBackground
import com.khammin.core.presentation.components.Square
import com.khammin.core.presentation.components.SquareContent
import com.khammin.core.presentation.components.bottomsheets.AuthBottomSheet
import com.khammin.core.presentation.components.bottomsheets.GameMenuDrawerSheet
import com.khammin.core.presentation.components.bottomsheets.GameModeBottomSheet
import com.khammin.core.presentation.components.bottomsheets.JoinRoomBottomSheet
import com.khammin.core.presentation.components.bottomsheets.MultiplayerModeBottomSheet
import com.khammin.core.presentation.components.bottomsheets.NoInternetBottomSheet
import com.khammin.core.presentation.components.bottomsheets.WordLengthSelectionBottomSheet
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.core.presentation.components.enums.Types
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.preview.GameLightBackgroundPreview
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import kotlinx.coroutines.delay
import com.khammin.game.R
import kotlinx.coroutines.launch
import com.khammin.game.presentation.preferences.contract.PreferencesIntent
import com.khammin.game.presentation.preferences.contract.PreferencesUiState
import com.khammin.game.presentation.home.vm.HomeViewModel
import com.khammin.game.presentation.preferences.vm.PreferencesViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onPlayClick: IntAction,
    onMultiplayerClick: (roomId: String, isHost: Boolean, userId: String) -> Unit,
    onChallengeClick: Action,
    onLeaderboardClick: Action,
    onProfileClick: Action,
    onThemeChanged: ThemeAction,
    onLanguageChanged: LanguageAction,
    onLoginWithEmail: Action,
    onSignUpClick: Action,
    preferencesViewModel: PreferencesViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val homeUiState        by homeViewModel.uiState.collectAsStateWithLifecycle()
    val preferencesUiState by preferencesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(preferencesUiState.selectedLanguage) {
        homeViewModel.prefetchWords(
            if (preferencesUiState.selectedLanguage == AppLanguage.ARABIC) "ar" else "en"
        )
    }

    HomeContent(
        uiState            = preferencesUiState,
        onPlayClick        = onPlayClick,
        onMultiplayerClick = onMultiplayerClick,
        onCreateRoom = { callback ->
            homeViewModel.createRoom(
                language = if (preferencesUiState.selectedLanguage == AppLanguage.ARABIC) "ar" else "en",
                onRoomCreated = { roomId, myId ->
                    callback(roomId, myId)
                }
            )
        },
        onJoinRoom = { code, callback ->
            homeViewModel.joinRoom(code) { roomId, myId ->
                callback(roomId, myId)
            }
        },
        joinRoomLoading  = homeUiState.joinRoomLoading,
        createRoomLoading = homeUiState.createRoomLoading,
        joinRoomError    = homeUiState.joinRoomError,
        onClearJoinError = { homeViewModel.clearJoinRoomError() },
        onChallengeClick   = onChallengeClick,
        onLeaderboardClick = onLeaderboardClick,
        onProfileClick     = onProfileClick,
        onIntent = { intent ->
            preferencesViewModel.onEvent(intent)
            when (intent) {
                is PreferencesIntent.ChangeTheme    -> onThemeChanged(intent.theme)
                is PreferencesIntent.ChangeLanguage -> onLanguageChanged(intent.language)
            }
        },
        isLoggedIn         = homeUiState.isLoggedIn,
        hasSolvedChallenge = homeUiState.hasSolvedChallenge,
        onLoginWithEmail   = onLoginWithEmail,
        onSignUpClick      = onSignUpClick,
        noInternetError  = homeUiState.noInternetError,
        onClearNoInternet = { homeViewModel.clearNoInternetError() },
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: PreferencesUiState,
    onPlayClick: (Int) -> Unit,
    onMultiplayerClick: (roomId: String, isHost: Boolean, userId: String) -> Unit = { _, _, _ -> },
    onCreateRoom: (onRoomCreated: (roomId: String, myId: String) -> Unit) -> Unit = { _ -> },
    onJoinRoom: (code: String, onJoined: (roomId: String, myId: String) -> Unit) -> Unit = { _, _ -> },
    joinRoomLoading: Boolean = false,
    createRoomLoading: Boolean = false,
    joinRoomError: String? = null,
    onClearJoinError: () -> Unit = {},
    onChallengeClick: Action,
    onLeaderboardClick: Action,
    onProfileClick: Action,
    onIntent: (PreferencesIntent) -> Unit,
    isLoggedIn: Boolean = false,
    hasSolvedChallenge: Boolean = false,
    onLoginWithEmail: Action = {},
    onSignUpClick: Action = {},
    noInternetError: Boolean = false,
    onClearNoInternet: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAuthSheet         by remember { mutableStateOf(false) }
    var showLengthSheet       by remember { mutableStateOf(false) }
    var countdownSeconds      by remember { mutableStateOf(secondsUntilMidnight()) }
    var showGameModeSheet     by remember { mutableStateOf(false) }
    var showMultiplayerSheet  by remember { mutableStateOf(false) }
    var showJoinRoomSheet     by remember { mutableStateOf(false) }
    var lastFailedAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    if (noInternetError) {
        NoInternetBottomSheet(
            onRetry = {
                onClearNoInternet()
                lastFailedAction?.invoke()
            },
            onDismiss = { onClearNoInternet() }
        )
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            countdownSeconds = secondsUntilMidnight()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GameMenuDrawerSheet(
                selectedLanguage   = uiState.selectedLanguage,
                selectedTheme      = uiState.selectedTheme,
                onClose            = { scope.launch { drawerState.close() } },
                onProfile          = {
                    scope.launch { drawerState.close() }
                    onProfileClick()
                },
                isLoggedIn         = isLoggedIn,
                onLoginClick       = {
                    scope.launch { drawerState.close() }
                    onLoginWithEmail()
                },
                onLanguageSelected = { onIntent(PreferencesIntent.ChangeLanguage(it)) },
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {

            GradientBackground()

            GameTopBar(
                startIcon          = Icons.Filled.Menu,
                onStartIconClicked = { scope.launch { drawerState.open() } },
                modifier           = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
                containerColor     = Color.Transparent,
                showBackground = false,
                isDarkMode         = uiState.selectedTheme == AppColorTheme.DARK,
                onThemeToggle      = { onIntent(PreferencesIntent.ChangeTheme(it)) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                WordleText(
                    text          = stringResource(R.string.welcome_to),
                    color         = colors.pinkText,
                    fontSize      = GameDesignTheme.typography.titleLarge,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign     = TextAlign.Center
                )

                WordleText(
                    text          = stringResource(R.string.Khammin),
                    color         = colors.title,
                    fontSize      = GameDesignTheme.typography.displayMedium,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    textAlign     = TextAlign.Center
                )

                WordleText(
                    text       = stringResource(R.string.home_description),
                    color      = colors.body,
                    fontSize   = GameDesignTheme.typography.titleMedium,
                    textAlign  = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.xxl))

                GameButton(
                    label           = stringResource(R.string.quick_play),
                    backgroundBrush = colors.buttonPinkBrush,
                    contentColor    = colors.title,
                    showBorder      = false,
                    onClick         = { showGameModeSheet = true },
                    modifier        = Modifier.fillMaxWidth()
                )

                if (showGameModeSheet) {
                    GameModeBottomSheet(
                        onSinglePlayer = {
                            showGameModeSheet = false
                            showLengthSheet   = true
                        },
                        onMultiplayer = {
                            showGameModeSheet    = false
                            showMultiplayerSheet = true
                        },
                        onDismiss = { showGameModeSheet = false }
                    )
                }

                if (showLengthSheet) {
                    WordLengthSelectionBottomSheet(
                        onLengthSelected = { length ->
                            showLengthSheet = false
                            onPlayClick(length)
                        },
                        onDismiss = { showLengthSheet = false }
                    )
                }

                if (showMultiplayerSheet) {
                    MultiplayerModeBottomSheet(
                        isLoading    = createRoomLoading,
                        onCreateRoom = {
                            showMultiplayerSheet = false
                            lastFailedAction = {
                                onCreateRoom { roomId, myId ->
                                    onMultiplayerClick(roomId, true, myId)
                                }
                            }
                            onCreateRoom { roomId, myId ->
                                onMultiplayerClick(roomId, true, myId)
                            }
                        },
                        onJoinRoom = {
                            showMultiplayerSheet = false
                            showJoinRoomSheet    = true
                        },
                        onDismiss      = { showMultiplayerSheet = false }
                    )
                }

                if (showJoinRoomSheet) {
                    JoinRoomBottomSheet(
                        onJoin = { code ->
                            lastFailedAction = {
                                onJoinRoom(code) { roomId, myId ->
                                    showJoinRoomSheet = false
                                    onMultiplayerClick(roomId, false, myId)
                                }
                            }
                            onJoinRoom(code) { roomId, myId ->
                                showJoinRoomSheet = false
                                onMultiplayerClick(roomId, false, myId)
                            }
                        },
                        onDismiss    = {
                            showJoinRoomSheet = false
                            onClearJoinError()
                        },
                        isLoading    = joinRoomLoading,
                        errorMessage = joinRoomError,
                    )
                }

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

                GameButton(
                    label           = stringResource(R.string.take_challenge),
                    backgroundBrush = colors.buttonTealBrush,
                    contentColor    = colors.title,
                    showBorder      = false,
                    onClick         = {
                        if (isLoggedIn) onChallengeClick()
                        else showAuthSheet = true
                    },
                    modifier        = Modifier.fillMaxWidth()
                )

                if (showAuthSheet) {
                    AuthBottomSheet(
                        onDismiss        = { showAuthSheet = false },
                        onLoginWithEmail = {
                            showAuthSheet = false
                            onLoginWithEmail()
                        },
                        onSignUpClick    = {
                            showAuthSheet = false
                            onSignUpClick()
                        },
                    )
                }

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

                GameButton(
                    label           = stringResource(R.string.leaderboard),
                    backgroundBrush = colors.buttonTaupeBrush,
                    contentColor    = colors.title,
                    showBorder      = false,
                    onClick         = onLeaderboardClick,
                    modifier        = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

                if (isLoggedIn && hasSolvedChallenge) {
                    NextWordCountdownRow(countdownSeconds = countdownSeconds)
                }
            }
        }
    }
}

@Composable
private fun NextWordCountdownRow(countdownSeconds: Long) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.countdownBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = Icons.Outlined.Schedule,
            contentDescription = null,
            tint               = colors.buttonTeal,
            modifier           = Modifier.size(24.dp)
        )
        WordleText(
            text       = stringResource(R.string.countdown_next_word),
            color      = colors.body,
            fontSize   = GameDesignTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            WordleText(
                text       = countdownSeconds.toHhMmSs(),
                color      = colors.body,
                fontSize   = GameDesignTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun secondsUntilMidnight(): Long {
    val now      = LocalDateTime.now()
    val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
    return Duration.between(now, midnight).seconds
}

private fun Long.toHhMmSs(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return "%02d : %02d : %02d".format(h, m, s)
}

@RequiresApi(Build.VERSION_CODES.O)
@GameLightBackgroundPreview
@Composable
private fun PreviewHomeScreenLightMode() {
    HomeContent(
        uiState            = PreferencesUiState(),
        onPlayClick        = {},
        onMultiplayerClick = { _, _, _ -> },
        onChallengeClick   = {},
        onLeaderboardClick = {},
        onProfileClick     = {},
        onIntent           = {}
    )
}