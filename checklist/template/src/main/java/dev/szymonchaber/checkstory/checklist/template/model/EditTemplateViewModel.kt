package dev.szymonchaber.checkstory.checklist.template.model

import androidx.core.os.bundleOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.usecase.GetCurrentUserUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.StoreCommandsUseCase
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
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class EditTemplateViewModel @Inject constructor(
    private val templateFactory: OnboardingTemplateFactory,
    private val getTemplateUseCase: GetTemplateUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val storeCommandsUseCase: StoreCommandsUseCase,
    private val tracker: Tracker
) : BaseViewModel<
        EditTemplateEvent,
        EditTemplateState,
        EditTemplateEffect
        >(
    EditTemplateState.Loading
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
            handleAddTaskClicked(),
            handleTaskRemoved(),
            handleSiblingMoved(),
            handleChildMoved(),
            handleNewSiblingDragged(),
            handleNewChildDragged(),
            handleNewTaskDraggedToTop(),
            handleNewTaskDraggedToBottom(),
            handleTaskMovedToTop(),
            handleTaskMovedToBottom(),
            handleNewTaskDraggableClicked(),
            handleItemTitleChanged(),
            handleSaveTemplateClicked(),
            handleDeleteTemplateClicked(),
            handleConfirmDeleteTemplateClicked(),
            handleChildTaskAdded(),
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
        return filterIsInstance<EditTemplateEvent.CreateTemplate>()
            .map {
                if (isTemplateAlreadyCreated()) {
                    state.first() to null
                } else {
                    val template = emptyTemplate()
                    val templateLoadingState = EditTemplateState.Success.fromTemplate(template)
                        .copy(
                            commands = listOf(
                                TemplateCommand.CreateNewTemplate(
                                    template.id,
                                    Clock.System.now()
                                )
                            )
                        )
                    templateLoadingState to null
                }
            }
    }

    private fun Flow<EditTemplateEvent>.handleGenerateOnboardingTemplate(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.GenerateOnboardingTemplate>()
            .map {
                if (isTemplateAlreadyCreated()) {
                    state.first() to null
                } else {
                    tracker.logEvent("onboarding_template_generated")
                    templateFactory.generateOnboardingTemplate() to null
                }
            }
    }

    private fun Flow<EditTemplateEvent>.handleEditChecklist(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.EditTemplate>()
            .flatMapLatest { event ->
                if (isTemplateAlreadyLoaded(event)) {
                    flowOf(state.first() to null)
                } else {
                    flowOf(
                        getTemplateUseCase.getTemplate(event.templateId)?.let {
                            withContext(Dispatchers.Default) {
                                EditTemplateState.Success.fromTemplate(it)
                            }
                        } ?: EditTemplateState.Loading // TODO this should be error, template not found
                    )
                        .onStart {
                            emit(EditTemplateState.Loading)
                        }
                        .map {
                            it to null
                        }
                }
            }
    }

    private suspend fun isTemplateAlreadyLoaded(event: EditTemplateEvent.EditTemplate): Boolean {
        return (state.first() as? EditTemplateState.Success)?.template?.id == event.templateId
    }

    private suspend fun isTemplateAlreadyCreated(): Boolean {
        return state.first() is EditTemplateState.Success
    }

    private fun Flow<EditTemplateEvent>.handleTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                loadingState.withNewTitle(event.newTitle) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleDescriptionChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.DescriptionChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                loadingState.withNewDescription(event.newDescription) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleTaskRemoved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TaskRemoved>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("delete_checkbox_clicked")
                loadingState.minusTask(event.task) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleSiblingMoved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.SiblingMovedBelow>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("checkbox_moved_to_sibling")
                loadingState.withSiblingMovedBelow(event.target, event.newSibling) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewSiblingDragged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewSiblingDraggedBelow>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("new_checkbox_dragged_to_sibling")
                loadingState.withNewSiblingMovedBelow(event.target) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildMoved(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildMovedBelow>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("checkbox_moved_to_child")
                loadingState.withChildMovedBelow(event.target, event.newChild) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewChildDragged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewChildDraggedBelow>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("new_checkbox_dragged_to_child")
                loadingState.withNewChildMovedBelow(event.target) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleTaskMovedToTop(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TaskMovedToTop>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("checkbox_moved_to_top")
                loadingState.withTaskMovedToTop(event.taskId) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewTaskDraggedToTop(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewTaskDraggedToTop>()
            .withSuccessState()
            .map { (loadingState, _) ->
                tracker.logEvent("new_checkbox_dragged_to_top")
                loadingState.withNewTaskAtTop() to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleTaskMovedToBottom(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TaskMovedToBottom>()
            .withSuccessState()
            .map { (loadingState, event) ->
                tracker.logEvent("checkbox_moved_to_bottom")
                loadingState.withTaskMovedToBottom(event.taskId) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewTaskDraggableClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewTaskDraggableClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                tracker.logEvent("new_checkbox_draggable_clicked")
                loadingState to EditTemplateEffect.ShowTryDraggingSnackbar()
            }
    }

    private fun Flow<EditTemplateEvent>.handleNewTaskDraggedToBottom(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.NewTaskDraggedToBottom>()
            .withSuccessState()
            .map { (loadingState, _) ->
                tracker.logEvent("new_checkbox_dragged_to_bottom")
                loadingState.withNewTaskAtBottom() to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleChildTaskAdded(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.ChildTaskAdded>()
            .withSuccessState()
            .map { (loadingState, event) ->
                withContext(Dispatchers.Default) {
                    tracker.logEvent("add_child_checkbox_clicked")
                    loadingState.plusChildTask(TemplateTaskId(event.parentId.id)) to null
                }
            }
    }

    private fun Flow<EditTemplateEvent>.handleItemTitleChanged(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TaskTitleChanged>()
            .withSuccessState()
            .map { (loadingState, event) ->
                loadingState.changeTaskTitle(event.task, event.newTitle) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleAddTaskClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.AddTaskClicked>()
            .withSuccessState()
            .map { (loadingState, _) ->
                tracker.logEvent("add_checkbox_clicked")
                loadingState.plusNewTask("") to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleSaveTemplateClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.SaveTemplateClicked>()
            .withSuccessState()
            .mapLatest { (loadingState, _) ->
                val template = loadingState.template
                storeCommandsUseCase.storeCommands(consolidateCommands(loadingState.finalizedCommands()))
                tracker.logEvent(
                    "save_template_clicked", bundleOf(
                        "title_length" to template.title.length,
                        "description_length" to template.description.length,
                        "checkbox_count" to template.tasks.flatMap { it.children + it }.count(),
                        "reminder_count" to template.reminders.count()
                    )
                )
                if (loadingState.onboardingPlaceholders != null) {
                    tracker.logEvent("template_saved_during_onboarding")
                }
                null to EditTemplateEffect.CloseScreen
            }
    }

    private fun consolidateCommands(commands: List<TemplateCommand>): List<TemplateCommand> {
        // TODO this is a good place to trim titles and descriptions
        return commands
            .withLastCommandOfType<TemplateCommand.RenameTemplate> {
                it.templateId
            }
            .withLastCommandOfType<TemplateCommand.ChangeTemplateDescription> {
                it.templateId
            }
            .withLastCommandOfType<TemplateCommand.RenameTemplateTask> {
                it.taskId
            }
    }

    private inline fun <reified T : TemplateCommand> List<TemplateCommand>.withLastCommandOfType(
        groupBy: (T) -> Any
    ): List<TemplateCommand> {
        val consolidatedCommand = filterIsInstance<T>()
            .groupBy(groupBy)
            .map { (_, events) ->
                events.sortedBy { it.timestamp }.takeLast(1)
            }
            .flatten()
        val commandsWithoutConsolidatedCommand = filterNot { it is T }
        return commandsWithoutConsolidatedCommand.plus(consolidatedCommand)
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
                storeCommandsUseCase.storeCommands(
                    consolidateCommands(loadingState.markDeleted().finalizedCommands())
                )
                if (loadingState.onboardingPlaceholders != null) {
                    tracker.logEvent("template_creation_cancelled_during_onboarding")
                }
                null to EditTemplateEffect.CloseScreen
            }
    }

    private fun Flow<EditTemplateEvent>.handleBackClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.BackClicked>()
            .withSuccessState()
            .map { (success, _) ->
                val effect = if (canSafelyExit(success)) {
                    EditTemplateEffect.CloseScreen
                } else {
                    EditTemplateEffect.ShowConfirmExitDialog
                }
                null to effect
            }
    }

    private fun canSafelyExit(success: EditTemplateState.Success): Boolean {
        return success.commands.isEmpty() || hasCreateCommandOnly(success)
    }

    private fun hasCreateCommandOnly(success: EditTemplateState.Success): Boolean {
        return success.commands.size == 1 && success.commands.first() is TemplateCommand.CreateNewTemplate
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
                val user = getCurrentUserUseCase.getCurrentUserFlow().first()
                val effect = if (user.isPaidUser) {
                    EditTemplateEffect.ShowAddReminderSheet(state.template.id)
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
                success.withUpdatedReminder(event.reminder) to null
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
                success.minusReminder(event.reminder) to null
            }
    }

    private fun Flow<EditTemplateEvent>.handleTemplateHistoryClicked(): Flow<Pair<EditTemplateState?, EditTemplateEffect?>> {
        return filterIsInstance<EditTemplateEvent.TemplateHistoryClicked>()
            .withSuccessState()
            .map { (success, _) ->
                tracker.logEvent("edit_template_history_clicked")
                state.first() to EditTemplateEffect.OpenTemplateHistory(success.template.id)
            }
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<EditTemplateState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it }
                .filterIsInstance<EditTemplateState.Success>()
                .map { it to event }
                .take(1)
        }
    }

    fun isReorderValid(subject: TemplateTaskId, target: TemplateTaskId): Boolean {
        return (_state.value as? EditTemplateState.Success)?.let {
            !it.getAllAncestorsOf(target).contains(subject)
        } ?: false
    }
}

// TODO use this
private fun Template.trimEndingWhitespaces(): Template {
    return with(this) {
        copy(
            title = title.trimEnd(),
            description = description.trimEnd(),
            tasks = tasks.map {
                it.trimEndTitlesRecursive()
            }
        )
    }
}

private fun TemplateTask.trimEndTitlesRecursive(): TemplateTask {
    return copy(title = title.trimEnd(), children = children.map { it.trimEndTitlesRecursive() })
}
