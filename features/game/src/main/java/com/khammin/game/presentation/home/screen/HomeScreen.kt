package com.khammin.game.presentation.home.screen

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.khammin.core.alias.Action
import com.khammin.core.alias.IntAction
import java.time.Duration
import java.time.LocalDateTime
import com.khammin.core.alias.LanguageAction
import com.khammin.core.alias.ThemeAction
import com.khammin.core.presentation.components.bottomsheets.CreateRoomWordBottomSheet
import com.khammin.core.presentation.components.bottomsheets.GameModeBottomSheet
import com.khammin.core.presentation.components.bottomsheets.JoinRoomBottomSheet
import com.khammin.core.presentation.components.bottomsheets.MultiplayerModeBottomSheet
import com.khammin.core.presentation.components.bottomsheets.NoInternetBottomSheet
import com.khammin.core.presentation.components.bottomsheets.WordLengthSelectionBottomSheet
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.buttons.GameButtonVariant
import com.khammin.core.presentation.components.enums.AppColorTheme
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import kotlinx.coroutines.delay
import com.khammin.game.R
import com.khammin.core.R as CoreRes
import com.khammin.game.presentation.preferences.contract.PreferencesIntent
import com.khammin.game.presentation.preferences.contract.PreferencesUiState
import com.khammin.game.presentation.home.contract.HomeIntent
import com.khammin.game.presentation.home.contract.HomeUiState
import com.khammin.game.presentation.home.vm.HomeViewModel
import com.khammin.game.presentation.preferences.vm.PreferencesViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onPlayClick: IntAction,
    onMultiplayerClick: (roomId: String, isHost: Boolean, userId: String, isCustomWord: Boolean, isLobbyMode: Boolean) -> Unit,
    onChallengeClick: Action,
    onLeaderboardClick: Action,
    onProfileClick: Action,
    onThemeChanged: ThemeAction,
    onLanguageChanged: LanguageAction,
    preferencesViewModel: PreferencesViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val homeUiState        by homeViewModel.uiState.collectAsStateWithLifecycle()
    val preferencesUiState by preferencesViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
            account.idToken?.let { idToken ->
                homeViewModel.signInWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            // Sign-in failed — user stays as guest
        }
        homeViewModel.markWelcomeSheetShown()
    }

    LaunchedEffect(Unit) {
        homeViewModel.prefetchWords("ar")
    }

    HomeContent(
        uiState            = preferencesUiState,
        homeUiState        = homeUiState,
        onHomeIntent       = { homeViewModel.onEvent(it) },
        onPlayClick        = onPlayClick,
        onMultiplayerClick = onMultiplayerClick,
        onCreateRoom = { customWord, callback ->
            homeViewModel.createRoom(
                language      = "ar",
                customWord    = customWord,
                onRoomCreated = { roomId, myId -> callback(roomId, myId) }
            )
        },
        onJoinRoom = { code, callback ->
            homeViewModel.joinRoom(code, preferencesUiState.selectedLanguage.code) { roomId, myId, isCustomWord, isLobbyMode ->
                callback(roomId, myId, isCustomWord, isLobbyMode)
            }
        },
        onClearJoinError        = { homeViewModel.clearJoinRoomError() },
        onChallengeClick        = onChallengeClick,
        onLeaderboardClick      = onLeaderboardClick,
        onProfileClick          = onProfileClick,
        onIntent = { intent ->
            preferencesViewModel.onEvent(intent)
            when (intent) {
                is PreferencesIntent.ChangeTheme    -> onThemeChanged(intent.theme)
                is PreferencesIntent.ChangeLanguage -> onLanguageChanged(intent.language)
            }
        },
        onSignInWithGoogle      = { googleSignInClient?.let { googleSignInLauncher.launch(it.signInIntent) } },
        onContinueAsGuest       = { homeViewModel.markWelcomeSheetShown() },
        onMarkWelcomeSheetShown = { homeViewModel.markWelcomeSheetShown() },
        onClearNoInternet       = { homeViewModel.clearNoInternetError() },
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: PreferencesUiState,
    homeUiState: HomeUiState,
    onHomeIntent: (HomeIntent) -> Unit,
    onPlayClick: (Int) -> Unit,
    onMultiplayerClick: (roomId: String, isHost: Boolean, userId: String, isCustomWord: Boolean, isLobbyMode: Boolean) -> Unit = { _, _, _, _, _ -> },
    onCreateRoom: (customWord: String?, onRoomCreated: (roomId: String, myId: String) -> Unit) -> Unit = { _, _ -> },
    onJoinRoom: (code: String, onJoined: (roomId: String, myId: String, isCustomWord: Boolean, isLobbyMode: Boolean) -> Unit) -> Unit = { _, _ -> },
    onClearJoinError: () -> Unit = {},
    onChallengeClick: Action,
    onLeaderboardClick: Action,
    onProfileClick: Action,
    onIntent: (PreferencesIntent) -> Unit,
    onSignInWithGoogle: Action = {},
    onContinueAsGuest: Action = {},
    onMarkWelcomeSheetShown: Action = {},
    onClearNoInternet: () -> Unit = {},
) {
    var countdownSeconds by remember { mutableStateOf(secondsUntilMidnight()) }
    // lastFailedAction captures navigation lambdas so it stays local (not serialisable)
    var lastFailedAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Auto-show welcome sheet on first launch
//    LaunchedEffect(showWelcomeSheet) {
//        if (showWelcomeSheet) showAuthSheet = true
//    }

    if (homeUiState.noInternetError) {
        NoInternetBottomSheet(
            onRetry = {
                onClearNoInternet()
                lastFailedAction?.invoke()
            },
            onDismiss = { onClearNoInternet() }
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            countdownSeconds = secondsUntilMidnight()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        GameTopBar(
            startIcon          = Icons.Filled.Person,
            onStartIconClicked = onProfileClick,
            modifier           = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
            containerColor     = Color.Transparent,
            showBackground     = false,
            isDarkMode         = uiState.selectedTheme == AppColorTheme.DARK,
            onThemeToggle      = { onIntent(PreferencesIntent.ChangeTheme(it)) }
        )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            ) {
                val isLandscape = maxWidth > maxHeight
                val isDark = uiState.selectedTheme == AppColorTheme.DARK

                if (isLandscape) {
                    // ── Landscape: logo left (40%) | buttons right (60%) ──
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Logo column — 40%
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Image(
                                painter            = painterResource(id = CoreRes.drawable.newlogo),
                                contentDescription = null,
                                modifier           = Modifier
                                    .fillMaxWidth(0.75f)
                                    .aspectRatio(1f)
                                    .graphicsLayer { alpha = if (isDark) 0.90f else 1f }
                            )
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                                        append(stringResource(R.string.home_title_start))
                                    }
                                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)) {
                                        append(stringResource(R.string.home_title_bold))
                                    }
                                },
                                color     = colors.title,
                                fontSize  = GameDesignTheme.typography.displayMedium,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth()
                            )
                            WordleText(
                                text          = stringResource(R.string.home_subtitle),
                                color         = colors.body,
                                fontSize      = GameDesignTheme.typography.titleMedium,
                                fontWeight    = FontWeight.Normal,
                                textAlign     = TextAlign.Center,
                                lineHeight    = 24.sp,
                                letterSpacing = 0.sp
                            )
                        }

                        // Buttons column — 60%, scrollable
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            ButtonsSection(
                                homeUiState        = homeUiState,
                                onHomeIntent       = onHomeIntent,
                                onLastFailedAction = { lastFailedAction = it },
                                countdownSeconds   = countdownSeconds,
                                onPlayClick        = onPlayClick,
                                onChallengeClick   = onChallengeClick,
                                onLeaderboardClick = onLeaderboardClick,
                                onMultiplayerClick = onMultiplayerClick,
                                onCreateRoom       = onCreateRoom,
                                onJoinRoom         = onJoinRoom,
                                onClearJoinError   = onClearJoinError,
                            )
                        }
                    }
                } else {
                    // ── Portrait: existing Column layout with verticalScroll ──
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Image(
                            painter            = painterResource(id = CoreRes.drawable.newlogo),
                            contentDescription = null,
                            modifier           = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1f)
                                .padding(bottom = 8.dp)
                                .graphicsLayer { alpha = if (isDark) 0.90f else 1f }
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                                    append(stringResource(R.string.home_title_start))
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)) {
                                    append(stringResource(R.string.home_title_bold))
                                }
                            },
                            color     = colors.title,
                            fontSize  = GameDesignTheme.typography.displayMedium,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth()
                        )
                        WordleText(
                            text          = stringResource(R.string.home_subtitle),
                            color         = colors.body,
                            fontSize      = GameDesignTheme.typography.titleMedium,
                            fontWeight    = FontWeight.Normal,
                            textAlign     = TextAlign.Center,
                            lineHeight    = 24.sp,
                            letterSpacing = 0.sp
                        )

                        ButtonsSection(
                            homeUiState        = homeUiState,
                            onHomeIntent       = onHomeIntent,
                            onLastFailedAction = { lastFailedAction = it },
                            countdownSeconds   = countdownSeconds,
                            onPlayClick        = onPlayClick,
                            onChallengeClick   = onChallengeClick,
                            onLeaderboardClick = onLeaderboardClick,
                            onMultiplayerClick = onMultiplayerClick,
                            onCreateRoom       = onCreateRoom,
                            onJoinRoom         = onJoinRoom,
                            onClearJoinError   = onClearJoinError,
                        )
                    }
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ButtonsSection(
    homeUiState: HomeUiState,
    onHomeIntent: (HomeIntent) -> Unit,
    onLastFailedAction: ((() -> Unit)?) -> Unit,
    countdownSeconds: Long,
    onPlayClick: IntAction,
    onChallengeClick: Action,
    onLeaderboardClick: Action,
    onMultiplayerClick: (roomId: String, isHost: Boolean, userId: String, isCustomWord: Boolean, isLobbyMode: Boolean) -> Unit,
    onCreateRoom: (customWord: String?, onRoomCreated: (roomId: String, myId: String) -> Unit) -> Unit,
    onJoinRoom: (code: String, onJoined: (roomId: String, myId: String, isCustomWord: Boolean, isLobbyMode: Boolean) -> Unit) -> Unit,
    onClearJoinError: () -> Unit,
) {
    Spacer(modifier = Modifier.height(GameDesignTheme.spacing.xxl))

    GameButton(
        label    = stringResource(R.string.quick_play),
        onClick  = { onHomeIntent(HomeIntent.ShowGameModeSheet(true)) },
        variant  = GameButtonVariant.Primary,
        modifier = Modifier.fillMaxWidth()
    )

    if (homeUiState.showGameModeSheet) {
        GameModeBottomSheet(
            onSinglePlayer = {
                onHomeIntent(HomeIntent.ShowGameModeSheet(false))
                onHomeIntent(HomeIntent.ShowLengthSheet(true))
            },
            onMultiplayer = {
                onHomeIntent(HomeIntent.ShowGameModeSheet(false))
                onHomeIntent(HomeIntent.ShowMultiplayerSheet(true))
            },
            onDismiss = { onHomeIntent(HomeIntent.ShowGameModeSheet(false)) }
        )
    }

    if (homeUiState.showLengthSheet) {
        WordLengthSelectionBottomSheet(
            easyWordsSolved    = homeUiState.easyWordsSolved,
            classicWordsSolved = homeUiState.classicWordsSolved,
            onLengthSelected   = { length ->
                onHomeIntent(HomeIntent.ShowLengthSheet(false))
                onPlayClick(length)
            },
            onDismiss = { onHomeIntent(HomeIntent.ShowLengthSheet(false)) }
        )
    }

    if (homeUiState.showMultiplayerSheet) {
        MultiplayerModeBottomSheet(
            isLoading    = homeUiState.createRoomLoading,
            onCreateRoom = {
                onHomeIntent(HomeIntent.ShowMultiplayerSheet(false))
                onHomeIntent(HomeIntent.ShowWordPickerSheet(true))
            },
            onJoinRoom = {
                onHomeIntent(HomeIntent.ShowMultiplayerSheet(false))
                onHomeIntent(HomeIntent.ShowJoinRoomSheet(true))
            },
            onDismiss = { onHomeIntent(HomeIntent.ShowMultiplayerSheet(false)) }
        )
    }

    if (homeUiState.showWordPickerSheet) {
        CreateRoomWordBottomSheet(
            isLoading   = homeUiState.createRoomLoading,
            loadingType = homeUiState.createRoomType,
            onRandomWord = {
                onHomeIntent(HomeIntent.SetCreateRoomType("random"))
                onLastFailedAction {
                    onCreateRoom(null) { roomId, myId ->
                        onHomeIntent(HomeIntent.ShowWordPickerSheet(false))
                        onMultiplayerClick(roomId, true, myId, false, true)
                    }
                }
                onCreateRoom(null) { roomId, myId ->
                    onHomeIntent(HomeIntent.ShowWordPickerSheet(false))
                    onMultiplayerClick(roomId, true, myId, false, true)
                }
            },
            onCustomWord = {
                onHomeIntent(HomeIntent.SetCreateRoomType("custom"))
                onLastFailedAction {
                    onCreateRoom("") { roomId, myId ->
                        onHomeIntent(HomeIntent.ShowWordPickerSheet(false))
                        onMultiplayerClick(roomId, true, myId, true, false)
                    }
                }
                onCreateRoom("") { roomId, myId ->
                    onHomeIntent(HomeIntent.ShowWordPickerSheet(false))
                    onMultiplayerClick(roomId, true, myId, true, false)
                }
            },
            onDismiss = { onHomeIntent(HomeIntent.ShowWordPickerSheet(false)) }
        )
    }

    if (homeUiState.showJoinRoomSheet) {
        JoinRoomBottomSheet(
            roomCode        = homeUiState.joinRoomCode,
            onRoomCodeChange = { onHomeIntent(HomeIntent.SetJoinRoomCode(it)) },
            onJoin = { code ->
                onLastFailedAction {
                    onJoinRoom(code) { roomId, myId, isCustomWord, isLobbyMode ->
                        onHomeIntent(HomeIntent.ShowJoinRoomSheet(false))
                        onMultiplayerClick(roomId, false, myId, isCustomWord, isLobbyMode)
                    }
                }
                onJoinRoom(code) { roomId, myId, isCustomWord, isLobbyMode ->
                    onHomeIntent(HomeIntent.ShowJoinRoomSheet(false))
                    onMultiplayerClick(roomId, false, myId, isCustomWord, isLobbyMode)
                }
            },
            onDismiss = {
                onHomeIntent(HomeIntent.ShowJoinRoomSheet(false))
                onClearJoinError()
            },
            isLoading    = homeUiState.joinRoomLoading,
            errorMessage = homeUiState.joinRoomError,
        )
    }

    Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

    GameButton(
        label    = stringResource(R.string.take_challenge),
        onClick  = onChallengeClick,
        variant  = GameButtonVariant.Muted,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

    GameButton(
        label    = stringResource(R.string.leaderboard),
        onClick  = onLeaderboardClick,
        variant  = GameButtonVariant.Ghost,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

    if (homeUiState.hasSolvedChallenge) {
        NextWordCountdownRow(countdownSeconds = countdownSeconds)
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
            tint               = colors.logoBlue,
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
