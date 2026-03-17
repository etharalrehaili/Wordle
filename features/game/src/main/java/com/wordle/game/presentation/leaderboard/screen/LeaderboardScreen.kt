package com.wordle.game.presentation.leaderboard.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.PlayerAvatar
import com.wordle.core.presentation.components.PlayerCard
import com.wordle.core.presentation.components.PodiumPlayer
import com.wordle.core.presentation.components.TopThreePodium
import com.wordle.core.presentation.components.buttons.SegmentedButton
import com.wordle.core.presentation.components.enums.LeaderboardFilter
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.components.text.WordleText
import com.wordle.core.presentation.theme.GameDesignTheme
import com.wordle.core.presentation.theme.GameDesignTheme.colors
import com.wordle.core.presentation.theme.GameDesignTheme.spacing
import com.wordle.core.presentation.theme.GameDesignTheme.typography
import com.wordle.game.R
import com.wordle.game.presentation.leaderboard.contract.LeaderboardIntent
import com.wordle.game.presentation.leaderboard.contract.LeaderboardUiState
import com.wordle.game.presentation.leaderboard.vm.LeaderboardViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel(),
    onClose: Action,
) {
    val uiState by viewModel.uiState.collectAsState()

    LeaderboardContent(
        uiState  = uiState,
        onClose = onClose,
        onIntent = viewModel::onEvent,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LeaderboardContent(
    uiState: LeaderboardUiState,
    onClose: Action,
    onIntent: (LeaderboardIntent) -> Unit,
) {

    Log.d("Leaderboard", "LeaderboardContent composed, isLoading=${uiState.isLoading}, error=${uiState.error}, players=${uiState.players.size}")

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────
            GameTopBar(
                title = stringResource(R.string.leaderboard),
                endIcon = Icons.Filled.Close,
                onEndIconClicked = onClose,
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colors.buttonTeal,
                            strokeWidth = 2.dp,
                        )
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = colors.body.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            WordleText(
                                text = uiState.error ?: "",
                                color = colors.body.copy(alpha = 0.5f),
                                fontSize = typography.labelMedium,
                            )
                        }
                    }
                }

                else -> {
                    val now = System.currentTimeMillis()
                    val oneWeekMs = 7L * 24 * 60 * 60 * 1000
                    val oneMonthMs = 30L * 24 * 60 * 60 * 1000

                    Log.d("Leaderboard", "Total players: ${uiState.players.size}")
                    Log.d("Leaderboard", "Selected filter: ${uiState.selectedFilter}")
                    uiState.players.forEach { player ->
                        Log.d("Leaderboard", "Player: ${player.name}, lastPlayedAt=${player.lastPlayedAt}")
                    }

                    val filteredPlayers = when (uiState.selectedFilter) {
                        LeaderboardFilter.THIS_WEEK -> uiState.players.filter { player ->
                            player.lastPlayedAt?.let {
                                try {
                                    val time = java.time.Instant.parse(it).toEpochMilli()
                                    val diff = now - time
                                    Log.d("Leaderboard", "Player: ${player.name}, diff=${diff}ms, oneWeekMs=$oneWeekMs, included=${diff <= oneWeekMs}")
                                    diff <= oneWeekMs
                                } catch (e: Exception) {
                                    Log.e("Leaderboard", "Failed to parse lastPlayedAt for ${player.name}: $it, error=${e.message}")
                                    true
                                }
                            } ?: run {
                                Log.d("Leaderboard", "Player: ${player.name} has no lastPlayedAt → excluded")
                                false
                            }
                        }
                        LeaderboardFilter.THIS_MONTH -> uiState.players.filter { player ->
                            player.lastPlayedAt?.let {
                                try {
                                    val time = java.time.Instant.parse(it).toEpochMilli()
                                    val diff = now - time
                                    Log.d("Leaderboard", "Player: ${player.name}, diff=${diff}ms, oneMonthMs=$oneMonthMs, included=${diff <= oneMonthMs}")
                                    diff <= oneMonthMs
                                } catch (e: Exception) {
                                    Log.e("Leaderboard", "Failed to parse lastPlayedAt for ${player.name}: $it, error=${e.message}")
                                    true
                                }
                            } ?: run {
                                Log.d("Leaderboard", "Player: ${player.name} has no lastPlayedAt → excluded")
                                false
                            }
                        }
                        LeaderboardFilter.ALL_TIME -> uiState.players
                    }

                    Log.d("Leaderboard", "Filtered players count: ${filteredPlayers.size}")

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
                            val selectedLabel = filterLabels[filterValues.indexOf(uiState.selectedFilter)]

                            SegmentedButton(
                                options          = filterLabels,
                                selectedOption   = selectedLabel,
                                onOptionSelected = { label ->
                                    val index = filterLabels.indexOf(label)
                                    onIntent(LeaderboardIntent.ChangeFilter(filterValues[index]))
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )

                            Box(
                                modifier         = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.offset(y = (-48).dp)
                                ) {
                                    Text(text = "🏆", fontSize = 48.sp)
                                    WordleText(
                                        text      = stringResource(R.string.leaderboard_no_winners),
                                        color     = colors.body.copy(alpha = 0.5f),
                                        fontSize  = typography.labelMedium,
                                        textAlign = TextAlign.Center,
                                        modifier  = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier       = Modifier.fillMaxSize(),
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
                                val selectedLabel = filterLabels[filterValues.indexOf(uiState.selectedFilter)]

                                SegmentedButton(
                                    options          = filterLabels,
                                    selectedOption   = selectedLabel,
                                    onOptionSelected = { label ->
                                        val index = filterLabels.indexOf(label)
                                        onIntent(LeaderboardIntent.ChangeFilter(filterValues[index]))
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                            // ── Podium ────────────────────────────────────
                            if (top3.size >= 3) {
                                item {
                                    TopThreePodium(
                                        first = PodiumPlayer(
                                            top3[0].name,
                                            top3[0].currentPoints,
                                            top3[0].avatarUrl,
                                            top3[0].firebaseUid == currentUid
                                        ),
                                        second = PodiumPlayer(
                                            top3[1].name,
                                            top3[1].currentPoints,
                                            top3[1].avatarUrl,
                                            top3[1].firebaseUid == currentUid
                                        ),
                                        third = PodiumPlayer(
                                            top3[2].name,
                                            top3[2].currentPoints,
                                            top3[2].avatarUrl,
                                            top3[2].firebaseUid == currentUid
                                        ),
                                    )
                                }
                            }

                            // ── Section label ─────────────────────────────
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
                                    points = player.currentPoints,
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
                                            points = me.currentPoints,
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