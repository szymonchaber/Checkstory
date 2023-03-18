package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.checklist.template.ViewTemplateCheckboxKey
import dev.szymonchaber.checkstory.checklist.template.viewKey
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder

sealed interface TemplateLoadingState {

    data class Success(
        val originalChecklistTemplate: ChecklistTemplate,
        val checkboxes: List<ViewTemplateCheckbox>,
        val checkboxesToDelete: List<TemplateCheckbox>,
        val remindersToDelete: List<Reminder>,
        val checklistTemplate: ChecklistTemplate = originalChecklistTemplate,
        val mostRecentlyAddedItem: ViewTemplateCheckboxKey? = null,
        val onboardingPlaceholders: OnboardingPlaceholders? = null,
        val isOnboardingTemplate: Boolean = false
    ) : TemplateLoadingState {


        private val unwrappedCheckboxes = flattenWithNestedLevel()

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

        fun plusNewCheckbox(title: String): Success {
            val newCheckbox = ViewTemplateCheckbox.New(
                TemplateCheckboxId(checkboxes.size.toLong()),
                null,
                true,
                title,
                listOf(),
                true
            )
            return copy(
                checkboxes = checkboxes.plus(
                    newCheckbox
                ),
                mostRecentlyAddedItem = newCheckbox.viewKey
            )
        }

        fun plusNestedCheckbox(placeholderTitle: String, childrenTitles: List<CheckboxToChildren>): Success {
            val parent = ViewTemplateCheckbox.New(
                id = TemplateCheckboxId(checkboxes.size.toLong()),
                parentViewKey = null,
                isParent = true,
                title = "",
                children = listOf(),
                isLastChild = true,
                placeholderTitle = placeholderTitle
            )
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
            return copy(
                checkboxes = filteredCheckboxes,
                checkboxesToDelete = updatedCheckboxesToDelete,
                mostRecentlyAddedItem = null
            )
        }

        fun plusChildCheckbox(parentId: ViewTemplateCheckboxKey): Success {
            var addedItem: ViewTemplateCheckboxKey? = null
            val onItemActuallyAdded: (ViewTemplateCheckboxKey) -> Unit = {
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

        fun withMovedCheckbox(from: ViewTemplateCheckboxKey, to: ViewTemplateCheckboxKey): TemplateLoadingState {
            return copy(checkboxes = checkboxes) // TODO DO!
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
