package dev.szymonchaber.checkstory.data.api.event.dto

import kotlinx.serialization.Serializable

@Serializable
sealed interface CommandDto {

    val eventId: String
    val timestamp: Long
    val userId: String
}
