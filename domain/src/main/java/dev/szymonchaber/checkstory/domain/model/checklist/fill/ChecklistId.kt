package dev.szymonchaber.checkstory.domain.model.checklist.fill

import java.io.Serializable
import java.util.*

@JvmInline
value class ChecklistId(val id: UUID) : Serializable {

    companion object {

        fun new(): ChecklistId = ChecklistId(UUID.randomUUID())
    }
}
