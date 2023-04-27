package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

sealed interface DomainCommand {

    val timestamp: Long
    val commandId: UUID
}

sealed interface TemplateDomainCommand : DomainCommand {

    fun applyTo(template: ChecklistTemplate): ChecklistTemplate

    val templateId: ChecklistTemplateId

    data class CreateNewTemplate(
        override val templateId: ChecklistTemplateId,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            val instant = Instant.ofEpochMilli(timestamp)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            return template.copy(createdAt = localDateTime)
        }
    }

    data class RenameTemplate(
        override val templateId: ChecklistTemplateId,
        val newTitle: String,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template.copy(title = newTitle)
        }
    }

    data class ChangeTemplateDescription(
        override val templateId: ChecklistTemplateId,
        val newDescription: String,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateDomainCommand {

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
    ) : TemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return if (parentTaskId != null) {
                template.copy(items = template.items.map { it.plusChildCheckboxRecursive(parentTaskId, taskId) })
            } else {
                template.copy(
                    items = template.items.plus(
                        TemplateCheckbox(
                            taskId,
                            null,
                            "",
                            listOf(),
                            template.items.size.toLong()
                        )
                    )
                )
            }
        }

        private fun TemplateCheckbox.plusChildCheckboxRecursive(
            parentId: TemplateCheckboxId,
            newCheckboxId: TemplateCheckboxId
        ): TemplateCheckbox {
            return copy(
                children = if (id == parentId) {
                    children.plus(
                        TemplateCheckbox(
                            id = newCheckboxId,
                            parentId = parentId,
                            title = "",
                            children = listOf(),
                            sortPosition = children.size.toLong()
                        )
                    )
                } else {
                    children.map {
                        it.plusChildCheckboxRecursive(parentId, newCheckboxId)
                    }
                }
            )
        }
    }

    class RenameTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        val newTitle: String,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template.copy(items = template.items
                .map {
                    it.withUpdatedTitleRecursive(taskId, newTitle)
                })
        }

        companion object {

            fun TemplateCheckbox.withUpdatedTitleRecursive(
                idToUpdate: TemplateCheckboxId,
                newTitle: String,
            ): TemplateCheckbox {
                return if (id == idToUpdate) {
                    copy(title = newTitle)
                } else {
                    copy(children = children.map { it.withUpdatedTitleRecursive(idToUpdate, newTitle) })
                }
            }
        }
    }

    class DeleteTemplateTask(
        override val templateId: ChecklistTemplateId,
        val taskId: TemplateCheckboxId,
        override val timestamp: Long,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template // TODO logic
        }
    }

    data class UpdateCheckboxPositions(
        val localPositions: Map<TemplateCheckboxId, Long>,
        override val timestamp: Long,
        override val commandId: UUID,
        override val templateId: ChecklistTemplateId
    ) : TemplateDomainCommand {

        override fun applyTo(template: ChecklistTemplate): ChecklistTemplate {
            return template.copy(
                items = template.items.map(::updatePosition).sortedBy(TemplateCheckbox::sortPosition)
            )
        }

        private fun updatePosition(templateCheckbox: TemplateCheckbox): TemplateCheckbox {
            val newPosition = localPositions[templateCheckbox.id] ?: templateCheckbox.sortPosition
            return templateCheckbox.copy(
                sortPosition = newPosition,
                children = templateCheckbox.children.map(::updatePosition).sortedBy(TemplateCheckbox::sortPosition)
            )
        }
    }
}
