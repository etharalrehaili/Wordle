package com.wordle.game.presentation.leaderboard.screen

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.PlayerCard
import com.wordle.core.presentation.components.PodiumPlayer
import com.wordle.core.presentation.components.TopThreePodium
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.theme.LocalWordleColors
import com.wordle.game.R
import com.wordle.game.presentation.leaderboard.vm.LeaderboardViewModel

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel(),
    onClose: Action,
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    val colors = LocalWordleColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameTopBar(
                title            = stringResource(R.string.leaderboard),
                endIcon          = Icons.Filled.Close,
                onEndIconClicked = onClose,
                modifier         = Modifier.fillMaxWidth()
            )

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.correct)
                    }
                }
                uiState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.error ?: "", color = colors.body)
                    }
                }
                else -> {
                    val players = uiState.players
                    val top3 = players.take(3)
                    val rest = players.drop(3)
                    val currentPlayer = players.indexOfFirst { it.firebaseUid == currentUid }

                    if (top3.size >= 3) {
                        TopThreePodium(
                            first  = PodiumPlayer(top3[0].name, top3[0].currentPoints, top3[0].avatarUrl),
                            second = PodiumPlayer(top3[1].name, top3[1].currentPoints, top3[1].avatarUrl),
                            third  = PodiumPlayer(top3[2].name, top3[2].currentPoints, top3[2].avatarUrl),
                        )
                    }

                    LazyColumn(
                        modifier            = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(rest) { index, player ->
                            PlayerCard(
                                rank      = index + 4,
                                name      = player.name,
                                points    = player.currentPoints,
                                avatarUrl = player.avatarUrl,
                                borderColor = if (player.firebaseUid == currentUid) colors.correct else colors.border,
                            )
                        }

                        // Pin current user at bottom if not in top 10
                        if (currentPlayer == -1) {
                            val me = players.firstOrNull { it.firebaseUid == currentUid }
                            if (me != null) {
                                item {
                                    PlayerCard(
                                        rank        = players.size + 1,
                                        name        = me.name,
                                        points      = me.currentPoints,
                                        avatarUrl   = me.avatarUrl,
                                        borderColor = colors.correct,
                                        modifier    = Modifier.padding(bottom = 16.dp)
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