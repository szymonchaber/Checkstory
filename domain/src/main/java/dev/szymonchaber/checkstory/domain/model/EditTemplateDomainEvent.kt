package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import java.util.*

sealed interface EditTemplateDomainEvent {

    val timestamp: Long
    val eventId: UUID
    val templateId: ChecklistTemplateId

    data class CreateNewTemplate(
        override val templateId: ChecklistTemplateId,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent

    data class RenameTemplate(
        override val templateId: ChecklistTemplateId,
        val newTitle: String,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent

    data class ChangeTemplateDescription(
        override val templateId: ChecklistTemplateId,
        val newDescription: String,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent

    class AddTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        val parentTaskId: TemplateCheckboxId?,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent

    class RenameTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        val newTitle: String,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent

    class DeleteTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainEvent
}
