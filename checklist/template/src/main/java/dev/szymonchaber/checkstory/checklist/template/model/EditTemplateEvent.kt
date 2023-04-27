package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder

sealed interface EditTemplateEvent {

    object CreateChecklistTemplate : EditTemplateEvent

    object GenerateOnboardingChecklistTemplate : EditTemplateEvent

    data class EditChecklistTemplate(val checklistTemplateId: ChecklistTemplateId) : EditTemplateEvent

    data class TitleChanged(val newTitle: String) : EditTemplateEvent

    data class DescriptionChanged(val newDescription: String) : EditTemplateEvent

    data class ItemRemoved(val checkbox: ViewTemplateCheckbox) : EditTemplateEvent

    data class ItemTitleChanged(val checkbox: ViewTemplateCheckbox, val newTitle: String) : EditTemplateEvent

    data class ChildItemAdded(val parentViewKey: TemplateCheckboxId) : EditTemplateEvent

    object AddReminderClicked : EditTemplateEvent

    data class ReminderClicked(val reminder: Reminder) : EditTemplateEvent

    data class ReminderSaved(val reminder: Reminder) : EditTemplateEvent

    data class DeleteReminderClicked(val reminder: Reminder) : EditTemplateEvent

    data class SiblingMovedBelow(
        val target: TemplateCheckboxId,
        val newSibling: TemplateCheckboxId
    ) : EditTemplateEvent

    data class NewSiblingDraggedBelow(val target: TemplateCheckboxId) : EditTemplateEvent

    data class ChildMovedBelow(
        val target: TemplateCheckboxId,
        val newChild: TemplateCheckboxId
    ) : EditTemplateEvent

    data class NewChildDraggedBelow(val target: TemplateCheckboxId) : EditTemplateEvent

    object NewCheckboxDraggedToTop : EditTemplateEvent

    object NewCheckboxDraggedToBottom : EditTemplateEvent

    data class CheckboxMovedToTop(val checkboxKey: TemplateCheckboxId) : EditTemplateEvent

    data class CheckboxMovedToBottom(val checkboxKey: TemplateCheckboxId) : EditTemplateEvent

    object AddCheckboxClicked : EditTemplateEvent

    object SaveTemplateClicked : EditTemplateEvent

    object DeleteTemplateClicked : EditTemplateEvent

    object ConfirmDeleteTemplateClicked : EditTemplateEvent

    object BackClicked : EditTemplateEvent

    object ConfirmExitClicked : EditTemplateEvent

    object TemplateHistoryClicked : EditTemplateEvent

    object NewTaskDraggableClicked : EditTemplateEvent
}
