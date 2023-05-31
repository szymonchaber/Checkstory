package dev.szymonchaber.checkstory.api.serializers

import kotlinx.serialization.Serializable
import java.util.*

typealias DtoUUID = @Serializable(UUIDSerializer::class) UUID
