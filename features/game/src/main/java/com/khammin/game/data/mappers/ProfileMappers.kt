package com.khammin.game.data.mappers

import com.khammin.game.data.local.entity.ProfileEntity
import com.khammin.game.data.remote.model.ProfileItem
import com.khammin.game.domain.model.Profile

// ProfileItem (API) → ProfileEntity (Room)
fun ProfileItem.toEntity() = ProfileEntity(
    firebaseUid   = firebaseUid,
    documentId    = documentId,
    name          = name,
    avatarUrl     = avatarUrl,
    enGamesPlayed    = enGamesPlayed,
    enWordsSolved    = enWordsSolved,
    enWinPercentage  = enWinPercentage,
    enCurrentPoints  = enCurrentPoints,
    enLastPlayedAt   = enLastPlayedAt,
    arGamesPlayed    = arGamesPlayed,
    arWordsSolved    = arWordsSolved,
    arWinPercentage  = arWinPercentage,
    arCurrentPoints  = arCurrentPoints,
    arLastPlayedAt   = arLastPlayedAt,
)

// ProfileItem (API) → Profile (Domain)
fun ProfileItem.toDomain() = Profile(
    id               = id,
    documentId       = documentId,
    firebaseUid      = firebaseUid,
    name             = name,
    avatarUrl        = avatarUrl,
    enGamesPlayed    = enGamesPlayed,
    enWordsSolved    = enWordsSolved,
    enWinPercentage  = enWinPercentage,
    enCurrentPoints  = enCurrentPoints,
    enLastPlayedAt   = enLastPlayedAt,
    arGamesPlayed    = arGamesPlayed,
    arWordsSolved    = arWordsSolved,
    arWinPercentage  = arWinPercentage,
    arCurrentPoints  = arCurrentPoints,
    arLastPlayedAt   = arLastPlayedAt,
)

// ProfileEntity (Room) → Profile (Domain)
fun ProfileEntity.toDomain() = Profile(
    id            = 0,
    firebaseUid   = firebaseUid,
    documentId    = documentId,
    name          = name,
    avatarUrl     = avatarUrl,
    enGamesPlayed    = enGamesPlayed,
    enWordsSolved    = enWordsSolved,
    enWinPercentage  = enWinPercentage,
    enCurrentPoints  = enCurrentPoints,
    enLastPlayedAt   = enLastPlayedAt,
    arGamesPlayed    = arGamesPlayed,
    arWordsSolved    = arWordsSolved,
    arWinPercentage  = arWinPercentage,
    arCurrentPoints  = arCurrentPoints,
    arLastPlayedAt   = arLastPlayedAt,
)