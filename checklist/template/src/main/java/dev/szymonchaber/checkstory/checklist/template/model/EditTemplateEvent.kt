package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.checklist.template.ViewTemplateCheckboxKey
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder

sealed interface EditTemplateEvent {

    object CreateChecklistTemplate : EditTemplateEvent

    object GenerateOnboardingChecklistTemplate : EditTemplateEvent

    data class EditChecklistTemplate(val checklistTemplateId: ChecklistTemplateId) : EditTemplateEvent

    data class TitleChanged(val newTitle: String) : EditTemplateEvent

    data class DescriptionChanged(val newDescription: String) : EditTemplateEvent

    data class ItemRemoved(val checkbox: ViewTemplateCheckbox) : EditTemplateEvent

    data class ItemTitleChanged(val checkbox: ViewTemplateCheckbox, val newTitle: String) : EditTemplateEvent

    data class ChildItemAdded(val parentViewKey: ViewTemplateCheckboxKey) : EditTemplateEvent

    object AddReminderClicked : EditTemplateEvent

    data class ReminderClicked(val reminder: Reminder) : EditTemplateEvent

    data class ReminderSaved(val reminder: Reminder) : EditTemplateEvent

    data class DeleteReminderClicked(val reminder: Reminder) : EditTemplateEvent

    data class SiblingMovedBelow(
        val target: ViewTemplateCheckboxKey,
        val newSibling: ViewTemplateCheckboxKey
    ) : EditTemplateEvent

    data class ChildMovedBelow(
        val target: ViewTemplateCheckboxKey,
        val newChild: ViewTemplateCheckboxKey
    ) : EditTemplateEvent

    data class CheckboxMovedToTop(val checkboxKey: ViewTemplateCheckboxKey) : EditTemplateEvent

    object AddCheckboxClicked : EditTemplateEvent

    object SaveTemplateClicked : EditTemplateEvent

    object DeleteTemplateClicked : EditTemplateEvent

    object ConfirmDeleteTemplateClicked : EditTemplateEvent

    object BackClicked : EditTemplateEvent

    object ConfirmExitClicked : EditTemplateEvent

    object TemplateHistoryClicked : EditTemplateEvent
}
