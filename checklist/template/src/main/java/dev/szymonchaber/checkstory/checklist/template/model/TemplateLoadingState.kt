package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.TemplateCommand.DeleteTemplateReminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import kotlinx.datetime.Clock
import java.util.*

sealed interface TemplateLoadingState {

    data class Success(
        val originalTemplate: Template,
        val tasks: List<ViewTemplateTask> = originalTemplate.tasks.map {
            ViewTemplateTask.Existing.fromDomainModel(it)
        },
        val updatedTemplate: Template = originalTemplate,
        val mostRecentlyAddedItem: TemplateTaskId? = null,
        val onboardingPlaceholders: OnboardingPlaceholders? = null,
        val isOnboardingTemplate: Boolean = false,
        private val commands: List<TemplateCommand> = listOf()
    ) : TemplateLoadingState {

        val unwrappedTasks = flattenWithNestedLevel()

        val template = commands
            .fold(originalTemplate) { template, templateCommand ->
                templateCommand.applyTo(template)
            }

        fun finalizedCommands(): List<TemplateCommand> {
            val indexedTasks = tasks
                .mapIndexed { index, task ->
                    task.toDomainModel(position = index, templateId = template.id)
                }
            val localPositions = indexedTasks.flatten()
                .associate {
                    it.id to it.sortPosition
                }
            return commands.plus(
                TemplateCommand.UpdateTaskPositions(
                    localPositions,
                    Clock.System.now(),
                    UUID.randomUUID(),
                    template.id
                )
            )
        }

        private fun List<TemplateTask>.flatten(): List<TemplateTask> {
            return flatMap {
                listOf(it) + it.children.flatten()
            }
        }

        private fun flattenWithNestedLevel(): List<Pair<ViewTemplateTask, Int>> {
            val result = mutableListOf<Pair<ViewTemplateTask, Int>>()

            fun visit(task: ViewTemplateTask, level: Int) {
                result.add(Pair(task, level))
                task.children.forEach { child -> visit(child, level + 1) }
            }

            tasks.forEach { task -> visit(task, 0) }
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

        fun plusNewTask(
            title: String,
            placeholderTitle: String? = null,
            id: TemplateTaskId = TemplateTaskId(UUID.randomUUID())
        ): Success {
            val newTask = newTask(title, placeholderTitle, id)
            return copy(
                tasks = tasks.plus(newTask),
                mostRecentlyAddedItem = newTask.id
            )
                .plusCommand(
                    TemplateCommand.AddTemplateTask(
                        template.id,
                        newTask.id,
                        null,
                        Clock.System.now()
                    )
                )
        }

        fun minusTask(task: ViewTemplateTask): Success {
            val filteredTasks =
                tasks
                    .filterNot { it.id == task.id }
                    .map {
                        it.minusChildTaskRecursive(task)
                    }
            return copy(
                tasks = filteredTasks,
                mostRecentlyAddedItem = null
            ).plusCommand(
                TemplateCommand.DeleteTemplateTask(
                    originalTemplate.id,
                    task.id,
                    Clock.System.now()
                )
            )
        }

        fun plusChildTask(
            parentId: TemplateTaskId,
            newId: TemplateTaskId = TemplateTaskId(UUID.randomUUID()),
            placeholderTitle: String? = null
        ): Success {
            return copy(
                tasks = tasks.map {
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

        fun changeTaskTitle(task: ViewTemplateTask, title: String): Success {
            return copy(
                tasks = tasks.map {
                    it.withUpdatedTitleRecursive(task, title)
                }
            )
                .plusCommand(
                    TemplateCommand.RenameTemplateTask(
                        templateId = template.id,
                        taskId = task.id,
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

        fun withNewSiblingMovedBelow(below: TemplateTaskId): TemplateLoadingState {
            val newCheckbox = newTask()
            val parentId = findParentId(tasks, below)
            return copy(
                tasks = tasks.withSiblingBelow(below, newCheckbox),
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

        private fun findParentId(tasks: List<ViewTemplateTask>, id: TemplateTaskId): TemplateTaskId? {
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
            below: TemplateTaskId,
            movedCheckboxId: TemplateTaskId
        ): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(movedCheckboxId)
            val newTasks = filteredTasks.withSiblingBelow(below, movedItem)
            return copy(tasks = newTasks)
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

        private fun List<ViewTemplateTask>.withSiblingBelow(
            below: TemplateTaskId,
            movedItem: ViewTemplateTask
        ): List<ViewTemplateTask> {
            return if (any { it.id == below }) {
                val newTaskIndex = indexOfFirst { it.id == below } + 1
                withTaskAtIndex(movedItem.updateParentId(null), newTaskIndex)
            } else {
                map {
                    it.withMovedSiblingRecursive(below, movedItem)
                }
            }
        }

        fun withNewChildMovedBelow(below: TemplateTaskId): TemplateLoadingState {
            val newItem = newTask()
            return copy(
                tasks = tasks.withChildBelow(below, newItem),
                mostRecentlyAddedItem = newItem.id
            ).plusCommand(
                TemplateCommand.AddTemplateTask(
                    template.id,
                    newItem.id,
                    TemplateTaskId(below.id),
                    Clock.System.now()
                )
            )
        }

        fun withChildMovedBelow(
            below: TemplateTaskId,
            childTaskId: TemplateTaskId
        ): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(childTaskId)
            val newTasks = filteredTasks.withChildBelow(below, movedItem)
            return copy(tasks = newTasks)
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

        private fun List<ViewTemplateTask>.withChildBelow(
            below: TemplateTaskId,
            movedItem: ViewTemplateTask
        ): List<ViewTemplateTask> {
            return map {
                it.withMovedChildRecursive(below, movedItem)
            }
        }

        fun withTaskMovedToTop(taskId: TemplateTaskId): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(taskId)
            return copy(
                tasks = filteredTasks.withTaskAtIndex(movedItem.updateParentId(null), 0)
            ).plusCommand(
                TemplateCommand.MoveTemplateTask(
                    taskId = taskId,
                    newParentTaskId = null,
                    timestamp = Clock.System.now(),
                    commandId = UUID.randomUUID(),
                    templateId = originalTemplate.id
                )
            )
        }

        fun withNewTaskAtTop(): TemplateLoadingState {
            val newItem = newTask()
            return copy(
                tasks = tasks.withTaskAtIndex(newItem, 0),
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

        fun withTaskMovedToBottom(checkboxId: TemplateTaskId): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(checkboxId)
            return copy(
                tasks = filteredTasks.withTaskAtIndex(movedItem.updateParentId(null), filteredTasks.size)
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

        fun withNewTaskAtBottom(): TemplateLoadingState {
            val newItem = newTask()
            return copy(
                tasks = tasks.withTaskAtIndex(newItem, tasks.size),
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

        private fun withExtractedTask(id: TemplateTaskId): Pair<List<ViewTemplateTask>, ViewTemplateTask> {
            var movedItem: ViewTemplateTask? = null
            val onItemFoundAndRemoved: (ViewTemplateTask) -> Unit = {
                movedItem = it
            }
            val withExtractedElement = tasks
                .filter {
                    if (it.id == TemplateTaskId(id.id)) {
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

        private fun newTask(
            title: String = "",
            placeholderTitle: String? = null,
            id: TemplateTaskId = TemplateTaskId(UUID.randomUUID())
        ): ViewTemplateTask.New {
            return ViewTemplateTask.New(
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

        fun getAllAncestorsOf(target: TemplateTaskId): List<TemplateTaskId> {
            val ancestors = mutableListOf<TemplateTaskId>()
            var currentTarget = target
            while (true) {
                findParentId(tasks, currentTarget)?.let {
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

fun List<ViewTemplateTask>.withTaskAtIndex(
    checkbox: ViewTemplateTask,
    index: Int
): List<ViewTemplateTask> {
    return take(index) + checkbox + drop(index)
}

// TODO not sure if this is still required
fun List<ViewTemplateTask>.updateParentIds(parentId: TemplateTaskId? = null): List<ViewTemplateTask> {
    return map {
        it.abstractCopy(parentId = parentId, children = it.children.updateParentIds(it.id))
    }
}
