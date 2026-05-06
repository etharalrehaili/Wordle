package com.khammin.core.domain.model

enum class RoomStatus(val value: String) {
    WAITING("waiting"),
    PLAYING("playing"),
    FINISHED("finished"),
    RESTARTING("restarting"),
}
