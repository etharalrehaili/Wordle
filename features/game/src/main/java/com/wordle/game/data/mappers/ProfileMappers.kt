package com.wordle.game.data.mappers

import com.wordle.game.data.local.entity.ProfileEntity
import com.wordle.game.data.remote.model.ProfileItem
import com.wordle.game.domain.model.Profile

// ProfileItem (API) → ProfileEntity (Room)
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

// ProfileItem (API) → Profile (Domain)
fun ProfileItem.toDomain() = Profile(
    id            = id,
    firebaseUid   = firebaseUid,
    documentId    = documentId,
    name          = name,
    avatarUrl     = avatarUrl,
    gamesPlayed   = gamesPlayed,
    wordsSolved   = wordsSolved,
    winPercentage = winPercentage,
    currentPoints = currentPoints,
    lastPlayedAt = lastPlayedAt,
)

// ProfileEntity (Room) → Profile (Domain)
fun ProfileEntity.toDomain() = Profile(
    id            = 0,
    firebaseUid   = firebaseUid,
    documentId    = documentId,
    name          = name,
    avatarUrl     = avatarUrl,
    gamesPlayed   = gamesPlayed,
    wordsSolved   = wordsSolved,
    winPercentage = winPercentage,
    currentPoints = currentPoints
)