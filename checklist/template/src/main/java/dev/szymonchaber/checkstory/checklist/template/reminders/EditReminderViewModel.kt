package dev.szymonchaber.checkstory.checklist.template.reminders

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEffect
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEvent
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderLoadingState
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderState
import dev.szymonchaber.checkstory.checklist.template.reminders.edit.IntervalType
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
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
import javax.inject.Inject

@HiltViewModel
class EditReminderViewModel @Inject constructor() :
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
            eventFlow.handleTypeSelected(),
            eventFlow.handleTimeSet(),
            eventFlow.handleDateSet(),
            eventFlow.handleIntervalSelected(),
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
                    IntervalType.WEEKLY -> Interval.Weekly(listOf())
                    IntervalType.MONTHLY -> Interval.Monthly(1)
                    IntervalType.YEARLY -> Interval.Yearly(1)
                }
                val newReminder = (success.reminder as Reminder.Recurring).copy(interval = newInterval)
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
