package com.khammin.game.data.mappers

import com.khammin.game.data.local.entity.ProfileEntity
import com.khammin.game.data.remote.model.ProfileItem
import com.khammin.game.domain.model.Profile

// ProfileItem (API) → ProfileEntity (Room) — always synced, never pending
fun ProfileItem.toEntity() = ProfileEntity(
    firebaseUid         = firebaseUid,
    documentId          = documentId,
    name                = name,
    avatarUrl           = avatarUrl,
    enGamesPlayed       = enGamesPlayed,
    enWordsSolved       = enWordsSolved,
    enWinPercentage     = enWinPercentage,
    enCurrentPoints     = enCurrentPoints,
    enLastPlayedAt      = enLastPlayedAt,
    arGamesPlayed       = arGamesPlayed,
    arWordsSolved       = arWordsSolved,
    arWinPercentage     = arWinPercentage,
    arCurrentPoints     = arCurrentPoints,
    arLastPlayedAt      = arLastPlayedAt,
    pendingSync         = false,
    pendingSyncLanguage = null,
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
    id               = 0,
    firebaseUid      = firebaseUid,
    documentId       = documentId,
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

// Profile (Domain) → ProfileEntity (Room) — used when saving offline-pending updates
fun Profile.toEntity(pendingSync: Boolean = false, pendingSyncLanguage: String? = null) = ProfileEntity(
    firebaseUid         = firebaseUid,
    documentId          = documentId,
    name                = name,
    avatarUrl           = avatarUrl,
    enGamesPlayed       = enGamesPlayed,
    enWordsSolved       = enWordsSolved,
    enWinPercentage     = enWinPercentage,
    enCurrentPoints     = enCurrentPoints,
    enLastPlayedAt      = enLastPlayedAt,
    arGamesPlayed       = arGamesPlayed,
    arWordsSolved       = arWordsSolved,
    arWinPercentage     = arWinPercentage,
    arCurrentPoints     = arCurrentPoints,
    arLastPlayedAt      = arLastPlayedAt,
    pendingSync         = pendingSync,
    pendingSyncLanguage = pendingSyncLanguage,
)
