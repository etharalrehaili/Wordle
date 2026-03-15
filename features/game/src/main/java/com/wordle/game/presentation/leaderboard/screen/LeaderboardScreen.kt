package com.wordle.game.presentation.leaderboard.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.wordle.core.alias.Action
import com.wordle.core.presentation.components.PlayerCard
import com.wordle.core.presentation.components.PodiumPlayer
import com.wordle.core.presentation.components.TopThreePodium
import com.wordle.core.presentation.components.navigation.GameTopBar
import com.wordle.core.presentation.components.text.WordleText
import com.wordle.core.presentation.theme.GameDesignTheme
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

            // ── Header ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.buttonPink.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                GameTopBar(
                    title            = stringResource(R.string.leaderboard),
                    endIcon          = Icons.Filled.Close,
                    onEndIconClicked = onClose,
                    modifier         = Modifier.fillMaxWidth(),
                    containerColor   = Color.Transparent,
                )
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color       = colors.buttonTeal,
                            strokeWidth = 2.dp,
                        )
                    }
                }

                uiState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                                fontSize = GameDesignTheme.typography.labelMedium,
                            )
                        }
                    }
                }

                else -> {
                    val players       = uiState.players
                    val top3          = players.take(3)
                    val rest          = players.drop(3)
                    val currentPlayer = players.indexOfFirst { it.firebaseUid == currentUid }

                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding      = PaddingValues(bottom = 32.dp),
                    ) {

                        // ── Podium ────────────────────────────────────
                        if (top3.size >= 3) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    colors.buttonTeal.copy(alpha = 0.07f),
                                                    Color.Transparent,
                                                )
                                            )
                                        )
                                        .padding(bottom = 8.dp)
                                ) {
                                    TopThreePodium(
                                        first  = PodiumPlayer(top3[0].name, top3[0].currentPoints, top3[0].avatarUrl),
                                        second = PodiumPlayer(top3[1].name, top3[1].currentPoints, top3[1].avatarUrl),
                                        third  = PodiumPlayer(top3[2].name, top3[2].currentPoints, top3[2].avatarUrl),
                                    )
                                }
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
                                        modifier  = Modifier.weight(1f),
                                        color     = colors.divider.copy(alpha = 0.5f),
                                        thickness = 0.5.dp,
                                    )
                                    WordleText(
                                        text          = "Rankings",
                                        color         = colors.body.copy(alpha = 0.35f),
                                        fontSize      = GameDesignTheme.typography.labelSmall,
                                        fontWeight    = FontWeight.Medium,
                                        letterSpacing = 1.sp,
                                    )
                                    HorizontalDivider(
                                        modifier  = Modifier.weight(1f),
                                        color     = colors.divider.copy(alpha = 0.5f),
                                        thickness = 0.5.dp,
                                    )
                                }
                            }
                        }

                        // ── Rest of players ───────────────────────────
                        itemsIndexed(rest) { index, player ->
                            val isMe = player.firebaseUid == currentUid
                            PlayerCard(
                                rank        = index + 4,
                                name        = player.name,
                                points      = player.currentPoints,
                                avatarUrl   = player.avatarUrl,
                                borderColor = if (isMe) colors.buttonTeal else colors.border,
                                modifier    = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 8.dp)
                                    .then(
                                        if (isMe) Modifier.background(
                                            color = colors.buttonTeal.copy(alpha = 0.05f),
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
                                        color     = colors.divider.copy(alpha = 0.4f),
                                        thickness = 0.5.dp,
                                        modifier  = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                                            imageVector        = Icons.Outlined.PersonPin,
                                            contentDescription = null,
                                            tint               = colors.buttonPink.copy(alpha = 0.6f),
                                            modifier           = Modifier.size(14.dp)
                                        )
                                        WordleText(
                                            text          = "Your position",
                                            color         = colors.body.copy(alpha = 0.35f),
                                            fontSize      = GameDesignTheme.typography.labelSmall,
                                            fontWeight    = FontWeight.Medium,
                                            letterSpacing = 1.sp,
                                        )
                                    }
                                    PlayerCard(
                                        rank        = players.size + 1,
                                        name        = me.name,
                                        points      = me.currentPoints,
                                        avatarUrl   = me.avatarUrl,
                                        borderColor = colors.buttonPink,
                                        modifier    = Modifier
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