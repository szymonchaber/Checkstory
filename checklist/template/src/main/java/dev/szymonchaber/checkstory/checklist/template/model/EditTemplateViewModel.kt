package dev.szymonchaber.checkstory.checklist.template.model

import androidx.core.os.bundleOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.usecase.DeleteChecklistTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteRemindersUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteTemplateCheckboxUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.UpdateChecklistTemplateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    private val getChecklistTemplateUseCase: GetChecklistTemplateUseCase,
    private val updateChecklistTemplateUseCase: UpdateChecklistTemplateUseCase,
    private val deleteTemplateCheckboxUseCase: DeleteTemplateCheckboxUseCase,
    private val deleteChecklistTemplateUseCase: DeleteChecklistTemplateUseCase,
    private val deleteRemindersUseCase: DeleteRemindersUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val tracker: Tracker
) : BaseViewModel<
        EditTemplateEvent,
        EditTemplateState,
        EditTemplateEffect
        >(
    EditTemplateState.initial
) {

    override fun buildMviFlow(eventFlow: Flow<EditTemplateEvent>): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return merge(
            eventFlow.handleCreateChecklist(),
            eventFlow.handleEditChecklist(),
            eventFlow.handleTitleChanged(),
            eventFlow.handleDescriptionChanged(),
            eventFlow.handleAddCheckboxClicked(),
            eventFlow.handleItemRemoved(),
            eventFlow.handleItemTitleChanged(),
            eventFlow.handleSaveTemplateClicked(),
            eventFlow.handleDeleteTemplateClicked(),
            eventFlow.handleConfirmDeleteTemplateClicked(),
            eventFlow.handleChildItemAdded(),
            eventFlow.handleChildItemDeleted(),
            eventFlow.handleChildItemChanged(),
            eventFlow.handleAddReminderClicked(),
            eventFlow.handleReminderClicked(),
            eventFlow.handleReminderSaved(),
            eventFlow.handleReminderDeleted(),
            eventFlow.handleBackClicked(),
            eventFlow.handleConfirmExitClicked()
        )
    }

    private fun Flow<EditTemplateEvent>.handleCreateChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.CreateChecklistTemplate>()
            .map {
                val newChecklistTemplate = ChecklistTemplate(
                    ChecklistTemplateId(0),
                    "",
                    "",
                    listOf(
                        TemplateCheckbox(TemplateCheckboxId(0), null, "", listOf()),
                    ),
                    LocalDateTime.now(),
                    listOf(),
                    listOf()
                )
                EditTemplateState(TemplateLoadingState.Success.fromTemplate(newChecklistTemplate)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleEditChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.EditChecklistTemplate>()
            .flatMapLatest { event ->
                getChecklistTemplateUseCase.getChecklistTemplate(event.checklistTemplateId)
                    .map {
                        TemplateLoadingState.Success.fromTemplate(it)
                    }
                    .onStart<TemplateLoadingState> {
                        emit(TemplateLoadingState.Loading)
                    }
                    .map {
                        EditTemplateState(it) to null
                    }
            }
    }

    private fun Flow<EditTemplateEvent>.handleTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = loadingState.updateTemplate {
                    copy(title = event.newTitle)
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleDescriptionChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.DescriptionChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = loadingState.updateTemplate {
                    copy(description = event.newDescription)
                }
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemRemoved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ItemRemoved>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("delete_checkbox_clicked")
                EditTemplateState(loadingState.minusCheckbox(event.checkbox)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemAdded(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemAdded>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("add_child_checkbox_clicked")
                EditTemplateState(loadingState.plusChildCheckbox(event.parent, "")) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ItemTitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                EditTemplateState(loadingState.changeCheckboxTitle(event.checkbox, event.newTitle)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemDeleted(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemDeleted>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("delete_child_checkbox_clicked")
                EditTemplateState(loadingState.minusChildCheckbox(event.checkbox, event.child)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemTitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                EditTemplateState(
                    loadingState.changeChildCheckboxTitle(event.checkbox, event.child, event.newTitle)
                ) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleAddCheckboxClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.AddCheckboxClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                val newLoadingState = loadingState.plusNewCheckbox("")
                tracker.logEvent("add_checkbox_clicked")
                EditTemplateState(newLoadingState) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleSaveTemplateClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.SaveTemplateClicked>()
            .withSuccessState()
            .mapLatest { (loadingState, _) ->
                val checklistTemplate = loadingState
                    .updateTemplate {
                        copy(
                            title = title.trimEnd(),
                            description = description.trim(),
                            items = loadingState.checkboxes.map(ViewTemplateCheckbox::toDomainModel)
                                .map { it.copy(title = it.title.trimEnd()) })
                    }
                    .checklistTemplate
                updateChecklistTemplateUseCase.updateChecklistTemplate(checklistTemplate)
                deleteTemplateCheckboxUseCase.deleteTemplateCheckboxes(loadingState.checkboxesToDelete)
                deleteRemindersUseCase.deleteReminders(loadingState.remindersToDelete)
                tracker.logEvent(
                    "save_template_clicked", bundleOf(
                        "title_length" to checklistTemplate.title.length,
                        "description_length" to checklistTemplate.description.length,
                        "checkbox_count" to checklistTemplate.items.flatMap { it.children + it }.count(),
                        "reminder_count" to checklistTemplate.reminders.count()
                    )
                )
                null to EditTemplateEffect.CloseScreen
            }
    }

    private fun Flow<EditTemplateEvent>.handleDeleteTemplateClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.DeleteTemplateClicked>()
            .withSuccessState()
            .map { (_, _) ->
                tracker.logEvent("delete_template_clicked")
                null to EditTemplateEffect.ShowConfirmDeleteDialog()
            }
    }

    private fun Flow<EditTemplateEvent>.handleConfirmDeleteTemplateClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ConfirmDeleteTemplateClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                tracker.logEvent("delete_template_confirmation_clicked")
                if (loadingState.checklistTemplate.isStored) {
                    deleteChecklistTemplateUseCase.deleteChecklistTemplate(loadingState.checklistTemplate)
                }
                null to EditTemplateEffect.CloseScreen
            }
    }

    private fun Flow<EditTemplateEvent>.handleBackClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.BackClicked>()
            .withSuccessState()
            .map { (success, _) ->
                val event = if (success.isChanged() || !success.checklistTemplate.isStored) {
                    EditTemplateEffect.ShowConfirmExitDialog()
                } else {
                    EditTemplateEffect.CloseScreen
                }
                null to event
            }
    }

    private fun Flow<EditTemplateEvent>.handleConfirmExitClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ConfirmExitClicked>()
            .withSuccessState()
            .map {
                tracker.logEvent("exit_without_saving_confirmation_clicked")
                null to EditTemplateEffect.CloseScreen
            }
    }

    private fun Flow<EditTemplateEvent>.handleAddReminderClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.AddReminderClicked>()
            .withSuccessState()
            .map { (state, _) ->
                tracker.logEvent("add_reminder_clicked")
                val user = getUserUseCase.getUser().first()
                val effect = if (canAddReminderToTemplate(user, state.checklistTemplate)) {
                    EditTemplateEffect.ShowAddReminderSheet()
                } else {
                    EditTemplateEffect.ShowFreeRemindersUsed()
                }
                null to effect
            }
    }

    private fun canAddReminderToTemplate(user: User, template: ChecklistTemplate): Boolean {
        return template.reminders.count() < MAX_FREE_REMINDERS || user.isPaidUser
    }

    private fun Flow<EditTemplateEvent>.handleReminderClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ReminderClicked>()
            .withSuccessState()
            .map { (_, event) ->
                tracker.logEvent("reminder_clicked")
                null to EditTemplateEffect.ShowEditReminderSheet(event.reminder)
            }
    }

    private fun Flow<EditTemplateEvent>.handleReminderSaved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ReminderSaved>()
            .withSuccessState()
            .map { (success, event) ->
                trackReminderSaved(event)
                EditTemplateState(success.plusReminder(event.reminder)) to null
            }
    }

    private fun trackReminderSaved(event: EditTemplateEvent.ReminderSaved) {
        val reminderDetails = when (event.reminder) {
            is Reminder.Exact -> {
                bundleOf("type" to "exact")
            }
            is Reminder.Recurring -> {
                when (val interval = event.reminder.interval) {
                    Interval.Daily -> bundleOf("interval" to "daily")
                    is Interval.Weekly -> bundleOf("interval" to "weekly", "days_of_week" to interval.dayOfWeek)
                    is Interval.Monthly -> bundleOf("interval" to "monthly", "day_of_month" to interval.dayOfMonth)
                    is Interval.Yearly -> bundleOf("interval" to "yearly", "day_of_year" to interval.dayOfYear)
                }.apply {
                    putString("type", "recurring")
                }
            }
        }
        reminderDetails.putString("time", event.reminder.startDateTime.toLocalTime().toString())
        reminderDetails.putString("date", event.reminder.startDateTime.toLocalDate().toString())
        tracker.logEvent("reminder_added", reminderDetails)
    }

    private fun Flow<EditTemplateEvent>.handleReminderDeleted(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.DeleteReminderClicked>()
            .withSuccessState()
            .map { (success, event) ->
                tracker.logEvent("delete_reminder_clicked")
                EditTemplateState(success.minusReminder(event.reminder)) to null
            }
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<TemplateLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.templateLoadingState }
                .filterIsInstance<TemplateLoadingState.Success>()
                .map { it to event }
                .take(1)
        }
    }

    companion object {

        private const val MAX_FREE_REMINDERS = 3
    }
}
