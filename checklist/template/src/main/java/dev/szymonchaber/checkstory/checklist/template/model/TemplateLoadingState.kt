package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.checklist.template.ViewTemplateCheckboxId
import dev.szymonchaber.checkstory.checklist.template.ViewTemplateCheckboxKey
import dev.szymonchaber.checkstory.checklist.template.viewId
import dev.szymonchaber.checkstory.checklist.template.viewKey
import dev.szymonchaber.checkstory.domain.model.EditTemplateDomainEvent
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import java.util.*

sealed interface TemplateLoadingState {

    data class Success(
        val originalChecklistTemplate: ChecklistTemplate,
        val checkboxes: List<ViewTemplateCheckbox>,
        val checkboxesToDelete: List<TemplateCheckbox> = listOf(),
        val remindersToDelete: List<Reminder> = listOf(),
        val checklistTemplate: ChecklistTemplate = originalChecklistTemplate,
        val mostRecentlyAddedItem: ViewTemplateCheckboxId? = null,
        val onboardingPlaceholders: OnboardingPlaceholders? = null,
        val isOnboardingTemplate: Boolean = false,
        val events: List<EditTemplateDomainEvent> = listOf()
    ) : TemplateLoadingState {

        val unwrappedCheckboxes = flattenWithNestedLevel()

        private fun flattenWithNestedLevel(): List<Pair<ViewTemplateCheckbox, Int>> {
            val result = mutableListOf<Pair<ViewTemplateCheckbox, Int>>()

            fun visit(checkbox: ViewTemplateCheckbox, level: Int) {
                result.add(Pair(checkbox, level))
                checkbox.children.forEach { child -> visit(child, level + 1) }
            }

            checkboxes.forEach { checkbox -> visit(checkbox, 0) }
            return result
        }


        fun isChanged(): Boolean {
            return originalChecklistTemplate != checklistTemplate
                    || originalChecklistTemplate.items != checkboxes.mapIndexed { index, checkbox ->
                checkbox.toDomainModel(position = index)
            }
                    || checkboxesToDelete.isNotEmpty()
                    || remindersToDelete.isNotEmpty()
        }

        fun updateTemplate(block: ChecklistTemplate.() -> ChecklistTemplate): Success {
            return copy(checklistTemplate = checklistTemplate.block())
        }

        fun plusNewCheckbox(title: String): Pair<Success, TemplateCheckboxId> {
            val newCheckbox = newCheckbox(title)
            return copy(
                checkboxes = checkboxes.plus(newCheckbox),
                mostRecentlyAddedItem = newCheckbox.viewId
            ) to newCheckbox.id
        }

        fun plusNestedCheckbox(placeholderTitle: String, childrenTitles: List<CheckboxToChildren>): Success {
            val parent = newCheckbox(placeholderTitle = placeholderTitle)
            return copy(
                checkboxes = checkboxes.plus(parent)
            ).let {
                childrenTitles.fold(it) { acc, (childTitle, nestedChildren) ->
                    acc.plusChildCheckboxNested(parent.viewKey, childTitle, nestedChildren)
                }
            }
        }

        fun minusCheckbox(checkbox: ViewTemplateCheckbox): Success {
            val filteredCheckboxes =
                checkboxes
                    .filterNot { it.viewKey == checkbox.viewKey }
                    .map {
                        it.minusChildCheckboxRecursive(checkbox)
                    }
            val shouldDeleteFromDatabase = checkbox is ViewTemplateCheckbox.Existing
            val updatedCheckboxesToDelete = if (shouldDeleteFromDatabase) {
                checkboxesToDelete.plus(checkbox.toDomainModel(position = 0))
            } else {
                checkboxesToDelete
            }

            val updatedEvents = if (shouldDeleteFromDatabase) {
                events.plus(
                    EditTemplateDomainEvent.DeleteTemplateTask(
                        originalChecklistTemplate.id,
                        checkbox.id,
                        System.currentTimeMillis()
                    )
                )
            } else {
                events
            }
            return copy(
                checkboxes = filteredCheckboxes,
                checkboxesToDelete = updatedCheckboxesToDelete,
                mostRecentlyAddedItem = null,
                events = updatedEvents
            )
        }

        fun plusChildCheckbox(parentId: ViewTemplateCheckboxKey): Success {
            var addedItem: ViewTemplateCheckboxId? = null
            val onItemActuallyAdded: (ViewTemplateCheckboxId) -> Unit = {
                if (addedItem != null) {
                    error("Attempted two sub-task additions where there should be one!")
                }
                addedItem = it
            }
            return copy(
                checkboxes = checkboxes.map {
                    it.plusChildCheckboxRecursive(parentId, onItemActuallyAdded)
                },
                mostRecentlyAddedItem = addedItem
            )
        }

        private fun plusChildCheckboxNested(
            parentId: ViewTemplateCheckboxKey,
            placeholderTitle: String = "",
            children: List<CheckboxToChildren> = listOf()
        ): Success {
            return copy(
                checkboxes = checkboxes.map {
                    it.plusNestedChildCheckboxRecursive(parentId, placeholderTitle, children)
                }
            )
        }

        fun changeCheckboxTitle(checkbox: ViewTemplateCheckbox, title: String): Success {
            return copy(
                checkboxes = checkboxes.map {
                    it.withUpdatedTitleRecursive(checkbox, title)
                }
            )
        }

        fun plusReminder(reminder: Reminder): Success {
            return updateTemplate {
                copy(reminders = reminders.plus(reminder))
            }
        }

        fun minusReminder(reminder: Reminder): TemplateLoadingState {
            val updatedRemindersToDelete = if (reminder.isStored) {
                remindersToDelete.plus(reminder)
            } else {
                remindersToDelete
            }
            return updateTemplate {
                copy(reminders = reminders.minus(reminder))
            }.copy(remindersToDelete = updatedRemindersToDelete)
        }

        fun withNewSiblingMovedBelow(below: ViewTemplateCheckboxKey): TemplateLoadingState {
            val newCheckbox = newCheckbox()
            return copy(
                checkboxes = checkboxes.withSiblingBelow(below, newCheckbox),
                mostRecentlyAddedItem = newCheckbox.viewId
            )
        }

        fun withSiblingMovedBelow(
            below: ViewTemplateCheckboxKey,
            movedCheckboxKey: ViewTemplateCheckboxKey
        ): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(movedCheckboxKey)
            return copy(checkboxes = filteredTasks.withSiblingBelow(below, movedItem))
        }

        private fun List<ViewTemplateCheckbox>.withSiblingBelow(
            below: ViewTemplateCheckboxKey,
            movedItem: ViewTemplateCheckbox
        ): List<ViewTemplateCheckbox> {
            return if (any { it.viewKey == below }) {
                val newTaskIndex = indexOfFirst { it.viewKey == below } + 1
                withCheckboxAtIndex(movedItem.updateParentKey(null), newTaskIndex)
            } else {
                map {
                    it.withMovedSiblingRecursive(below, movedItem)
                }
            }
        }

        fun withNewChildMovedBelow(below: ViewTemplateCheckboxKey): TemplateLoadingState {
            val newItem = newCheckbox()
            return copy(
                checkboxes = checkboxes.withChildBelow(below, newItem),
                mostRecentlyAddedItem = newItem.viewId
            )
        }

        fun withChildMovedBelow(
            below: ViewTemplateCheckboxKey,
            newChildKey: ViewTemplateCheckboxKey
        ): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(newChildKey)
            return copy(checkboxes = filteredTasks.withChildBelow(below, movedItem))
        }

        private fun List<ViewTemplateCheckbox>.withChildBelow(
            below: ViewTemplateCheckboxKey,
            movedItem: ViewTemplateCheckbox
        ): List<ViewTemplateCheckbox> {
            return map {
                it.withMovedChildRecursive(below, movedItem)
            }
        }

        fun withCheckboxMovedToTop(checkboxKey: ViewTemplateCheckboxKey): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(checkboxKey)
            return copy(
                checkboxes = filteredTasks.withCheckboxAtIndex(movedItem.updateParentKey(null), 0)
            )
        }

        fun withNewCheckboxAtTop(): TemplateLoadingState {
            val newItem = newCheckbox()
            return copy(
                checkboxes = checkboxes.withCheckboxAtIndex(newItem, 0),
                mostRecentlyAddedItem = newItem.viewId
            )
        }

        fun withCheckboxMovedToBottom(checkboxKey: ViewTemplateCheckboxKey): TemplateLoadingState {
            val (filteredTasks, movedItem) = withExtractedTask(checkboxKey)
            return copy(
                checkboxes = filteredTasks.withCheckboxAtIndex(movedItem.updateParentKey(null), filteredTasks.size)
            )
        }

        fun withNewCheckboxAtBottom(): TemplateLoadingState {
            val newItem = newCheckbox()
            return copy(
                checkboxes = checkboxes.withCheckboxAtIndex(newItem, checkboxes.size),
                mostRecentlyAddedItem = newItem.viewId
            )
        }

        private fun withExtractedTask(viewKey: ViewTemplateCheckboxKey): Pair<List<ViewTemplateCheckbox>, ViewTemplateCheckbox> {
            var movedItem: ViewTemplateCheckbox? = null
            val onItemFoundAndRemoved: (ViewTemplateCheckbox) -> Unit = {
                movedItem = it
            }
            val withExtractedElement = checkboxes
                .filter {
                    if (it.viewId == viewKey.viewId) {
                        movedItem = it
                        false
                    } else {
                        true
                    }
                }
                .map {
                    it.withoutChild(viewKey, onItemFoundAndRemoved)
                }
            return withExtractedElement to movedItem!!
        }

        private fun newCheckbox(title: String = "", placeholderTitle: String? = null): ViewTemplateCheckbox.New {
            return ViewTemplateCheckbox.New(
                id = TemplateCheckboxId(UUID.randomUUID()),
                parentViewKey = null,
                isParent = true,
                title = title,
                children = listOf(),
                isLastChild = true,
                placeholderTitle = placeholderTitle
            )
        }

        fun plusEvent(event: EditTemplateDomainEvent): Success {
            return copy(events = events.plus(event))
        }

        companion object {

            fun fromTemplate(checklistTemplate: ChecklistTemplate): Success {
                return Success(
                    checklistTemplate,
                    checklistTemplate.items.map { ViewTemplateCheckbox.Existing.fromDomainModel(it) },
                    listOf(),
                    listOf()
                )
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

fun List<ViewTemplateCheckbox>.updateParentKeys(parentKey: ViewTemplateCheckboxKey? = null): List<ViewTemplateCheckbox> {
    return map {
        val updatedCheckbox = it.abstractCopy(parentViewKey = parentKey)
        updatedCheckbox.abstractCopy(children = updatedCheckbox.children.updateParentKeys(updatedCheckbox.viewKey))
    }
}
