package com.khammin.game.presentation.challenge.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.buttons.GameButtonSize
import com.khammin.core.presentation.components.buttons.GameButtonVariant
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.game.R
import com.khammin.core.R as CoreR
import com.khammin.game.domain.model.ChallengeDifficulty
import com.khammin.game.domain.model.ChallengeStatus
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.game.presentation.challenge.contract.ChallengeUiItem
import com.khammin.game.presentation.challenge.vm.ChallengesViewModel
import java.util.Locale

@Composable
fun ChallengesScreen(
    onClose: Action,
    onSignInWithGoogle: Action = {},
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    viewModel: ChallengesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val isGuest by produceState(initialValue = FirebaseAuth.getInstance().currentUser?.isAnonymous == true) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            value = auth.currentUser?.isAnonymous == true
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        awaitDispose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }

    ChallengesContent(
        onClose            = onClose,
        isLoading          = state.isLoading,
        totalPoints        = state.totalPoints,
        challenges         = state.challenges,
        isGuest            = isGuest,
        onSignInWithGoogle = onSignInWithGoogle,
        currentLanguage    = currentLanguage,
    )
}

@Composable
fun ChallengesContent(
    onClose: Action,
    isLoading: Boolean = false,
    totalPoints: Int = 0,
    challenges: List<ChallengeUiItem> = emptyList(),
    isGuest: Boolean = false,
    onSignInWithGoogle: Action = {},
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
) {
    val isArabic = currentLanguage == AppLanguage.ARABIC

    Scaffold(
        modifier            = Modifier.fillMaxSize(),
        containerColor      = colors.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            GameTopBar(
                title            = stringResource(R.string.challenges_title),
                points           = totalPoints,
                endIcon          = Icons.Filled.Close,
                onEndIconClicked = onClose,
                showBackground   = false,
                modifier         = Modifier.fillMaxWidth().statusBarsPadding(),
            )
        },
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.logoGreen)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Guest banner ──────────────────────────────────────────
            if (isGuest) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.logoBlue.copy(alpha = 0.07f))
                        .border(1.dp, colors.logoBlue.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon + title row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.logoBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint               = colors.logoBlue,
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            WordleText(
                                text       = stringResource(CoreR.string.auth_join_title),
                                color      = colors.title,
                                fontSize   = GameDesignTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(2.dp))
                            WordleText(
                                text       = stringResource(CoreR.string.auth_join_subtitle),
                                color      = colors.body.copy(alpha = 0.65f),
                                fontSize   = GameDesignTheme.typography.labelSmall,
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.logoBlue.copy(alpha = 0.12f))
                    )

                    GameButton(
                        label    = stringResource(CoreR.string.auth_sign_in_google),
                        onClick  = onSignInWithGoogle,
                        variant  = GameButtonVariant.Primary,
                        size     = GameButtonSize.Small,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            val byDifficulty = challenges.groupBy { it.difficulty }

            listOf(
                ChallengeDifficulty.BEGINNER,
                ChallengeDifficulty.INTERMEDIATE,
                ChallengeDifficulty.EXPERT,
            ).forEach { difficulty ->
                val items = byDifficulty[difficulty]
                    ?.sortedBy { item ->
                        when (item.status) {
                            ChallengeStatus.AVAILABLE   -> 0
                            ChallengeStatus.IN_PROGRESS -> 1
                            ChallengeStatus.COMPLETED   -> 2
                        }
                    }
                if (!items.isNullOrEmpty()) {
                    ChallengeSection(
                        difficulty = difficulty,
                        cards = items.map { item ->
                            {
                                ChallengeCard(
                                    title  = if (isArabic) item.titleAr else item.titleEn,
                                    points = item.points,
                                    icon   = iconForName(item.iconName),
                                    item   = item,
                                )
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Maps the Firestore iconName string to a Material icon. Add more entries as needed. */
fun iconForName(name: String): ImageVector = when (name) {
    "timer"    -> Icons.Outlined.Timer
    "star"     -> Icons.Outlined.Star
    "bolt"     -> Icons.Outlined.Bolt
    "groups"   -> Icons.Outlined.Groups
    "speed"    -> Icons.Outlined.Speed
    "calendar" -> Icons.Outlined.CalendarMonth
    "trophy"   -> Icons.Outlined.EmojiEvents
    "military" -> Icons.Outlined.MilitaryTech
    "flash"    -> Icons.Outlined.FlashOn
    "grid"     -> Icons.Outlined.GridView
    "abc"      -> Icons.Outlined.Abc
    "sports"   -> Icons.Outlined.SportsEsports
    "check"    -> Icons.Outlined.CheckCircle
    else       -> Icons.Outlined.Star
}

@Composable
fun ChallengeSection(
    difficulty: ChallengeDifficulty,
    cards: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    val (titleRes, badgeRes, badgeColor) = when (difficulty) {
        ChallengeDifficulty.BEGINNER     -> Triple(R.string.difficulty_beginner,     R.string.difficulty_badge_beginner,     colors.logoGreen)
        ChallengeDifficulty.INTERMEDIATE -> Triple(R.string.difficulty_intermediate, R.string.difficulty_badge_intermediate, colors.logoOrange)
        ChallengeDifficulty.EXPERT       -> Triple(R.string.difficulty_expert,       R.string.difficulty_badge_expert,       colors.logoPink)
    }

    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WordleText(
                text       = stringResource(titleRes),
                color      = colors.title,
                fontSize   = GameDesignTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                WordleText(
                    text       = stringResource(badgeRes),
                    color      = badgeColor,
                    fontSize   = GameDesignTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        cards.forEach { card -> card() }
    }
}

@Composable
fun ChallengeCard(
    modifier: Modifier = Modifier,
    title: String,
    points: Int,
    icon: ImageVector,
    item: ChallengeUiItem? = null,
) {
    val status       = item?.status ?: ChallengeStatus.AVAILABLE
    val progress     = item?.progress ?: 0
    val target       = item?.target ?: 1
    val showProgress = status == ChallengeStatus.IN_PROGRESS && target > 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (status) {
                            ChallengeStatus.COMPLETED   -> colors.logoGreen.copy(alpha = 0.15f)
                            ChallengeStatus.IN_PROGRESS -> colors.logoOrange.copy(alpha = 0.15f)
                            ChallengeStatus.AVAILABLE   -> colors.logoBlue.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = when (status) {
                        ChallengeStatus.COMPLETED   -> colors.logoGreen
                        ChallengeStatus.IN_PROGRESS -> colors.logoOrange
                        ChallengeStatus.AVAILABLE   -> colors.logoBlue
                    },
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                WordleText(
                    text       = title,
                    color      = colors.title,
                    fontSize   = GameDesignTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                WordleText(
                    text       = "+$points pts",
                    color      = colors.logoGreen,
                    fontSize   = GameDesignTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            when {
                showProgress -> {
                    WordleText(
                        text       = String.format(Locale.US, "%d / %d", progress, target),
                        color      = colors.body.copy(alpha = 0.5f),
                        fontSize   = GameDesignTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                status == ChallengeStatus.COMPLETED -> {
                    WordleText(
                        text       = stringResource(R.string.challenge_status_completed),
                        color      = colors.logoGreen,
                        fontSize   = GameDesignTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                status == ChallengeStatus.IN_PROGRESS -> {
                    WordleText(
                        text       = stringResource(R.string.challenge_status_in_progress),
                        color      = colors.logoOrange,
                        fontSize   = GameDesignTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (showProgress) {
            val fraction = (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.logoGreen)
                )
            }
        }
    }
}