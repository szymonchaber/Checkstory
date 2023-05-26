package dev.szymonchaber.checkstory.data.database.model.command

import dev.szymonchaber.checkstory.data.api.serializers.DtoUUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed interface CommandDataEntity {

    val timestamp: Instant
    val commandId: DtoUUID
}
