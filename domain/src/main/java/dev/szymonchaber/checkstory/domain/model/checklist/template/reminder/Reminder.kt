package dev.szymonchaber.checkstory.domain.model.checklist.template.reminder

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import java.io.Serializable
import java.time.LocalDateTime

sealed class Reminder {

    abstract val id: ReminderId
    abstract val forTemplate: ChecklistTemplateId

    val isStored: Boolean
        get() = id.id != 0L

    data class Exact(
        override val id: ReminderId,
        override val forTemplate: ChecklistTemplateId,
        val dateTime: LocalDateTime
    ) : Reminder()
}

@JvmInline
value class ReminderId(val id: Long) : Serializable
