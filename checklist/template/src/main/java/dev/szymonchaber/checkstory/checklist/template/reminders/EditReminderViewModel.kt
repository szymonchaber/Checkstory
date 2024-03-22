package dev.szymonchaber.checkstory.checklist.template.reminders

import androidx.core.os.bundleOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEffect
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEvent
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderLoadingState
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderState
import dev.szymonchaber.checkstory.checklist.template.reminders.edit.IntervalType
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import java.time.LocalDateTime
import java.util.*
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
                    ReminderId(UUID.randomUUID()),
                    it.templateId,
                    LocalDateTime.now().withSecond(0)
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
            .withReadyState()
            .map { (ready, _) ->
                val newState = ready.updateReminder {
                    when (this) {
                        is Reminder.Exact -> {
                            Reminder.Recurring(id, forTemplate, startDateTime, Interval.Daily)
                        }
                        is Reminder.Recurring -> {
                            Reminder.Exact(id, forTemplate, startDateTime)
                        }
                    }
                }
                tracker.logEvent("reminder_type_changed")
                EditReminderState(newState) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleTimeSet(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.ReminderTimeSet>()
            .withReadyState()
            .map { (ready, event) ->
                val newState = ready.updateReminder {
                    updateTime(event.time.withSecond(0))
                }
                tracker.logEvent("reminder_time_set", bundleOf("time" to event.time))
                EditReminderState(newState) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleDateSet(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.ReminderDateSet>()
            .withReadyState()
            .map { (ready, event) ->
                val newState = ready.updateReminder {
                    updateDate(event.date)
                }
                tracker.logEvent("reminder_date_set", bundleOf("date" to event.date))
                EditReminderState(newState) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleIntervalSelected(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.IntervalSelected>()
            .withReadyState()
            .filter {
                it.first.reminder is Reminder.Recurring
            }
            .map { (ready, event) ->
                val newInterval = when (event.intervalType) {
                    IntervalType.DAILY -> Interval.Daily
                    IntervalType.WEEKLY -> Interval.Weekly(LocalDateTime.now().dayOfWeek)
                    IntervalType.MONTHLY -> Interval.Monthly(1)
                    IntervalType.YEARLY -> Interval.Yearly(1)
                }
                tracker.logEvent("reminder_interval_selected", bundleOf("interval" to toTrackingName(newInterval)))
                val newReminder = (ready.reminder as Reminder.Recurring).copy(interval = newInterval)
                EditReminderState(ready.copy(reminder = newReminder)) to null
            }
    }

    private fun toTrackingName(interval: Interval): String {
        return when (interval) {
            Interval.Daily -> "daily"
            is Interval.Monthly -> "monthly"
            is Interval.Weekly -> "weekly"
            is Interval.Yearly -> "yearly"
        }
    }

    private fun Flow<EditReminderEvent>.handleDaysOfWeekSelected(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.DaysOfWeekSelected>()
            .withReadyState()
            .filter {
                (it.first.reminder is Reminder.Recurring)
            }
            .map { (ready, event) ->
                val newInterval = when (val interval = (ready.reminder as Reminder.Recurring).interval) {
                    is Interval.Weekly -> interval.copy(dayOfWeek = event.daysOfWeek.last()) // TODO some unwrapping is needed
                    else -> interval
                }
                tracker.logEvent(
                    "reminder_days_of_week_selected",
                    bundleOf("days_of_week" to event.daysOfWeek.joinToString())
                )
                val newReminder = ready.reminder.copy(interval = newInterval)
                EditReminderState(ready.copy(reminder = newReminder)) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleDayOfMonthSelected(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.DayOfMonthSelected>()
            .withReadyState()
            .filter {
                (it.first.reminder is Reminder.Recurring)
            }
            .map { (ready, event) ->
                val newInterval = when (val interval = (ready.reminder as Reminder.Recurring).interval) {
                    is Interval.Monthly -> interval.copy(dayOfMonth = event.dayOfMonth)
                    else -> interval
                }
                tracker.logEvent("reminder_day_of_month_selected", bundleOf("day_of_month" to event.dayOfMonth))
                val newReminder = ready.reminder.copy(interval = newInterval)
                EditReminderState(ready.copy(reminder = newReminder)) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleDayOfYearSelected(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.DayOfYearSelected>()
            .withReadyState()
            .filter {
                (it.first.reminder is Reminder.Recurring)
            }
            .map { (ready, event) ->
                val newInterval = when (val interval = (ready.reminder as Reminder.Recurring).interval) {
                    is Interval.Yearly -> interval.copy(dayOfYear = event.dayOfYear)
                    else -> interval
                }
                tracker.logEvent("reminder_day_of_year_selected", bundleOf("day_of_year" to event.dayOfYear))
                val newReminder = ready.reminder.copy(interval = newInterval)
                EditReminderState(ready.copy(reminder = newReminder)) to null
            }
    }

    private fun Flow<EditReminderEvent>.handleSaveClicked(): Flow<Pair<EditReminderState?, EditReminderEffect?>> {
        return filterIsInstance<EditReminderEvent.SaveReminderClicked>()
            .withReadyState()
            .map { (ready, _) ->
                null to EditReminderEffect.RelayReminderToSave(ready.reminder)
            }
    }

    private fun <T> Flow<T>.withReadyState(): Flow<Pair<EditReminderLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.reminderLoadingState }
                .filterIsInstance<EditReminderLoadingState.Success>()
                .map { it to event }
                .take(1)
        }
    }
}
