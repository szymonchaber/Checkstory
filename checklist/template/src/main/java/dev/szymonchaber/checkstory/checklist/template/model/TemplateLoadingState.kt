package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.checklist.template.wrapReorderChanges
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import timber.log.Timber

sealed interface TemplateLoadingState {

    data class Success(
        val originalChecklistTemplate: ChecklistTemplate,
        val checkboxes: List<ViewTemplateCheckbox>,
        val checkboxesToDelete: List<TemplateCheckbox>,
        val remindersToDelete: List<Reminder>,
        val checklistTemplate: ChecklistTemplate = originalChecklistTemplate
    ) : TemplateLoadingState {

        val unwrappedCheckboxes = checkboxes.flatMap {
            listOf(it) + it.children
        }

        fun isChanged(): Boolean {
            return originalChecklistTemplate != checklistTemplate
                    || originalChecklistTemplate.items != checkboxes.map { it.toDomainModel() }
                    || checkboxesToDelete.isNotEmpty()
                    || remindersToDelete.isNotEmpty()
        }

        fun updateTemplate(block: ChecklistTemplate.() -> ChecklistTemplate): Success {
            return copy(checklistTemplate = checklistTemplate.block())
        }

        fun plusNewCheckbox(title: String): Success {
            return copy(
                checkboxes = checkboxes.plus(
                    ViewTemplateCheckbox.New(
                        TemplateCheckboxId(checkboxes.size.toLong()),
                        null,
                        true,
                        title,
                        listOf()
                    )
                )
            )
        }

        fun minusCheckbox(checkbox: ViewTemplateCheckbox): Success {
            val shouldDeleteFromDatabase = checkbox is ViewTemplateCheckbox.Existing
            val updatedCheckboxesToDelete = if (shouldDeleteFromDatabase) {
                checkboxesToDelete.plus(checkbox.toDomainModel())
            } else {
                checkboxesToDelete
            }
            return copy(
                checkboxes = checkboxes.minus(checkbox),
                checkboxesToDelete = updatedCheckboxesToDelete
            )
        }

        fun plusChildCheckbox(parent: ViewTemplateCheckbox, title: String): Success {
            return copy(
                checkboxes = checkboxes.update(parent) {
                    it.plusChildCheckbox(title)
                }
            )
        }

        fun changeCheckboxTitle(checkbox: ViewTemplateCheckbox, title: String): Success {
            return copy(
                checkboxes = checkboxes.update(checkbox) {
                    it.withUpdatedTitle(title)
                }
            )
        }

        fun changeChildCheckboxTitle(
            parent: ViewTemplateCheckbox,
            child: ViewTemplateCheckbox,
            title: String
        ): Success {
            return copy(
                checkboxes = checkboxes.update(parent) {
                    it.editChildCheckboxTitle(child, title)
                }
            )
        }

        fun minusChildCheckbox(parent: ViewTemplateCheckbox, viewTemplateCheckbox: ViewTemplateCheckbox): Success {
            val shouldDeleteFromDatabase = viewTemplateCheckbox is ViewTemplateCheckbox.Existing
            val updatedCheckboxesToDelete = if (shouldDeleteFromDatabase) {
                checkboxesToDelete.plus(viewTemplateCheckbox.toDomainModel())
            } else {
                checkboxesToDelete
            }
            return copy(
                checkboxes = checkboxes.update(parent) {
                    it.minusChildCheckbox(viewTemplateCheckbox)
                },
                checkboxesToDelete = updatedCheckboxesToDelete
            )
        }

        fun plusReminder(reminder: Reminder): Success {
            return updateTemplate {
                copy(reminders = reminders.plus(reminder))
            } // Do the same new / old discrimination (or not?)
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

        fun withMovedUnwrappedCheckbox(from: ViewTemplateCheckbox, to: ViewTemplateCheckbox): TemplateLoadingState {
            val wrappedList = wrapReorderChanges(unwrappedCheckboxes, from, to)
            return copy(checkboxes = wrappedList)
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
