package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import java.util.*

sealed interface EditTemplateDomainEvent {

    val timestamp: Long
    val eventId: UUID
    val id: ChecklistTemplateId

    data class CreateNewTemplate(
        override val id: ChecklistTemplateId,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent

    data class RenameTemplate(
        override val id: ChecklistTemplateId,
        val newTitle: String,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent

    data class ChangeTemplateDescription(
        override val id: ChecklistTemplateId,
        val newDescription: String,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent

    class AddTemplateTask(
        override val id: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        val parentTaskId: UUID?,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent

    class DeleteTemplateTask(
        override val id: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent
}
