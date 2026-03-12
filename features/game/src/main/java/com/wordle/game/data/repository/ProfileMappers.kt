package com.wordle.game.data.repository

import com.wordle.game.data.local.entity.ProfileEntity
import com.wordle.game.data.remote.model.ProfileItem

fun ProfileItem.toEntity() = ProfileEntity(
    firebaseUid   = firebaseUid,
    documentId    = documentId,
    name          = name,
    avatarUrl     = avatarUrl,
    gamesPlayed   = gamesPlayed,
    wordsSolved   = wordsSolved,
    winPercentage = winPercentage,
    currentPoints = currentPoints,
)

fun ProfileEntity.toProfileItem() = ProfileItem(
    id            = 0,
    firebaseUid   = firebaseUid,
    documentId    = documentId,
    name          = name,
    avatarUrl     = avatarUrl,
    gamesPlayed   = gamesPlayed,
    wordsSolved   = wordsSolved,
    winPercentage = winPercentage,
    currentPoints = currentPoints,
)