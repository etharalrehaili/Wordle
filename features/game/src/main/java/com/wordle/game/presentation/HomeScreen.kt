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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.Square
import com.wordle.core.presentation.components.SquareContent
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPlayClick: Action,
    onChallengeClick: Action,
    onLeaderboardClick: Action,
    onThemeChanged: (AppColorTheme) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit
) {

    val colors = LocalWordleColors.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
    var selectedTheme by remember { mutableStateOf(AppColorTheme.DARK) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GameMenuDrawerSheet(
                selectedLanguage = selectedLanguage,
                onClose = { scope.launch { drawerState.close() } },
                onLanguageSelected = { language ->
                    selectedLanguage = language
                    onLanguageChanged(language)
                    when (language) {
                        AppLanguage.ENGLISH -> setAppLanguage("en")
                        AppLanguage.ARABIC  -> setAppLanguage("ar")
                    }
                },
                selectedTheme = selectedTheme,
                onThemeSelected = { theme ->
                    selectedTheme = theme
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
                startIcon = Icons.Filled.Menu,
                onStartIconClicked = {
                    scope.launch { drawerState.open() }
                },
                modifier = Modifier.align(Alignment.TopCenter)
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
                    text = stringResource(R.string.welcome_to),
                    color = colors.body,
                    fontSize = GameDesignTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.xxs))

                WordleText(
                    text = "HUROF",
                    color = colors.title,
                    fontSize = GameDesignTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.lg))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Square(
                        content = SquareContent.Letter('W'),
                        type = Types.CORRECT,
                        height = 62.dp
                    )
                    Square(
                        content = SquareContent.Letter('O'),
                        type = Types.PRESENT,
                        height = 62.dp
                    )
                    Square(
                        content = SquareContent.Letter('R'),
                        type = Types.ABSENT,
                        height = 62.dp
                    )
                    Square(
                        content = SquareContent.Letter('D'),
                        type = Types.ABSENT,
                        height = 62.dp
                    )
                    Square(
                        content = SquareContent.Letter('S'),
                        type = Types.CORRECT,
                        height = 62.dp
                    )
                }

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.lg))

                WordleText(
                    text = "Get a chance to guess the hidden word",
                    color = colors.body,
                    fontSize = GameDesignTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.xxl))

                GameButton(
                    label = stringResource(R.string.quick_play),
                    icon = Icons.Filled.PlayArrow,
                    backgroundColor = colors.correct,
                    onClick = onPlayClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

                GameButton(
                    label = stringResource(R.string.take_challenge),
                    icon = Icons.Outlined.EmojiEvents,
                    backgroundColor = colors.key,
                    contentColor = colors.title,
                    onClick = onChallengeClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(GameDesignTheme.spacing.sm))

                GameButton(
                    label = stringResource(R.string.leaderboard),
                    icon = Icons.Filled.BarChart,
                    onClick = onLeaderboardClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            WordleText(
                text = "MADE WITH ❤\uFE0F BY ETHAR",
                color = colors.body.copy(alpha = 0.5f),
                fontSize = GameDesignTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@GameLightBackgroundPreview
@Composable
private fun PreviewHomeScreenLightMode() {
    HomeScreen(
        onPlayClick = {},
        onChallengeClick = {},
        onLeaderboardClick = {},
        onThemeChanged = {},
        onLanguageChanged = {}
    )
}

@GameDarkBackgroundPreview
@Composable
private fun PreviewHomeScreenDarkMode() {
    HomeScreen(
        onPlayClick = {},
        onChallengeClick = {},
        onLeaderboardClick = {},
        onThemeChanged = {},
        onLanguageChanged = {}
    )
}