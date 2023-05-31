package dev.szymonchaber.checkstory.api.event.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface CommandDto {

    val commandId: UUID
    val timestamp: Instant
}
