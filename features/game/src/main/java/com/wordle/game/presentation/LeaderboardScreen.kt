package com.wordle.game.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.PlayerCard
import com.wordle.core.presentation.components.PodiumPlayer
import com.wordle.core.presentation.components.TopThreePodium
import com.wordle.core.presentation.components.buttons.SegmentedButton
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.game.R

private val mockPlayers = listOf(
    Triple("Ahmed Al-Rashid", 24500, null),
    Triple("Sarah Jenkins",   11820, null),
    Triple("Omar Hassan",      9340, null),
    Triple("Lena Fischer",     7890, null),
    Triple("Carlos Mendez",    6450, null),
)

data class CurrentPlayer(
    val name: String,
    val points: Int,
    val rank: Int,
    val avatarUrl: String? = null,
)

@Composable
fun LeaderboardScreen(
    onClose: Action,
    currentPlayer: CurrentPlayer = CurrentPlayer(
        name   = "You",
        points = 2150,
        rank   = 12,
    )
) {
    val colors = LocalWordleColors.current
    val filterOptions = listOf(
        stringResource(R.string.filter_all_time),
        stringResource(R.string.filter_this_week),
        stringResource(R.string.filter_today),
    )
    var selectedFilter by remember { mutableStateOf(filterOptions.first()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameTopBar(
                title            = stringResource(R.string.leaderboard),
                endIcon          = Icons.Filled.Close,
                onEndIconClicked = { onClose() },
                modifier         = Modifier.fillMaxWidth()
            )

            TopThreePodium(
                first  = PodiumPlayer(mockPlayers[0].first, mockPlayers[0].second),
                second = PodiumPlayer(mockPlayers[1].first, mockPlayers[1].second),
                third  = PodiumPlayer(mockPlayers[2].first, mockPlayers[2].second),
            )

            SegmentedButton(
                options          = filterOptions,
                selectedOption   = selectedFilter,
                onOptionSelected = { selectedFilter = it },
                modifier         = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyColumn(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(mockPlayers) { index, (name, points, avatar) ->
                    PlayerCard(
                        rank      = index + 1,
                        name      = name,
                        points    = points,
                        avatarUrl = avatar,
                    )
                }

                item {
                    PlayerCard(
                        rank        = currentPlayer.rank,
                        name        = currentPlayer.name,
                        points      = currentPlayer.points,
                        avatarUrl   = currentPlayer.avatarUrl,
                        borderColor = colors.correct,
                        modifier    = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}