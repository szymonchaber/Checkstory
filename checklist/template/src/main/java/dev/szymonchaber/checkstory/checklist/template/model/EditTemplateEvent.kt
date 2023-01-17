package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder

sealed interface EditTemplateEvent {

    object CreateChecklistTemplate : EditTemplateEvent

    data class EditChecklistTemplate(val checklistTemplateId: ChecklistTemplateId) : EditTemplateEvent

    data class TitleChanged(val newTitle: String) : EditTemplateEvent

    data class DescriptionChanged(val newDescription: String) : EditTemplateEvent

    data class ItemRemoved(val checkbox: ViewTemplateCheckbox) : EditTemplateEvent

    data class ParentItemsSwapped(val from: ViewTemplateCheckbox, val to: ViewTemplateCheckbox) : EditTemplateEvent

    data class ItemTitleChanged(val checkbox: ViewTemplateCheckbox, val newTitle: String) : EditTemplateEvent

    data class ChildItemAdded(val parent: ViewTemplateCheckbox) : EditTemplateEvent

    data class ChildItemTitleChanged(
        val checkbox: ViewTemplateCheckbox,
        val child: ViewTemplateCheckbox,
        val newTitle: String
    ) : EditTemplateEvent

    data class ChildItemDeleted(val checkbox: ViewTemplateCheckbox, val child: ViewTemplateCheckbox) : EditTemplateEvent

    object AddReminderClicked : EditTemplateEvent

    data class ReminderClicked(val reminder: Reminder) : EditTemplateEvent

    data class ReminderSaved(val reminder: Reminder) : EditTemplateEvent

    data class DeleteReminderClicked(val reminder: Reminder) : EditTemplateEvent

    data class ChildItemMoved(
        val child: ViewTemplateCheckbox,
        val oldParent: ViewTemplateCheckbox,
        val newParent: ViewTemplateCheckbox,
        val newLocalIndex: Int?
    ) : EditTemplateEvent

    data class OnUnwrappedCheckboxMoved(
        val from: ViewTemplateCheckbox,
        val to: ViewTemplateCheckbox
    ) : EditTemplateEvent

    object AddCheckboxClicked : EditTemplateEvent

    object SaveTemplateClicked : EditTemplateEvent

    object DeleteTemplateClicked : EditTemplateEvent

    object ConfirmDeleteTemplateClicked : EditTemplateEvent

    object BackClicked : EditTemplateEvent

    object ConfirmExitClicked : EditTemplateEvent

    object TemplateHistoryClicked : EditTemplateEvent
}
