package dev.szymonchaber.checkstory.api.command.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
internal sealed interface ApiCommand {

    val commandId: UUID
    val timestamp: Instant
}
