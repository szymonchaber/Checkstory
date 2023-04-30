package dev.szymonchaber.checkstory.data.api.event.dto

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface CommandDto {

    val eventId: UUID
    val timestamp: Long
    val userId: String
}
