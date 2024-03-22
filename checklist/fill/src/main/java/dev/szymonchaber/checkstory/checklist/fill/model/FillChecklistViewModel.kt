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

@HiltViewModel
class FillChecklistViewModel @Inject constructor(
    private val getChecklistToFillUseCase: GetChecklistToFillUseCase,
    private val createChecklistFromTemplateUseCase: CreateChecklistFromTemplateUseCase,
    private val tracker: Tracker,
    private val commandsUseCase: StoreCommandsUseCase
) :
    BaseViewModel<FillChecklistEvent, FillChecklistState, FillChecklistEffect>(
        FillChecklistState.Loading
    ) {

    override fun buildMviFlow(eventFlow: Flow<FillChecklistEvent>): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
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

    private fun Flow<FillChecklistEvent>.handleCheckChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.CheckChanged>()
            .withReadyState()
            .mapLatest { (success, event) ->
                tracker.logEvent("check_changed", bundleOf("checked" to event.newCheck))
                val updated = success.withUpdatedItemChecked(event.item.id, event.newCheck)
                run {
                    state.first()
                    updated
                } to null
            }
    }

    private fun Flow<FillChecklistEvent>.handleChildCheckChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.ChildCheckChanged>()
            .withReadyState()
            .mapLatest { (success, event) ->
                tracker.logEvent("child_check_changed", bundleOf("checked" to event.newCheck))
                val (_, child, newCheck) = event
                val updated = success.withUpdatedItemChecked(child.id, newCheck)
                run {
                    state.first()
                    updated
                } to null
            }
    }

    private fun Flow<FillChecklistEvent>.handleLoadChecklist(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.LoadChecklist>()
            .flatMapLatest { loadEvent ->
                getChecklistToFillUseCase.getChecklist(loadEvent.checklistId).map {
                    FillChecklistState.Ready(it) to null
                }
            }
    }

    private fun Flow<FillChecklistEvent>.handleCreateChecklist(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.CreateChecklistFromTemplate>()
            .flatMapLatest { loadEvent ->
                createChecklistFromTemplateUseCase.createChecklistFromTemplate(loadEvent.templateId)
                    .map {
                        val checklistLoadingState = FillChecklistState.Ready(it)
                            .copy(
                                commands = listOf(
                                    ChecklistCommand.CreateChecklistCommand(
                                        it.id,
                                        it.templateId,
                                        it.title,
                                        it.description,
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

    private fun Flow<FillChecklistEvent>.handleNotesClicked(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.NotesClicked>()
            .withReadyState()
            .map {
                state.first() to FillChecklistEffect.ShowNotesEditShelf()
            }
    }

    private fun Flow<FillChecklistEvent>.handleNotesChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.NotesChanged>()
            .withReadyState()
            .map { (success, event) ->
                val updatedLoadingState = success.withUpdatedNotes(event.notes)
                run {
                    state.first()
                    updatedLoadingState
                } to null
            }
    }

    private fun Flow<FillChecklistEvent>.handleEditClicked(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.EditTemplateClicked>()
            .flatMapLatest {
                state.filterIsInstance<FillChecklistState.Ready>().take(1)
            }
            .map {
                tracker.logEvent("checklist_edit_template_clicked")
                state.first() to FillChecklistEffect.NavigateToEditTemplate(it.checklist.templateId)
            }
    }

    private fun Flow<FillChecklistEvent>.handleSaveClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.SaveChecklistClicked>()
            .withReadyState()
            .map { (success, _) ->
                val flattenedItems = success.checklist.flattenedItems
                val trackingParams =
                    bundleOf("checked_count" to flattenedItems.checkedCount(), "total_count" to flattenedItems.count())
                tracker.logEvent("save_checklist_clicked", trackingParams)
                commandsUseCase.storeCommands(success.consolidatedCommands())
                null to FillChecklistEffect.CloseScreen
            }
    }

    private fun Flow<FillChecklistEvent>.handleDeleteClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.DeleteChecklistClicked>()
            .withReadyState()
            .map {
                tracker.logEvent("delete_checklist_clicked")
                null to FillChecklistEffect.ShowConfirmDeleteDialog()
            }
    }

    private fun Flow<FillChecklistEvent>.handleDeleteConfirmed(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.ConfirmDeleteChecklistClicked>()
            .withReadyState()
            .map { (success, _) ->
                tracker.logEvent("delete_checklist_confirmation_clicked")
                commandsUseCase.storeCommands(
                    success.consolidatedCommands().plus(
                        ChecklistCommand.DeleteChecklistCommand(
                            checklistId = success.originalChecklist.id,
                            commandId = UUID.randomUUID(),
                            Clock.System.now()
                        )
                    )
                )
                null to FillChecklistEffect.CloseScreen
            }
    }

    private fun Flow<FillChecklistEvent>.handleBackClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.BackClicked>()
            .withReadyState()
            .map { (success, _) ->
                val event = if (success.isChanged()) {
                    FillChecklistEffect.ShowConfirmExitDialog()
                } else {
                    FillChecklistEffect.CloseScreen
                }
                null to event
            }
    }

    private fun Flow<FillChecklistEvent>.confirmExitClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.ConfirmExitClicked>()
            .withReadyState()
            .map {
                tracker.logEvent("exit_without_saving_confirmation_clicked")
                null to FillChecklistEffect.CloseScreen
            }
    }

    private fun <T> Flow<T>.withReadyState(): Flow<Pair<FillChecklistState.Ready, T>> {
        return flatMapLatest { event ->
            state
                .filterIsInstance<FillChecklistState.Ready>()
                .map { it to event }
                .take(1)
        }
    }
}
