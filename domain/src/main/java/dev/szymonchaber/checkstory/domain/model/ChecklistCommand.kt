package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.fill.TaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

sealed interface ChecklistCommand : Command {

    val checklistId: ChecklistId

    data class CreateChecklistCommand(
        override val checklistId: ChecklistId,
        val templateId: TemplateId,
        val title: String,
        val description: String,
        val tasks: List<Task>,
        override val commandId: UUID,
        override val timestamp: Instant,
        val notes: String? = null
    ) : ChecklistCommand {

        override fun applyTo(checklist: Checklist): Checklist {
            val localDateTime = LocalDateTime.ofInstant(timestamp.toJavaInstant(), ZoneId.systemDefault())
            return checklist.copy(
                id = checklistId,
                templateId = templateId,
                title = title,
                description = description,
                items = tasks,
                createdAt = localDateTime
            )
        }
    }

    data class EditChecklistNotesCommand(
        override val checklistId: ChecklistId,
        val newNotes: String,
        override val commandId: UUID,
        override val timestamp: Instant,
    ) : ChecklistCommand {

        override fun applyTo(checklist: Checklist): Checklist {
            return checklist.copy(notes = newNotes)
        }
    }

    data class ChangeTaskCheckedCommand(
        override val checklistId: ChecklistId,
        val taskId: TaskId,
        val isChecked: Boolean,
        override val commandId: UUID,
        override val timestamp: Instant,
    ) : ChecklistCommand {

        override fun applyTo(checklist: Checklist): Checklist {
            val newItems = checklist.items.map {
                it.withUpdatedIsCheckedRecursive(taskId, isChecked)
            }
            return checklist.copy(items = newItems)
        }
    }

    data class DeleteChecklistCommand(
        override val checklistId: ChecklistId,
        override val commandId: UUID,
        override val timestamp: Instant,
    ) : ChecklistCommand {

        override fun applyTo(checklist: Checklist): Checklist {
            return checklist.copy(isRemoved = true)
        }
    }

    fun applyTo(checklist: Checklist): Checklist
}

private fun List<Task>.withExtractedTask(id: TaskId): Pair<List<Task>, Task> {
    var movedItem: Task? = null
    val onItemFoundAndRemoved: (Task) -> Unit = {
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

private fun Task.withoutChild(
    childTaskId: TaskId,
    onItemFoundAndRemoved: (Task) -> Unit
): Task {
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
