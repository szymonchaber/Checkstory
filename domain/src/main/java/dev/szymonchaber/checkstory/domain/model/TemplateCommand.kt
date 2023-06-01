package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
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
        val taskId: TemplateTaskId,
        val parentTaskId: TemplateTaskId?,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            return if (parentTaskId != null) {
                template.copy(tasks = template.tasks.map { it.plusChildCheckboxRecursive(parentTaskId, taskId) })
            } else {
                template.copy(
                    tasks = template.tasks.plus(
                        TemplateTask(
                            taskId,
                            null,
                            "",
                            listOf(),
                            template.tasks.size.toLong(),
                            templateId
                        )
                    )
                )
            }
        }

        private fun TemplateTask.plusChildCheckboxRecursive(
            parentId: TemplateTaskId,
            newCheckboxId: TemplateTaskId
        ): TemplateTask {
            return copy(
                children = if (id == parentId) {
                    children.plus(
                        TemplateTask(
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
        val taskId: TemplateTaskId,
        val newTitle: String,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            return template.copy(tasks = template.tasks
                .map {
                    it.withUpdatedTitleRecursive(taskId, newTitle)
                })
        }

        companion object {

            fun TemplateTask.withUpdatedTitleRecursive(
                idToUpdate: TemplateTaskId,
                newTitle: String,
            ): TemplateTask {
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
        val taskId: TemplateTaskId,
        override val timestamp: Instant,
        override val commandId: UUID = UUID.randomUUID()
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            return template.copy(tasks = template.tasks.withExtractedTask(taskId).first)
        }
    }

    data class UpdateCheckboxPositions(
        val localPositions: Map<TemplateTaskId, Long>,
        override val timestamp: Instant,
        override val commandId: UUID,
        override val templateId: TemplateId
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            return template.copy(
                tasks = template.tasks.map(::updatePosition).sortedBy(TemplateTask::sortPosition)
            )
        }

        private fun updatePosition(templateTask: TemplateTask): TemplateTask {
            val newPosition = localPositions[templateTask.id] ?: templateTask.sortPosition
            return templateTask.copy(
                sortPosition = newPosition,
                children = templateTask.children.map(::updatePosition).sortedBy(TemplateTask::sortPosition)
            )
        }
    }

    data class MoveTemplateTask(
        val taskId: TemplateTaskId,
        val newParentTaskId: TemplateTaskId?,
        override val timestamp: Instant,
        override val commandId: UUID,
        override val templateId: TemplateId
    ) : TemplateCommand {

        override fun applyTo(template: Template): Template {
            val (filteredTasks, movedItem) = template.tasks.withExtractedTask(taskId)
            val movedItemWithTargetParent = movedItem.copy(parentId = newParentTaskId)
            val items = if (newParentTaskId == null) {
                filteredTasks.plus(movedItemWithTargetParent)
            } else {
                filteredTasks.map { it.withMovedChildRecursive(newParentTaskId, movedItemWithTargetParent) }
            }
            return template.copy(tasks = items)
        }

        private fun TemplateTask.withMovedChildRecursive(
            parentTask: TemplateTaskId,
            childTask: TemplateTask
        ): TemplateTask {
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

private fun List<TemplateTask>.withExtractedTask(id: TemplateTaskId): Pair<List<TemplateTask>, TemplateTask> {
    var movedItem: TemplateTask? = null
    val onItemFoundAndRemoved: (TemplateTask) -> Unit = {
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

private fun TemplateTask.withoutChild(
    childTaskId: TemplateTaskId,
    onItemFoundAndRemoved: (TemplateTask) -> Unit
): TemplateTask {
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
