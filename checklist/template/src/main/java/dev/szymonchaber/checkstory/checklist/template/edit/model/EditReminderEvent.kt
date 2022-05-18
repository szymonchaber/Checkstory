package dev.szymonchaber.checkstory.checklist.template.edit.model

import dev.szymonchaber.checkstory.checklist.template.reminders.edit.ReminderType
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId

sealed interface EditReminderEvent {

    object CreateReminder : EditReminderEvent

    data class EditReminder(val checklistTemplateId: ReminderId) : EditReminderEvent

    data class ReminderTypeSelected(val reminderType: ReminderType) :
        EditReminderEvent

    object SaveReminderClicked : EditReminderEvent

    object DeleteReminderClicked : EditReminderEvent
}

data class EditReminderState(
    val reminderLoadingState: EditReminderLoadingState
) {

    companion object {

        val initial = EditReminderState(EditReminderLoadingState.Loading)
    }
}

sealed interface EditReminderLoadingState {

    data class Success(
        val reminder: Reminder,
    ) : EditReminderLoadingState {

        fun updateReminder(block: Reminder.() -> Reminder): Success {
            return copy(reminder = reminder.block())
        }

        companion object {

            fun fromReminder(reminder: Reminder): Success {
                return Success(reminder)
            }
        }
    }

    object Loading : EditReminderLoadingState
}

sealed interface EditReminderEffect {

    object CloseScreen : EditReminderEffect
}
