package com.khammin.game.domain.model

import com.khammin.core.domain.model.TileState

data class Tile(
    val letter: Char = ' ',
    val state: TileState = TileState.EMPTY,
)
