package com.wordle.game.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.Square
import com.wordle.core.presentation.components.SquareContent
import com.wordle.core.presentation.components.bottomsheets.AuthBottomSheet
import com.wordle.core.presentation.components.bottomsheets.GameMenuDrawerSheet
import com.wordle.core.presentation.components.bottomsheets.setAppLanguage
import com.wordle.core.presentation.components.buttons.GameButton
import com.wordle.core.presentation.components.enums.AppColorTheme
import com.wordle.core.presentation.components.enums.AppLanguage
import com.wordle.core.presentation.components.enums.Types
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.components.text.WordleText
import com.wordle.core.presentation.preview.GameDarkBackgroundPreview
import com.wordle.core.presentation.preview.GameLightBackgroundPreview
import com.wordle.core.presentation.theme.GameDesignTheme
import com.wordle.core.presentation.theme.LocalWordleColors
import kotlinx.coroutines.launch
import com.wordle.game.R
import com.wordle.game.presentation.contract.PreferencesEffect
import com.wordle.game.presentation.contract.PreferencesIntent
import com.wordle.game.presentation.contract.PreferencesUiState
import com.wordle.game.presentation.viewmodel.HomeViewModel
import com.wordle.game.presentation.viewmodel.PreferencesViewModel

@Composable
fun HomeScreen(
    onPlayClick: Action,
    onChallengeClick: Action,
    onLeaderboardClick: Action,
    onProfileClick: Action,
    onThemeChanged: (AppColorTheme) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onLoginWithEmail: Action,
    onSignUpClick: Action,
    preferencesViewModel: PreferencesViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val preferencesUiState by preferencesViewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        preferencesViewModel.uiEffect.collect { effect ->
            when (effect) {
                is PreferencesEffect.ApplyLanguage -> setAppLanguage(effect.locale)
            }
        }
    }

    HomeContent(
        uiState            = preferencesUiState,
        onPlayClick        = onPlayClick,
        onChallengeClick   = onChallengeClick,
        onLeaderboardClick = onLeaderboardClick,
        onProfileClick     = onProfileClick,
        onIntent           = preferencesViewModel::onEvent,
        onThemeChanged     = onThemeChanged,
        onLanguageChanged  = onLanguageChanged,
        onLoginWithEmail = onLoginWithEmail,
        onSignUpClick    = onSignUpClick,
        isLoggedIn         = homeUiState.isLoggedIn,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: PreferencesUiState,
    onPlayClick: Action,
    onChallengeClick: Action,
    onLeaderboardClick: Action,
    onProfileClick: Action,
    onIntent: (PreferencesIntent) -> Unit,
    onThemeChanged: (AppColorTheme) -> Unit = {},
    onLanguageChanged: (AppLanguage) -> Unit = {},
    isLoggedIn: Boolean = false,
    onLoginWithEmail: Action = {},
    onSignUpClick: Action = {},
) {
    val colors = LocalWordleColors.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAuthSheet by remember { mutableStateOf(false) }

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
                onLanguageSelected = { language ->
                    onIntent(PreferencesIntent.ChangeLanguage(language))
                    onLanguageChanged(language)
                },
                onThemeSelected    = { theme ->
                    onIntent(PreferencesIntent.ChangeTheme(theme))
                    onThemeChanged(theme)
                },
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            GameTopBar(
                startIcon          = Icons.Filled.Menu,
                onStartIconClicked = { scope.launch { drawerState.open() } },
                modifier           = Modifier.align(Alignment.TopCenter)
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
                    color         = colors.body,
                    fontSize      = GameDesignTheme.typography.labelLarge,
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    textAlign     = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.xxs))

                WordleText(
                    text          = stringResource(R.string.hurof),
                    color         = colors.title,
                    fontSize      = GameDesignTheme.typography.displayMedium,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    textAlign     = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.lg))

                val demoWord = stringResource(R.string.wordle_letters)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    demoWord.forEachIndexed { index, char ->
                        Square(
                            content = SquareContent.Letter(char),
                            type    = listOf(
                                Types.CORRECT,
                                Types.PRESENT,
                                Types.ABSENT,
                                Types.ABSENT,
                                Types.CORRECT
                            )[index],
                            height = 62.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.lg))

                WordleText(
                    text       = stringResource(R.string.home_description),
                    color      = colors.body,
                    fontSize   = GameDesignTheme.typography.labelLarge,
                    textAlign  = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.xxl))

                GameButton(
                    label           = stringResource(R.string.quick_play),
                    icon            = Icons.Filled.PlayArrow,
                    backgroundColor = colors.correct,
                    onClick         = onPlayClick,
                    modifier        = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

                GameButton(
                    label           = stringResource(R.string.take_challenge),
                    icon            = Icons.Outlined.EmojiEvents,
                    backgroundColor = colors.key,
                    contentColor    = colors.title,
                    onClick         = {
                        if (isLoggedIn) onChallengeClick()
                        else showAuthSheet = true
                    },
                    modifier        = Modifier.fillMaxWidth()
                )

                if (showAuthSheet) {
                    AuthBottomSheet(
                        onDismiss         = { showAuthSheet = false },
                        onLoginWithEmail  = {
                            showAuthSheet = false
                            onLoginWithEmail()
                        },
                        onLoginWithGoogle = {
                            showAuthSheet = false
                            // TODO: Google sign-in
                        },
                        onSignUpClick     = {
                            showAuthSheet = false
                            onSignUpClick()
                        },
                    )
                }

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

                GameButton(
                    label    = stringResource(R.string.leaderboard),
                    icon     = Icons.Filled.BarChart,
                    onClick  = onLeaderboardClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            WordleText(
                text          = stringResource(R.string.made_with_love),
                color         = colors.body.copy(alpha = 0.5f),
                fontSize      = GameDesignTheme.typography.labelSmall,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                textAlign     = TextAlign.Center,
                modifier      = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@GameLightBackgroundPreview
@Composable
private fun PreviewHomeScreenLightMode() {
    HomeContent(
        uiState            = PreferencesUiState(),
        onPlayClick        = {},
        onChallengeClick   = {},
        onLeaderboardClick = {},
        onProfileClick     = {},
        onIntent           = {}
    )
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewHomeScreenDarkMode() {
    HomeContent(
        uiState            = PreferencesUiState(),
        onPlayClick        = {},
        onChallengeClick   = {},
        onLeaderboardClick = {},
        onProfileClick     = {},
        onIntent           = {}
    )
}