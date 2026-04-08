package com.khammin.game.presentation.leaderboard.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.khammin.core.alias.Action
import com.khammin.core.presentation.components.PlayerCard
import com.khammin.core.presentation.components.PodiumPlayer
import com.khammin.core.presentation.components.TopThreePodium
import com.khammin.core.presentation.components.buttons.GameButton
import com.khammin.core.presentation.components.buttons.SegmentedButton
import com.khammin.core.presentation.components.enums.AppLanguage
import com.khammin.core.presentation.components.enums.LeaderboardFilter
import com.khammin.core.presentation.components.navigation.GameTopBar
import com.khammin.core.presentation.components.text.WordleText
import com.khammin.core.presentation.theme.GameDesignTheme
import com.khammin.core.presentation.theme.GameDesignTheme.colors
import com.khammin.core.presentation.theme.GameDesignTheme.typography
import com.khammin.game.R
import com.khammin.game.presentation.leaderboard.contract.LeaderboardIntent
import com.khammin.game.presentation.leaderboard.contract.LeaderboardUiState
import com.khammin.game.presentation.leaderboard.vm.LeaderboardViewModel
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel(),
    onClose: Action,
    currentLanguage: AppLanguage,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(LeaderboardIntent.ChangeLanguage("ar"))
    }

    LeaderboardContent(
        uiState  = uiState,
        onClose  = onClose,
        onIntent = viewModel::onEvent,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardContent(
    uiState: LeaderboardUiState,
    onClose: Action,
    onIntent: (LeaderboardIntent) -> Unit,
) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // ── Top bar stays outside the refresh area ────────────────
        GameTopBar(
            title            = stringResource(R.string.leaderboard),
            endIcon          = Icons.Filled.Close,
            onEndIconClicked = onClose,
            containerColor   = Color.Transparent,
            showBackground     = false,
            modifier           = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        )

        // ── PullToRefreshBox wraps only the scrollable content ────
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = { onIntent(LeaderboardIntent.Refresh) },
            modifier     = Modifier.fillMaxSize(),
        ) {
            when {
                uiState.isLoading || uiState.isRefreshing -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color       = colors.buttonTeal,
                            strokeWidth = 2.dp,
                        )
                    }
                }

                uiState.noInternet -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier            = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.WifiOff,
                                contentDescription = null,
                                tint               = colors.body.copy(alpha = 0.3f),
                                modifier           = Modifier.size(48.dp)
                            )
                            WordleText(
                                text      = "No Internet Connection",
                                color     = colors.title,
                                fontSize  = typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            WordleText(
                                text      = "Please check your connection and try again",
                                color     = colors.body.copy(alpha = 0.5f),
                                fontSize  = typography.labelMedium,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(8.dp))
                            GameButton(
                                label           = "Try Again",
                                backgroundColor = colors.buttonTeal,
                                contentColor    = colors.title,
                                showBorder      = false,
                                onClick         = { onIntent(LeaderboardIntent.ChangeLanguage(uiState.language)) },
                                modifier        = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint               = colors.body.copy(alpha = 0.3f),
                                modifier           = Modifier.size(48.dp)
                            )
                            WordleText(
                                text     = uiState.error ?: "",
                                color    = colors.body.copy(alpha = 0.5f),
                                fontSize = typography.labelMedium,
                            )
                        }
                    }
                }

                else -> {
                    val now = System.currentTimeMillis()
                    val oneWeekMs = 7L * 24 * 60 * 60 * 1000
                    val oneMonthMs = 30L * 24 * 60 * 60 * 1000

                    uiState.players.forEach { player ->
                    }

                    val filteredPlayers = when (uiState.selectedFilter) {
                        LeaderboardFilter.THIS_WEEK -> uiState.players.filter { player ->
                            val lastPlayed = player.arLastPlayedAt
                            lastPlayed?.let {
                                try {
                                    val time = Instant.parse(it).toEpochMilli()
                                    now - time <= oneWeekMs
                                } catch (e: Exception) { true }
                            } ?: false
                        }

                        LeaderboardFilter.THIS_MONTH -> uiState.players.filter { player ->
                            val lastPlayed = player.arLastPlayedAt
                            lastPlayed?.let {
                                try {
                                    val time = Instant.parse(it).toEpochMilli()
                                    now - time <= oneMonthMs
                                } catch (e: Exception) { true }
                            } ?: false
                        }

                        LeaderboardFilter.ALL_TIME -> uiState.players
                    }

                    val players = filteredPlayers
                    val top3 = players.take(3)
                    val rest = players.drop(3)
                    val currentPlayer = players.indexOfFirst { it.firebaseUid == currentUid }

                    if (players.isEmpty()) {
                        // ── Show segmented button + empty state ───────────────
                        Column(modifier = Modifier.fillMaxSize()) {
                            val filterLabels = listOf(
                                stringResource(R.string.filter_this_week),
                                stringResource(R.string.filter_this_month),
                                stringResource(R.string.filter_all_time),
                            )
                            val filterValues = LeaderboardFilter.entries
                            val selectedLabel =
                                filterLabels[filterValues.indexOf(uiState.selectedFilter)]

                            SegmentedButton(
                                options = filterLabels,
                                selectedOption = selectedLabel,
                                onOptionSelected = { label ->
                                    val index = filterLabels.indexOf(label)
                                    onIntent(LeaderboardIntent.ChangeFilter(filterValues[index]))
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.offset(y = (-48).dp)
                                ) {
                                    Text(text = "🏆", fontSize = 48.sp)
                                    WordleText(
                                        text = stringResource(R.string.leaderboard_no_winners),
                                        color = colors.body.copy(alpha = 0.5f),
                                        fontSize = typography.labelMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp),
                        ) {
                            // ── Segmented Button ──────────────────────────────
                            item {
                                val filterLabels = listOf(
                                    stringResource(R.string.filter_this_week),
                                    stringResource(R.string.filter_this_month),
                                    stringResource(R.string.filter_all_time),
                                )
                                val filterValues = LeaderboardFilter.entries
                                val selectedLabel =
                                    filterLabels[filterValues.indexOf(uiState.selectedFilter)]

                                SegmentedButton(
                                    options = filterLabels,
                                    selectedOption = selectedLabel,
                                    onOptionSelected = { label ->
                                        val index = filterLabels.indexOf(label)
                                        onIntent(LeaderboardIntent.ChangeFilter(filterValues[index]))
                                    },
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )
                            }

                            // ── Podium — always shown, empty slots for missing 2nd/3rd ──
                            item {
                                TopThreePodium(
                                    first = PodiumPlayer(
                                        top3[0].name,
                                        top3[0].arCurrentPoints,
                                        top3[0].avatarUrl,
                                        top3[0].firebaseUid == currentUid
                                    ),
                                    second = top3.getOrNull(1)?.let {
                                        PodiumPlayer(
                                            it.name,
                                            it.arCurrentPoints,
                                            it.avatarUrl,
                                            it.firebaseUid == currentUid
                                        )
                                    },
                                    third = top3.getOrNull(2)?.let {
                                        PodiumPlayer(
                                            it.name,
                                            it.arCurrentPoints,
                                            it.avatarUrl,
                                            it.firebaseUid == currentUid
                                        )
                                    },
                                )
                            }

                            // ── Rest of players (rank 4+) ─────────────────────────────
                            if (rest.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        HorizontalDivider(
                                            modifier = Modifier.weight(1f),
                                            color = colors.divider.copy(alpha = 0.5f),
                                            thickness = 0.5.dp,
                                        )
                                        WordleText(
                                            text = stringResource(R.string.leaderboard_rankings),
                                            color = colors.body.copy(alpha = 0.35f),
                                            fontSize = GameDesignTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 1.sp,
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.weight(1f),
                                            color = colors.divider.copy(alpha = 0.5f),
                                            thickness = 0.5.dp,
                                        )
                                    }
                                }
                            }

                            // ── Rest of players ───────────────────────────
                            itemsIndexed(rest) { index, player ->
                                val isMe = player.firebaseUid == currentUid
                                PlayerCard(
                                    rank = index + 4,
                                    name = player.name,
                                    points = player.arCurrentPoints,
                                    avatarUrl = player.avatarUrl,
                                    borderColor = if (isMe) colors.buttonTeal else colors.buttonTaupe,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 8.dp)
                                        .then(
                                            if (isMe) Modifier.background(
                                                color = colors.buttonTeal.copy(alpha = 0.20f),
                                                shape = RoundedCornerShape(16.dp),
                                            ) else Modifier
                                        )
                                )
                            }

                            // ── Pinned current user ───────────────────────
                            if (currentPlayer == -1) {
                                val me = players.firstOrNull { it.firebaseUid == currentUid }
                                if (me != null) {
                                    item {
                                        HorizontalDivider(
                                            color = colors.divider.copy(alpha = 0.4f),
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 8.dp
                                            )
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp)
                                                .padding(bottom = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.PersonPin,
                                                contentDescription = null,
                                                tint = colors.buttonPink.copy(alpha = 0.6f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            WordleText(
                                                text = stringResource(R.string.leaderboard_your_position),
                                                color = colors.body.copy(alpha = 0.35f),
                                                fontSize = GameDesignTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 1.sp,
                                            )
                                        }
                                        PlayerCard(
                                            rank = players.size + 1,
                                            name = me.name,
                                            points      = me.arCurrentPoints,
                                            avatarUrl = me.avatarUrl,
                                            borderColor = colors.buttonPink,
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                                .padding(bottom = 16.dp)
                                                .background(
                                                    color = colors.buttonPink.copy(alpha = 0.05f),
                                                    shape = RoundedCornerShape(16.dp),
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}