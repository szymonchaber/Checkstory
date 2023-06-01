package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

sealed interface Command {

    val timestamp: Instant
    val commandId: UUID
}

sealed interface TemplateCommand : Command {

    fun applyTo(template: Template): Template

    val templateId: TemplateId

    data class CreateNewTemplate(
        override val templateId: TemplateId,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID(),
        val existingData: Template? = null
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            val instant = timestamp
            val localDateTime = LocalDateTime.ofInstant(instant.toJavaInstant(), ZoneId.systemDefault())
            return template.copy(createdAt = localDateTime)
        }
    }

    data class RenameTemplate(
        override val templateId: TemplateId,
        val newTitle: String,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            return template.copy(title = newTitle)
        }
    }

    data class ChangeTemplateDescription(
        override val templateId: TemplateId,
        val newDescription: String,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            return template.copy(description = newDescription)
        }
    }

    class AddTemplateTask(
        override val templateId: TemplateId,
        val taskId: TemplateCheckboxId,
        val parentTaskId: TemplateCheckboxId?,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
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
                            template.items.size.toLong(),
                            templateId
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
                            sortPosition = children.size.toLong(),
                            templateId = templateId
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
        override val templateId: TemplateId,
        val taskId: TemplateCheckboxId,
        val newTitle: String,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
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
        override val templateId: TemplateId,
        val taskId: TemplateCheckboxId,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            return template.copy(items = template.items.withExtractedTask(taskId).first)
        }
    }

    data class UpdateCheckboxPositions(
        val localPositions: Map<TemplateCheckboxId, Long>,
        override val timestamp: Instant,
        override val commandId: UUID,
        override val templateId: TemplateId
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
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

    data class MoveTemplateTask(
        val taskId: TemplateCheckboxId,
        val newParentTaskId: TemplateCheckboxId?,
        override val timestamp: Instant,
        override val commandId: UUID,
        override val templateId: TemplateId
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            val (filteredTasks, movedItem) = template.items.withExtractedTask(taskId)
            val movedItemWithTargetParent = movedItem.copy(parentId = newParentTaskId)
            val items = if (newParentTaskId == null) {
                filteredTasks.plus(movedItemWithTargetParent)
            } else {
                filteredTasks.map { it.withMovedChildRecursive(newParentTaskId, movedItemWithTargetParent) }
            }
            return template.copy(items = items)
        }

        private fun TemplateCheckbox.withMovedChildRecursive(
            parentTask: TemplateCheckboxId,
            childTask: TemplateCheckbox
        ): TemplateCheckbox {
            val updatedChildren = if (id == parentTask) {
                children.plus(childTask)
            } else {
                children.map { it.withMovedChildRecursive(parentTask, childTask) }
            }
            return copy(children = updatedChildren)
        }
    }

    class AddOrReplaceTemplateReminder(
        override val templateId: TemplateId,
        val reminder: Reminder,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            val newReminders = if (template.reminders.any { it.id == reminder.id }) {
                template.reminders.map {
                    if (it.id == reminder.id) {
                        reminder
                    } else {
                        it
                    }
                }
            } else {
                template.reminders.plus(reminder)
            }
            return template.copy(reminders = newReminders)
        }
    }

    class DeleteTemplateReminder(
        override val templateId: TemplateId,
        val reminderId: ReminderId,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            return template.copy(reminders = template.reminders.filterNot { it.id == reminderId })
        }
    }

    class DeleteTemplate(
        override val templateId: TemplateId,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            return template.copy(isRemoved = true)
        }
    }
}

private fun List<TemplateCheckbox>.withExtractedTask(id: TemplateCheckboxId): Pair<List<TemplateCheckbox>, TemplateCheckbox> {
    var movedItem: TemplateCheckbox? = null
    val onItemFoundAndRemoved: (TemplateCheckbox) -> Unit = {
        movedItem = it
    }
    val withExtractedElement = this
        .filter {
            if (it.id == id) {
                movedItem = it
                false
            } else {
                true
            }
        }
        .map {
            it.withoutChild(id, onItemFoundAndRemoved)
        }
    return withExtractedElement to movedItem!!
}

private fun TemplateCheckbox.withoutChild(
    childTaskId: TemplateCheckboxId,
    onItemFoundAndRemoved: (TemplateCheckbox) -> Unit
): TemplateCheckbox {
    val updatedChildren = children
        .firstOrNull {
            it.id == childTaskId
        }
        ?.let {
            onItemFoundAndRemoved(it)
            children.minus(it)
        }
        ?: children.map {
            it.withoutChild(childTaskId, onItemFoundAndRemoved)
        }
    return copy(children = updatedChildren)
}
