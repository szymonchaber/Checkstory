package dev.szymonchaber.checkstory.checklist.template.edit.model

import dev.szymonchaber.checkstory.checklist.template.reminders.edit.IntervalType
import dev.szymonchaber.checkstory.checklist.template.reminders.edit.ReminderType
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

sealed interface EditReminderEvent {

    object CreateReminder : EditReminderEvent

    data class EditReminder(val reminder: Reminder) : EditReminderEvent

    data class ReminderTypeSelected(val reminderType: ReminderType) : EditReminderEvent

    data class ReminderTimeSet(val time: LocalTime) : EditReminderEvent

    data class ReminderDateSet(val date: LocalDate) : EditReminderEvent

    data class IntervalSelected(val intervalType: IntervalType) : EditReminderEvent

    data class DaysOfWeekSelected(val daysOfWeek: List<DayOfWeek>) : EditReminderEvent

    data class DayOfMonthSelected(val dayOfMonth: Int) : EditReminderEvent

    data class DayOfYearSelected(val dayOfYear: Int) : EditReminderEvent

    object SaveReminderClicked : EditReminderEvent
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

    data class RelayReminderToSave(val reminder: Reminder) : EditReminderEffect
}
