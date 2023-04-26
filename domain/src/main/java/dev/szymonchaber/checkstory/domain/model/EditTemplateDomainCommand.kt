package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import java.util.*

sealed interface DomainCommand {

    val timestamp: Long
    val eventId: UUID
}

sealed interface EditTemplateDomainCommand : DomainCommand {

    val templateId: ChecklistTemplateId

    data class CreateNewTemplate(
        override val templateId: ChecklistTemplateId,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand

    data class RenameTemplate(
        override val templateId: ChecklistTemplateId,
        val newTitle: String,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand

    data class ChangeTemplateDescription(
        override val templateId: ChecklistTemplateId,
        val newDescription: String,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand

    class AddTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        val parentTaskId: TemplateCheckboxId?,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand

    class RenameTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        val newTitle: String,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand

    class DeleteTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        override val timestamp: Long,
        override val eventId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand
}
