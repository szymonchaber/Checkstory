package dev.szymonchaber.checkstory.checklist.template.reminders

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEffect
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEvent
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderLoadingState
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderState
import dev.szymonchaber.checkstory.checklist.template.reminders.edit.IntervalType
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EditReminderViewModel @Inject constructor(
    private val tracker: Tracker
) :
    BaseViewModel<
            EditReminderEvent,
            EditReminderState,
            EditReminderEffect
            >(
        EditReminderState.initial
    ) {

    override fun buildMviFlow(eventFlow: Flow<EditReminderEvent>): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return merge(
            eventFlow.handleCreateReminder(),
            eventFlow.handleEditReminder(),
            eventFlow.handleTypeSelected(),
            eventFlow.handleTimeSet(),
            eventFlow.handleDateSet(),
            eventFlow.handleIntervalSelected(),
            eventFlow.handleDaysOfWeekSelected(),
            eventFlow.handleDayOfMonthSelected(),
            eventFlow.handleDayOfYearSelected(),
            eventFlow.handleSaveClicked()
        )
    }

    private fun Flow<EditReminderEvent>.handleCreateReminder(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.CreateReminder>()
            .map {
                val newReminder = Reminder.Exact(
                    ReminderId(0),
                    ChecklistTemplateId(0),
                    LocalDateTime.now()
                )
                EditReminderState(EditReminderLoadingState.Success.fromReminder(newReminder)) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleEditReminder(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.EditReminder>()
            .map {
                EditReminderState(EditReminderLoadingState.Success.fromReminder(it.reminder)) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleTypeSelected(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.ReminderTypeSelected>()
            .withSuccessState()
            .map { (success, _) ->
                val newState = success.updateReminder {
                    when (this) {
                        is Reminder.Exact -> {
                            Reminder.Recurring(id, forTemplate, startDateTime, Interval.Daily)
                        }
                        is Reminder.Recurring -> {
                            Reminder.Exact(id, forTemplate, startDateTime)
                        }
                    }
                }
                EditReminderState(newState) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleTimeSet(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.ReminderTimeSet>()
            .withSuccessState()
            .map { (success, event) ->
                val newState = success.updateReminder {
                    updateTime(event.time)
                }
                EditReminderState(newState) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleDateSet(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.ReminderDateSet>()
            .withSuccessState()
            .map { (success, event) ->
                val newState = success.updateReminder {
                    updateDate(event.date)
                }
                EditReminderState(newState) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleIntervalSelected(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.IntervalSelected>()
            .withSuccessState()
            .filter {
                it.first.reminder is Reminder.Recurring
            }
            .map { (success, event) ->
                val newInterval = when (event.intervalType) {
                    IntervalType.DAILY -> Interval.Daily
                    IntervalType.WEEKLY -> Interval.Weekly(LocalDateTime.now().dayOfWeek)
                    IntervalType.MONTHLY -> Interval.Monthly(1)
                    IntervalType.YEARLY -> Interval.Yearly(1)
                }
                val newReminder = (success.reminder as Reminder.Recurring).copy(interval = newInterval)
                EditReminderState(success.copy(reminder = newReminder)) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleDaysOfWeekSelected(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.DaysOfWeekSelected>()
            .withSuccessState()
            .filter {
                (it.first.reminder is Reminder.Recurring)
            }
            .map { (success, event) ->
                val newInterval = when (val interval = (success.reminder as Reminder.Recurring).interval) {
                    is Interval.Weekly -> interval.copy(dayOfWeek = event.daysOfWeek.last()) // TODO some unwrapping is needed
                    else -> interval
                }
                val newReminder = success.reminder.copy(interval = newInterval)
                EditReminderState(success.copy(reminder = newReminder)) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleDayOfMonthSelected(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.DayOfMonthSelected>()
            .withSuccessState()
            .filter {
                (it.first.reminder is Reminder.Recurring)
            }
            .map { (success, event) ->
                val newInterval = when (val interval = (success.reminder as Reminder.Recurring).interval) {
                    is Interval.Monthly -> interval.copy(dayOfMonth = event.dayOfMonth)
                    else -> interval
                }
                val newReminder = success.reminder.copy(interval = newInterval)
                EditReminderState(success.copy(reminder = newReminder)) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleDayOfYearSelected(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.DayOfYearSelected>()
            .withSuccessState()
            .filter {
                (it.first.reminder is Reminder.Recurring)
            }
            .map { (success, event) ->
                val newInterval = when (val interval = (success.reminder as Reminder.Recurring).interval) {
                    is Interval.Yearly -> interval.copy(dayOfYear = event.dayOfYear)
                    else -> interval
                }
                val newReminder = success.reminder.copy(interval = newInterval)
                EditReminderState(success.copy(reminder = newReminder)) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleSaveClicked(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.SaveReminderClicked>()
            .withSuccessState()
            .map { (success, _) ->
                null to EditReminderEffect.RelayReminderToSave(success.reminder)
            }
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<EditReminderLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.reminderLoadingState }
                .filterIsInstance<EditReminderLoadingState.Success>()
                .map { it to event }
                .take(1)
        }
    }
}
