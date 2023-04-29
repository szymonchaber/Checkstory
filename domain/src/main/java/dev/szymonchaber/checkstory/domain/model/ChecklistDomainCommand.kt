package dev.szymonchaber.checkstory.domain.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import java.util.*

sealed interface ChecklistDomainCommand : DomainCommand {

    val checklistId: ChecklistId

    data class CreateChecklistCommand(
        override val checklistId: ChecklistId,
        val templateId: ChecklistTemplateId,
        val title: String,
        val description: String,
        val tasks: List<Checkbox>,
        override val commandId: UUID,
        override val timestamp: Long,
    ) : ChecklistDomainCommand {

        override fun applyTo(checklist: Checklist): Checklist {
            return checklist.copy(
                id = checklistId,
                checklistTemplateId = templateId,
                title = title,
                description = description,
                items = tasks
            )
        }
    }

    data class EditChecklistNotesCommand(
        override val checklistId: ChecklistId,
        val newNotes: String,
        override val commandId: UUID,
        override val timestamp: Long,
    ) : ChecklistDomainCommand {

        override fun applyTo(checklist: Checklist): Checklist {
            return checklist.copy(notes = newNotes)
        }
    }

    data class ChangeTaskCheckedCommand(
        override val checklistId: ChecklistId,
        val taskId: CheckboxId,
        val isChecked: Boolean,
        override val commandId: UUID,
        override val timestamp: Long,
    ) : ChecklistDomainCommand {

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
        override val timestamp: Long,
    ) : ChecklistDomainCommand {

        override fun applyTo(checklist: Checklist): Checklist {
            return checklist.copy(isRemoved = true)
        }
    }

    fun applyTo(checklist: Checklist): Checklist
}

private fun List<Checkbox>.withExtractedTask(id: CheckboxId): Pair<List<Checkbox>, Checkbox> {
    var movedItem: Checkbox? = null
    val onItemFoundAndRemoved: (Checkbox) -> Unit = {
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

private fun Checkbox.withoutChild(
    childTaskId: CheckboxId,
    onItemFoundAndRemoved: (Checkbox) -> Unit
): Checkbox {
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
