package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.TemplateCommand.DeleteTemplateReminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import kotlinx.datetime.Clock
import java.util.*

sealed interface TemplateLoadingState {

    data class Success(
        val originalTemplate: Template,
        val checkboxes: List<ViewTemplateCheckbox> = originalTemplate.items.map {
            ViewTemplateCheckbox.Existing.fromDomainModel(it)
        },
        val updatedTemplate: Template = originalTemplate,
        val mostRecentlyAddedItem: TemplateCheckboxId? = null,
        val onboardingPlaceholders: OnboardingPlaceholders? = null,
        val isOnboardingTemplate: Boolean = false,
        private val commands: List<TemplateCommand> = listOf()
    ) : TemplateLoadingState {

        val unwrappedCheckboxes = flattenWithNestedLevel()

        val template = commands
            .fold(originalTemplate) { template, templateCommand ->
                templateCommand.applyTo(template)
            }

        fun finalizedCommands(): List<TemplateCommand> {
            val indexedCheckboxes = checkboxes
                .mapIndexed { index, checkbox ->
                    checkbox.toDomainModel(position = index, templateId = template.id)
                }
            val localPositions = indexedCheckboxes.flatten()
                .associate {
                    it.id to it.sortPosition
                }
            return commands.plus(
                TemplateCommand.UpdateCheckboxPositions(
                    localPositions,
                    Clock.System.now(),
                    UUID.randomUUID(),
                    template.id
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
                TemplateCommand.RenameTemplate(
                    template.id, newTitle, Clock.System.now()
                )
            )
        }

        fun withNewDescription(newDescription: String): Success {
            return plusCommand(
                TemplateCommand.ChangeTemplateDescription(
                    template.id,
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
                    TemplateCommand.AddTemplateTask(
                        template.id,
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
                TemplateCommand.DeleteTemplateTask(
                    originalTemplate.id,
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
                    TemplateCommand.AddTemplateTask(
                        templateId = originalTemplate.id,
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
                    TemplateCommand.RenameTemplateTask(
                        templateId = template.id,
                        taskId = checkbox.id,
                        newTitle = title,
                        timestamp = Clock.System.now()
                    )
                )
        }

        fun withUpdatedReminder(reminder: Reminder): Success {
            return plusCommand(
                TemplateCommand.AddOrReplaceTemplateReminder(
                    template.id,
                    reminder,
                    Clock.System.now()
                )
            )
        }

        fun minusReminder(reminder: Reminder): TemplateLoadingState {
            return plusCommand(
                DeleteTemplateReminder(
                    template.id,
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
                TemplateCommand.AddTemplateTask(
                    template.id,
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
                    TemplateCommand.MoveTemplateTask(
                        taskId = movedCheckboxId,
                        newParentTaskId = findParentId(newTasks, movedCheckboxId),
                        timestamp = Clock.System.now(),
                        commandId = UUID.randomUUID(),
                        templateId = originalTemplate.id
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
                TemplateCommand.AddTemplateTask(
                    template.id,
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
                    TemplateCommand.MoveTemplateTask(
                        taskId = childTaskId,
                        newParentTaskId = findParentId(newTasks, childTaskId),
                        timestamp = Clock.System.now(),
                        commandId = UUID.randomUUID(),
                        templateId = originalTemplate.id
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
                TemplateCommand.MoveTemplateTask(
                    taskId = checkboxId,
                    newParentTaskId = null,
                    timestamp = Clock.System.now(),
                    commandId = UUID.randomUUID(),
                    templateId = originalTemplate.id
                )
            )
        }

        fun withNewCheckboxAtTop(): TemplateLoadingState {
            val newItem = newCheckbox()
            return copy(
                checkboxes = checkboxes.withCheckboxAtIndex(newItem, 0),
                mostRecentlyAddedItem = newItem.id
            ).plusCommand(
                TemplateCommand.AddTemplateTask(
                    template.id,
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
                TemplateCommand.MoveTemplateTask(
                    taskId = checkboxId,
                    newParentTaskId = null,
                    timestamp = Clock.System.now(),
                    commandId = UUID.randomUUID(),
                    templateId = originalTemplate.id
                )
            )
        }

        fun withNewCheckboxAtBottom(): TemplateLoadingState {
            val newItem = newCheckbox()
            return copy(
                checkboxes = checkboxes.withCheckboxAtIndex(newItem, checkboxes.size),
                mostRecentlyAddedItem = newItem.id
            ).plusCommand(
                TemplateCommand.AddTemplateTask(
                    template.id,
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

        private fun plusCommand(event: TemplateCommand): Success {
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
                TemplateCommand.DeleteTemplate(template.id, Clock.System.now())
            )
        }

        companion object {

            fun fromTemplate(template: Template): Success {
                return Success(originalTemplate = template)
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
