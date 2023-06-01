package dev.szymonchaber.checkstory.domain.model.checklist.template

import java.io.Serializable
import java.util.*

@JvmInline
value class TemplateId(val id: UUID) : Serializable {

    companion object {

        fun new() = TemplateId(UUID.randomUUID())

        fun fromUuidString(uuidString: String): TemplateId {
            return TemplateId(UUID.fromString(uuidString))
        }
    }
}
