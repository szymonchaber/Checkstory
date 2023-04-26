package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import java.util.*

sealed interface DomainCommand {

    val timestamp: Long
    val commandId: UUID
}

sealed interface EditTemplateDomainCommand : DomainCommand {

    fun applyTo(template: ChecklistTemplate): ChecklistTemplate

    val templateId: ChecklistTemplateId

    data class CreateNewTemplate(
        override val templateId: ChecklistTemplateId,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template
        }
    }

    data class RenameTemplate(
        override val templateId: ChecklistTemplateId,
        val newTitle: String,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template.copy(title = newTitle)
        }
    }

    data class ChangeTemplateDescription(
        override val templateId: ChecklistTemplateId,
        val newDescription: String,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template.copy(description = newDescription)
        }
    }

    class AddTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        val parentTaskId: TemplateCheckboxId?,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template.copy(
                items = template.items.plusElement(
                    TemplateCheckbox(
                        taskId,
                        parentTaskId,
                        "",
                        listOf(),
                        0
                    )
                )
            )
        }
    }

    class RenameTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        val newTitle: String,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template // TODO logic
        }
    }

    class DeleteTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : EditTemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template // TODO logic
        }
    }
}
