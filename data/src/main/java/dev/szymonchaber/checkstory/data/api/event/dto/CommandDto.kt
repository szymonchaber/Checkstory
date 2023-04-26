package dev.szymonchaber.checkstory.data.api.event.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed interface CommandDto {

    @Transient
    val commandType: String
    val eventId: String
    val timestamp: Long
    val userId: String
}
