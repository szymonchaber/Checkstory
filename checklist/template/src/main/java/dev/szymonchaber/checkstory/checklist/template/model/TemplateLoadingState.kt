package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand.DeleteTemplateReminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import kotlinx.datetime.Clock
import java.util.*

sealed interface TemplateLoadingState {

    data class Success(
        val originalChecklistTemplate: ChecklistTemplate,
        val checkboxes: List<ViewTemplateCheckbox> = originalChecklistTemplate.items.map {
            ViewTemplateCheckbox.Existing.fromDomainModel(it)
        },
        val updatedChecklistTemplate: ChecklistTemplate = originalChecklistTemplate,
        val mostRecentlyAddedItem: TemplateCheckboxId? = null,
        val onboardingPlaceholders: OnboardingPlaceholders? = null,
        val isOnboardingTemplate: Boolean = false,
        private val commands: List<TemplateDomainCommand> = listOf()
    ) : TemplateLoadingState {

        val unwrappedCheckboxes = flattenWithNestedLevel()

        val checklistTemplate = commands
            .fold(originalChecklistTemplate) { template, templateDomainCommand ->
                templateDomainCommand.applyTo(template)
            }

        fun finalizedCommands(): List<TemplateDomainCommand> {
            val indexedCheckboxes = checkboxes
                .mapIndexed { index, checkbox ->
                    checkbox.toDomainModel(position = index, templateId = checklistTemplate.id)
                }
            val localPositions = indexedCheckboxes.flatten()
                .associate {
                    it.id to it.sortPosition
                }
            return commands.plus(
                TemplateDomainCommand.UpdateCheckboxPositions(
                    localPositions,
                    Clock.System.now(),
                    UUID.randomUUID(),
                    checklistTemplate.id
                )
            )
        }

        private fun List<TemplateCheckbox>.flatten(): List<TemplateCheckbox> {
            return flatMap {
                listOf(it) + it.children.flatten()
            }
        }

        private fun flattenWithNestedLevel(): List<Pair<ViewTemplateCheckbox, Int>> {
            val result = mutableListOf<Pair<ViewTemplateCheckbox, Int>>()

            fun visit(checkbox: ViewTemplateCheckbox, level: Int) {
                result.add(Pair(checkbox, level))
                checkbox.children.forEach { child -> visit(child, level + 1) }
            }

            checkboxes.forEach { checkbox -> visit(checkbox, 0) }
            return result
        }

        fun isChanged() = commands.isNotEmpty()

        fun withNewTitle(newTitle: String): Success {
            return plusCommand(
                TemplateDomainCommand.RenameTemplate(
                    checklistTemplate.id, newTitle, Clock.System.now()
                )
            )
        }

        fun withNewDescription(newDescription: String): Success {
            return plusCommand(
                TemplateDomainCommand.ChangeTemplateDescription(
                    checklistTemplate.id,
                    newDescription,
                    Clock.System.now()
                )
            )
        }

        fun plusNewCheckbox(
            title: String,
            placeholderTitle: String? = null,
            id: TemplateCheckboxId = TemplateCheckboxId(UUID.randomUUID())
        ): Success {
            val newCheckbox = newCheckbox(title, placeholderTitle, id)
            return copy(
                checkboxes = checkboxes.plus(newCheckbox),
                mostRecentlyAddedItem = newCheckbox.id
            )
                .plusCommand(
                    TemplateDomainCommand.AddTemplateTask(
                        checklistTemplate.id,
                        newCheckbox.id,
                        null,
                        Clock.System.now()
                    )
                )
        }

        fun minusCheckbox(checkbox: ViewTemplateCheckbox): Success {
            val filteredCheckboxes =
                checkboxes
                    .filterNot { it.id == checkbox.id }
                    .map {
                        it.minusChildCheckboxRecursive(checkbox)
                    }
            return copy(
                checkboxes = filteredCheckboxes,
                mostRecentlyAddedItem = null
            ).plusCommand(
                TemplateDomainCommand.DeleteTemplateTask(
                    originalChecklistTemplate.id,
                    checkbox.id,
                    Clock.System.now()
                )
            )
        }

        fun plusChildCheckbox(
            parentId: TemplateCheckboxId,
            newId: TemplateCheckboxId = TemplateCheckboxId(UUID.randomUUID()),
            placeholderTitle: String? = null
        ): Success {
            return copy(
                checkboxes = checkboxes.map {
                    it.plusChildCheckboxRecursive(parentId, newId, placeholderTitle)
                },
                mostRecentlyAddedItem = newId
            )
                .plusCommand(
                    TemplateDomainCommand.AddTemplateTask(
                        templateId = originalChecklistTemplate.id,
                        taskId = newId,
                        parentTaskId = parentId,
                        Clock.System.now()
                    )
                )
        }

        fun changeCheckboxTitle(checkbox: ViewTemplateCheckbox, title: String): Success {
            return copy(
                checkboxes = checkboxes.map {
                    it.withUpdatedTitleRecursive(checkbox, title)
                }
            )
                .plusCommand(
                    TemplateDomainCommand.RenameTemplateTask(
                        templateId = checklistTemplate.id,
                        taskId = checkbox.id,
                        newTitle = title,
                        timestamp = Clock.System.now()
                    )
                )
        }

        fun withUpdatedReminder(reminder: Reminder): Success {
            return plusCommand(
                TemplateDomainCommand.AddOrReplaceTemplateReminder(
                    checklistTemplate.id,
                    reminder,
                    Clock.System.now()
                )
            )
        }

        fun minusReminder(reminder: Reminder): TemplateLoadingState {
            return plusCommand(
                DeleteTemplateReminder(
                    checklistTemplate.id,
                    reminder.id,
                    Clock.System.now()
                )
            )
        }

        fun withNewSiblingMovedBelow(below: TemplateCheckboxId): TemplateLoadingState {
            val newCheckbox = newCheckbox()
            val parentId = findParentId(checkboxes, below)
            return copy(
                checkboxes = checkboxes.withSiblingBelow(below, newCheckbox),
                mostRecentlyAddedItem = newCheckbox.id
            ).plusCommand(
                TemplateDomainCommand.AddTemplateTask(
                    checklistTemplate.id,
                    newCheckbox.id,
                    parentId,
                    Clock.System.now()
                )
            )
        }

        private fun findParentId(tasks: List<ViewTemplateCheckbox>, id: TemplateCheckboxId): TemplateCheckboxId? {
            tasks.forEach { task ->
                if (task.id == id) {
                    return task.parentId
                }
                val parentId = findParentId(task.children, id)
                if (parentId != null) {
                    return parentId
                }
            }
            return null
        }

        fun withSiblingMovedBelow(
            below: TemplateCheckboxId,
            movedCheckboxId: TemplateCheckboxId
        ): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(movedCheckboxId)
            val newTasks = filteredTasks.withSiblingBelow(below, movedItem)
            return copy(checkboxes = newTasks)
                .plusCommand(
                    TemplateDomainCommand.MoveTemplateTask(
                        taskId = movedCheckboxId,
                        newParentTaskId = findParentId(newTasks, movedCheckboxId),
                        timestamp = Clock.System.now(),
                        commandId = UUID.randomUUID(),
                        templateId = originalChecklistTemplate.id
                    )
                )
        }

        private fun List<ViewTemplateCheckbox>.withSiblingBelow(
            below: TemplateCheckboxId,
            movedItem: ViewTemplateCheckbox
        ): List<ViewTemplateCheckbox> {
            return if (any { it.id == below }) {
                val newTaskIndex = indexOfFirst { it.id == below } + 1
                withCheckboxAtIndex(movedItem.updateParentId(null), newTaskIndex)
            } else {
                map {
                    it.withMovedSiblingRecursive(below, movedItem)
                }
            }
        }

        fun withNewChildMovedBelow(below: TemplateCheckboxId): TemplateLoadingState {
            val newItem = newCheckbox()
            return copy(
                checkboxes = checkboxes.withChildBelow(below, newItem),
                mostRecentlyAddedItem = newItem.id
            ).plusCommand(
                TemplateDomainCommand.AddTemplateTask(
                    checklistTemplate.id,
                    newItem.id,
                    TemplateCheckboxId(below.id),
                    Clock.System.now()
                )
            )
        }

        fun withChildMovedBelow(
            below: TemplateCheckboxId,
            childTaskId: TemplateCheckboxId
        ): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(childTaskId)
            val newTasks = filteredTasks.withChildBelow(below, movedItem)
            return copy(checkboxes = newTasks)
                .plusCommand(
                    TemplateDomainCommand.MoveTemplateTask(
                        taskId = childTaskId,
                        newParentTaskId = findParentId(newTasks, childTaskId),
                        timestamp = Clock.System.now(),
                        commandId = UUID.randomUUID(),
                        templateId = originalChecklistTemplate.id
                    )
                )
        }

        private fun List<ViewTemplateCheckbox>.withChildBelow(
            below: TemplateCheckboxId,
            movedItem: ViewTemplateCheckbox
        ): List<ViewTemplateCheckbox> {
            return map {
                it.withMovedChildRecursive(below, movedItem)
            }
        }

        fun withCheckboxMovedToTop(checkboxId: TemplateCheckboxId): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(checkboxId)
            return copy(
                checkboxes = filteredTasks.withCheckboxAtIndex(movedItem.updateParentId(null), 0)
            ).plusCommand(
                TemplateDomainCommand.MoveTemplateTask(
                    taskId = checkboxId,
                    newParentTaskId = null,
                    timestamp = Clock.System.now(),
                    commandId = UUID.randomUUID(),
                    templateId = originalChecklistTemplate.id
                )
            )
        }

        fun withNewCheckboxAtTop(): TemplateLoadingState {
            val newItem = newCheckbox()
            return copy(
                checkboxes = checkboxes.withCheckboxAtIndex(newItem, 0),
                mostRecentlyAddedItem = newItem.id
            ).plusCommand(
                TemplateDomainCommand.AddTemplateTask(
                    checklistTemplate.id,
                    newItem.id,
                    null,
                    Clock.System.now()
                )
            )
        }

        fun withCheckboxMovedToBottom(checkboxId: TemplateCheckboxId): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(checkboxId)
            return copy(
                checkboxes = filteredTasks.withCheckboxAtIndex(movedItem.updateParentId(null), filteredTasks.size)
            ).plusCommand(
                TemplateDomainCommand.MoveTemplateTask(
                    taskId = checkboxId,
                    newParentTaskId = null,
                    timestamp = Clock.System.now(),
                    commandId = UUID.randomUUID(),
                    templateId = originalChecklistTemplate.id
                )
            )
        }

        fun withNewCheckboxAtBottom(): TemplateLoadingState {
            val newItem = newCheckbox()
            return copy(
                checkboxes = checkboxes.withCheckboxAtIndex(newItem, checkboxes.size),
                mostRecentlyAddedItem = newItem.id
            ).plusCommand(
                TemplateDomainCommand.AddTemplateTask(
                    checklistTemplate.id,
                    newItem.id,
                    null,
                    Clock.System.now()
                )
            )
        }

        private fun withExtractedTask(id: TemplateCheckboxId): Pair<List<ViewTemplateCheckbox>, ViewTemplateCheckbox> {
            var movedItem: ViewTemplateCheckbox? = null
            val onItemFoundAndRemoved: (ViewTemplateCheckbox) -> Unit = {
                movedItem = it
            }
            val withExtractedElement = checkboxes
                .filter {
                    if (it.id == TemplateCheckboxId(id.id)) {
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

        private fun newCheckbox(
            title: String = "",
            placeholderTitle: String? = null,
            id: TemplateCheckboxId = TemplateCheckboxId(UUID.randomUUID())
        ): ViewTemplateCheckbox.New {
            return ViewTemplateCheckbox.New(
                id = id,
                parentId = null,
                title = title,
                children = listOf(),
                placeholderTitle = placeholderTitle
            )
        }

        private fun plusCommand(event: TemplateDomainCommand): Success {
            return copy(commands = commands.plus(event))
        }

        fun getAllAncestorsOf(target: TemplateCheckboxId): List<TemplateCheckboxId> {
            val ancestors = mutableListOf<TemplateCheckboxId>()
            var currentTarget = target
            while (true) {
                findParentId(checkboxes, currentTarget)?.let {
                    ancestors.add(it)
                    currentTarget = it
                } ?: break
            }
            return ancestors
        }

        fun markDeleted(): Success {
            return plusCommand(
                TemplateDomainCommand.DeleteTemplate(checklistTemplate.id, Clock.System.now())
            )
        }

        companion object {

            fun fromTemplate(checklistTemplate: ChecklistTemplate): Success {
                return Success(originalChecklistTemplate = checklistTemplate)
            }
        }
    }

    object Loading : TemplateLoadingState
}

data class OnboardingPlaceholders(val title: String, val description: String)

fun List<ViewTemplateCheckbox>.withCheckboxAtIndex(
    checkbox: ViewTemplateCheckbox,
    index: Int
): List<ViewTemplateCheckbox> {
    return take(index) + checkbox + drop(index)
}

// TODO not sure if this is still required
fun List<ViewTemplateCheckbox>.updateParentIds(parentId: TemplateCheckboxId? = null): List<ViewTemplateCheckbox> {
    return map {
        it.abstractCopy(parentId = parentId, children = it.children.updateParentIds(it.id))
    }
}
