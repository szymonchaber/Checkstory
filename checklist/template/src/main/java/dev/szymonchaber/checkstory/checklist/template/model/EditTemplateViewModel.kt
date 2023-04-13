package dev.szymonchaber.checkstory.checklist.template.model

import android.app.Application
import androidx.core.os.bundleOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.EditTemplateDomainEvent
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.usecase.DeleteChecklistTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteRemindersUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteTemplateCheckboxUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.SynchronizeEventsUseCase
import dev.szymonchaber.checkstory.domain.usecase.UpdateChecklistTemplateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    private val application: Application,
    private val getChecklistTemplateUseCase: GetChecklistTemplateUseCase,
    private val updateChecklistTemplateUseCase: UpdateChecklistTemplateUseCase,
    private val deleteTemplateCheckboxUseCase: DeleteTemplateCheckboxUseCase,
    private val deleteChecklistTemplateUseCase: DeleteChecklistTemplateUseCase,
    private val deleteRemindersUseCase: DeleteRemindersUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val synchronizeEventsUseCase: SynchronizeEventsUseCase,
    private val tracker: Tracker
) : BaseViewModel<
        EditTemplateEvent,
        EditTemplateState,
        EditTemplateEffect
        >(
    EditTemplateState.initial
) {

    override fun buildMviFlow(eventFlow: Flow<EditTemplateEvent>): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return eventFlow.buildMviFlowActual()
    }

    private fun Flow<EditTemplateEvent>.buildMviFlowActual(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return merge(
            handleCreateChecklist(),
            handleGenerateOnboardingTemplate(),
            handleEditChecklist(),
            handleTitleChanged(),
            handleDescriptionChanged(),
            handleAddCheckboxClicked(),
            handleItemRemoved(),
            handleSiblingMoved(),
            handleChildMoved(),
            handleNewSiblingDragged(),
            handleNewChildDragged(),
            handleNewCheckboxDraggedToTop(),
            handleNewCheckboxDraggedToBottom(),
            handleCheckboxMovedToTop(),
            handleCheckboxMovedToBottom(),
            handleNewCheckboxDraggableClicked(),
            handleItemTitleChanged(),
            handleSaveTemplateClicked(),
            handleDeleteTemplateClicked(),
            handleConfirmDeleteTemplateClicked(),
            handleChildItemAdded(),
            handleAddReminderClicked(),
            handleReminderClicked(),
            handleReminderSaved(),
            handleReminderDeleted(),
            handleBackClicked(),
            handleConfirmExitClicked(),
            handleTemplateHistoryClicked()
        )
    }

    private fun Flow<EditTemplateEvent>.handleCreateChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.CreateChecklistTemplate>()
            .map {
                if (isTemplateAlreadyCreated()) {
                    state.first() to null
                } else {
                    val checklistTemplate = emptyChecklistTemplate()
                    val templateLoadingState = TemplateLoadingState.Success.fromTemplate(checklistTemplate)
                        .copy(
                            events = listOf(
                                EditTemplateDomainEvent.CreateNewTemplate(
                                    checklistTemplate.id,
                                    System.currentTimeMillis()
                                )
                            )
                        )
                    EditTemplateState(templateLoadingState) to null
                }
            }
    }

    private fun Flow<EditTemplateEvent>.handleGenerateOnboardingTemplate(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.GenerateOnboardingChecklistTemplate>()
            .map {
                if (isTemplateAlreadyCreated()) {
                    state.first() to null
                } else {
                    tracker.logEvent("onboarding_template_generated")
                    EditTemplateState(generateOnboardingTemplate(application.resources)) to null
                }
            }
    }

    private fun Flow<EditTemplateEvent>.handleEditChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.EditChecklistTemplate>()
            .flatMapLatest { event ->
                if (isTemplateAlreadyLoaded(event)) {
                    flowOf(state.first() to null)
                } else {
                    getChecklistTemplateUseCase.getChecklistTemplate(event.checklistTemplateId)
                        .map {
                            withContext(Dispatchers.Default) {
                                TemplateLoadingState.Success.fromTemplate(it)
                            }
                        }
                        .onStart<TemplateLoadingState> {
                            emit(TemplateLoadingState.Loading)
                        }
                        .map {
                            EditTemplateState(it) to null
                        }
                }
            }
    }

    private suspend fun isTemplateAlreadyLoaded(event: EditTemplateEvent.EditChecklistTemplate): Boolean {
        return (state.first().templateLoadingState as? TemplateLoadingState.Success)?.checklistTemplate?.id == event.checklistTemplateId
    }

    private suspend fun isTemplateAlreadyCreated(): Boolean {
        return state.first().templateLoadingState is TemplateLoadingState.Success
    }

    private fun Flow<EditTemplateEvent>.handleTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                val newLoadingState = loadingState
                    .updateTemplate {
                        copy(title = event.newTitle)
                    }
                    .plusEvent(
                        EditTemplateDomainEvent.RenameTemplate(
                            loadingState.checklistTemplate.id,
                            event.newTitle,
                            System.currentTimeMillis()
                        )
                    )
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

    private fun Flow<EditTemplateEvent>.handleSiblingMoved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.SiblingMovedBelow>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("checkbox_moved_to_sibling")
                EditTemplateState(loadingState.withSiblingMovedBelow(event.target, event.newSibling)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewSiblingDragged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewSiblingDraggedBelow>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("new_checkbox_dragged_to_sibling")
                EditTemplateState(loadingState.withNewSiblingMovedBelow(event.target)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildMoved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildMovedBelow>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("checkbox_moved_to_child")
                EditTemplateState(loadingState.withChildMovedBelow(event.target, event.newChild)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewChildDragged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewChildDraggedBelow>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("new_checkbox_dragged_to_child")
                EditTemplateState(loadingState.withNewChildMovedBelow(event.target)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleCheckboxMovedToTop(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.CheckboxMovedToTop>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("checkbox_moved_to_top")
                EditTemplateState(loadingState.withCheckboxMovedToTop(event.checkboxKey)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewCheckboxDraggedToTop(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewCheckboxDraggedToTop>()
            .withSuccessState()
            .map { (loadingState, _) ->
                tracker.logEvent("new_checkbox_dragged_to_top")
                EditTemplateState(loadingState.withNewCheckboxAtTop()) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleCheckboxMovedToBottom(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.CheckboxMovedToBottom>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("checkbox_moved_to_bottom")
                EditTemplateState(loadingState.withCheckboxMovedToBottom(event.checkboxKey)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewCheckboxDraggableClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewTaskDraggableClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                tracker.logEvent("new_checkbox_draggable_clicked")
                EditTemplateState(loadingState) to EditTemplateEffect.ShowTryDraggingSnackbar()
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewCheckboxDraggedToBottom(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewCheckboxDraggedToBottom>()
            .withSuccessState()
            .map { (loadingState, _) ->
                tracker.logEvent("new_checkbox_dragged_to_bottom")
                EditTemplateState(loadingState.withNewCheckboxAtBottom()) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildItemAdded(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildItemAdded>()
            .withSuccessState()
            .map { (loadingState, event) ->
                withContext(Dispatchers.Default) {
                    tracker.logEvent("add_child_checkbox_clicked")
                    EditTemplateState(loadingState.plusChildCheckbox(event.parentViewKey)) to null
                }
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ItemTitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                EditTemplateState(loadingState.changeCheckboxTitle(event.checkbox, event.newTitle)) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleAddCheckboxClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.AddCheckboxClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                val (newLoadingState, newCheckboxId) = loadingState.plusNewCheckbox("")
                newLoadingState
                    .plusEvent(
                        EditTemplateDomainEvent.AddTemplateTask(
                            loadingState.checklistTemplate.id, newCheckboxId, null, System.currentTimeMillis()
                        )
                    )
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
                            items = loadingState.checkboxes
                                .mapIndexed { index, checkbox ->
                                    checkbox.toDomainModel(position = index)
                                }
                        )
                    }
                    .checklistTemplate
                updateChecklistTemplateUseCase.updateChecklistTemplate(checklistTemplate)
                deleteTemplateCheckboxUseCase.deleteTemplateCheckboxes(loadingState.checkboxesToDelete)
                deleteRemindersUseCase.deleteReminders(loadingState.remindersToDelete)
                synchronizeEventsUseCase.synchronizeEvents(deduplicateEvents(loadingState.events))
                tracker.logEvent(
                    "save_template_clicked", bundleOf(
                        "title_length" to checklistTemplate.title.length,
                        "description_length" to checklistTemplate.description.length,
                        "checkbox_count" to checklistTemplate.items.flatMap { it.children + it }.count(),
                        "reminder_count" to checklistTemplate.reminders.count()
                    )
                )
                if (loadingState.isOnboardingTemplate) {
                    tracker.logEvent("template_saved_during_onboarding")
                }
                null to EditTemplateEffect.CloseScreen
            }
    }

    private fun deduplicateEvents(events: List<EditTemplateDomainEvent>): List<EditTemplateDomainEvent> {
        val deduplicatedRename = events.filterIsInstance<EditTemplateDomainEvent.RenameTemplate>()
            .groupBy {
                it.id
            }.map { (_, events) ->
                events.sortedBy { it.timestamp }.takeLast(1)
            }
            .flatten()
        val eventsWithoutRename = events.filterNot { it is EditTemplateDomainEvent.RenameTemplate }
        return eventsWithoutRename.plus(deduplicatedRename)
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
                if (loadingState.isOnboardingTemplate) {
                    tracker.logEvent("template_creation_cancelled_during_onboarding")
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
                val effect = if (user.isPaidUser) {
                    EditTemplateEffect.ShowAddReminderSheet()
                } else {
                    EditTemplateEffect.ShowFreeRemindersUsed()
                }
                null to effect
            }
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

    private fun Flow<EditTemplateEvent>.handleTemplateHistoryClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TemplateHistoryClicked>()
            .withSuccessState()
            .map { (success, _) ->
                if (success.checklistTemplate.isStored) {
                    tracker.logEvent("edit_template_history_clicked")
                    state.first() to EditTemplateEffect.OpenTemplateHistory(success.checklistTemplate.id)
                } else {
                    state.first() to null
                }
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
}
