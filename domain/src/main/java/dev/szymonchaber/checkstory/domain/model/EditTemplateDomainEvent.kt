package dev.szymonchaber.checkstory.domain.model

import java.util.*

sealed interface EditTemplateDomainEvent {

    val timestamp: Long

    data class CreateNewTemplate(val id: UUID, override val timestamp: Long) : EditTemplateDomainEvent

    data class RenameTemplate(val id: UUID, val newTitle: String, override val timestamp: Long) :
        EditTemplateDomainEvent

    class AddTemplateTask(
        val templateId: UUID, val taskId: UUID, val parentTaskId: String?,
        override val timestamp: Long
    ) : EditTemplateDomainEvent {
    }
}
