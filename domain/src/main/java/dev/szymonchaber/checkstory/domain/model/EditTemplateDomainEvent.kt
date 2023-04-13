package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import java.util.*

sealed interface EditTemplateDomainEvent {

    val timestamp: Long

    data class CreateNewTemplate(val id: ChecklistTemplateId, override val timestamp: Long) : EditTemplateDomainEvent

    data class RenameTemplate(val id: ChecklistTemplateId, val newTitle: String, override val timestamp: Long) :
        EditTemplateDomainEvent

    class AddTemplateTask(
        val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        val parentTaskId: UUID?,
        override val timestamp: Long
    ) : EditTemplateDomainEvent
}
