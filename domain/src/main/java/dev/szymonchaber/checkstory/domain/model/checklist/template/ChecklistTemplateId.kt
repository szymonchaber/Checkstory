package dev.szymonchaber.checkstory.domain.model.checklist.template

import java.io.Serializable
import java.util.*

@JvmInline
value class ChecklistTemplateId(val id: UUID) : Serializable {

    companion object {

        fun new() = ChecklistTemplateId(UUID.randomUUID())
    }
}
