package com.khammin.core.domain.model

enum class TileState {
    EMPTY,
    FILLED,
    CORRECT,
    MISPLACED,
    WRONG,
    SIMILAR  // right position, phonetically similar letter (e.g. ه ↔ ة)
}
