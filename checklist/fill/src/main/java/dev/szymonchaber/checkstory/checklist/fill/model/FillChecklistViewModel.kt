package dev.szymonchaber.checkstory.checklist.fill.model

import androidx.core.os.bundleOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task.Companion.checkedCount
import dev.szymonchaber.checkstory.domain.usecase.CreateChecklistFromTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistToFillUseCase
import dev.szymonchaber.checkstory.domain.usecase.StoreCommandsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.datetime.Clock
import java.util.*
import javax.inject.Inject
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistEffect as Effect
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistEvent as Event
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistState as State

@HiltViewModel
class FillChecklistViewModel @Inject constructor(
    private val getChecklistToFillUseCase: GetChecklistToFillUseCase,
    private val createChecklistFromTemplateUseCase: CreateChecklistFromTemplateUseCase,
    private val tracker: Tracker,
    private val commandsUseCase: StoreCommandsUseCase
) : BaseViewModel<Event, State, Effect>(State.Loading) {

    override fun buildMviFlow(eventFlow: Flow<Event>): Flow<Pair<State?, Effect?>> {
        return merge(
            eventFlow.handleCreateChecklist(),
            eventFlow.handleLoadChecklist(),
            eventFlow.handleCheckChanged(),
            eventFlow.handleNotesClicked(),
            eventFlow.handleNotesChanged(),
            eventFlow.handleEditClicked(),
            eventFlow.handleSaveClicked(),
            eventFlow.handleDeleteClicked(),
            eventFlow.handleDeleteConfirmed(),
            eventFlow.handleChildCheckChanged(),
            eventFlow.handleBackClicked(),
            eventFlow.confirmExitClicked()
        )
    }

    private fun Flow<Event>.handleCheckChanged(): Flow<Pair<State, Nothing?>> {
        return filterIsInstance<Event.CheckChanged>()
            .withReadyState()
            .mapLatest { (ready, event) ->
                tracker.logEvent("check_changed", bundleOf("checked" to event.newCheck))
                val updated = ready.withUpdatedItemChecked(event.item.id, event.newCheck)
                run {
                    state.first()
                    updated
                } to null
            }
    }

    private fun Flow<Event>.handleChildCheckChanged(): Flow<Pair<State, Nothing?>> {
        return filterIsInstance<Event.ChildCheckChanged>()
            .withReadyState()
            .mapLatest { (ready, event) ->
                tracker.logEvent("child_check_changed", bundleOf("checked" to event.newCheck))
                val (_, child, newCheck) = event
                val updated = ready.withUpdatedItemChecked(child.id, newCheck)
                run {
                    state.first()
                    updated
                } to null
            }
    }

    private fun Flow<Event>.handleLoadChecklist(): Flow<Pair<State, Effect?>> {
        return filterIsInstance<Event.LoadChecklist>()
            .flatMapLatest { loadEvent ->
                getChecklistToFillUseCase.getChecklist(loadEvent.checklistId).map {
                    State.Ready(it) to null
                }
            }
    }

    private fun Flow<Event>.handleCreateChecklist(): Flow<Pair<State, Effect?>> {
        return filterIsInstance<Event.CreateChecklistFromTemplate>()
            .flatMapLatest { loadEvent ->
                createChecklistFromTemplateUseCase.createChecklistFromTemplate(loadEvent.templateId)
                    .map {
                        val checklistLoadingState = State.Ready(it)
                            .copy(
                                commands = listOf(
                                    ChecklistCommand.CreateChecklistCommand(
                                        it.id,
                                        it.templateId,
                                        it.items,
                                        UUID.randomUUID(),
                                        Clock.System.now()
                                    )
                                )
                            )
                        checklistLoadingState to null
                    }
            }
    }

    private fun Flow<Event>.handleNotesClicked(): Flow<Pair<State, Effect?>> {
        return filterIsInstance<Event.NotesClicked>()
            .withReadyState()
            .map {
                state.first() to Effect.ShowNotesEditShelf()
            }
    }

    private fun Flow<Event>.handleNotesChanged(): Flow<Pair<State, Nothing?>> {
        return filterIsInstance<Event.NotesChanged>()
            .withReadyState()
            .map { (ready, event) ->
                ready.withUpdatedNotes(event.notes) to null
            }
    }

    private fun Flow<Event>.handleEditClicked(): Flow<Pair<State, Effect?>> {
        return filterIsInstance<Event.EditTemplateClicked>()
            .flatMapLatest {
                state.filterIsInstance<State.Ready>().take(1)
            }
            .map {
                tracker.logEvent("checklist_edit_template_clicked")
                state.first() to Effect.NavigateToEditTemplate(it.checklist.templateId)
            }
    }

    private fun Flow<Event>.handleSaveClicked(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.SaveChecklistClicked>()
            .withReadyState()
            .map { (ready, _) ->
                val flattenedItems = ready.checklist.flattenedItems
                val trackingParams =
                    bundleOf("checked_count" to flattenedItems.checkedCount(), "total_count" to flattenedItems.count())
                tracker.logEvent("save_checklist_clicked", trackingParams)
                commandsUseCase.storeCommands(ready.consolidatedCommands())
                null to Effect.CloseScreen
            }
    }

    private fun Flow<Event>.handleDeleteClicked(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.DeleteChecklistClicked>()
            .withReadyState()
            .map {
                tracker.logEvent("delete_checklist_clicked")
                null to Effect.ShowConfirmDeleteDialog()
            }
    }

    private fun Flow<Event>.handleDeleteConfirmed(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.ConfirmDeleteChecklistClicked>()
            .withReadyState()
            .map { (ready, _) ->
                tracker.logEvent("delete_checklist_confirmation_clicked")
                commandsUseCase.storeCommands(
                    ready.consolidatedCommands().plus(
                        ChecklistCommand.DeleteChecklistCommand(
                            checklistId = ready.originalChecklist.id,
                            commandId = UUID.randomUUID(),
                            Clock.System.now()
                        )
                    )
                )
                null to Effect.CloseScreen
            }
    }

    private fun Flow<Event>.handleBackClicked(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.BackClicked>()
            .withReadyState()
            .map { (ready, _) ->
                val event = if (canSafelyExit(ready)) {
                    Effect.CloseScreen
                } else {
                    Effect.ShowConfirmExitDialog()
                }
                null to event
            }
    }

    private fun canSafelyExit(ready: State.Ready): Boolean {
        return ready.commands.isEmpty() || isFreshChecklist(ready.commands)
    }

    private fun isFreshChecklist(commands: List<ChecklistCommand>): Boolean {
        return commands.size == 1 && commands.first() is ChecklistCommand.CreateChecklistCommand
    }

    private fun Flow<Event>.confirmExitClicked(): Flow<Pair<State?, Effect?>> {
        return filterIsInstance<Event.ConfirmExitClicked>()
            .withReadyState()
            .map {
                tracker.logEvent("exit_without_saving_confirmation_clicked")
                null to Effect.CloseScreen
            }
    }

    private fun <T> Flow<T>.withReadyState(): Flow<Pair<State.Ready, T>> {
        return flatMapLatest { event ->
            state
                .filterIsInstance<State.Ready>()
                .map { it to event }
                .take(1)
        }
    }
}
