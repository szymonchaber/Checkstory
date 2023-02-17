package dev.szymonchaber.checkstory.checklist.fill.model

import androidx.core.os.bundleOf
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox.Companion.checkedCount
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.usecase.CreateChecklistFromTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteChecklistUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistToFillUseCase
import dev.szymonchaber.checkstory.domain.usecase.SaveChecklistUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class FillChecklistViewModel @Inject constructor(
    private val getChecklistToFillUseCase: GetChecklistToFillUseCase,
    private val createChecklistFromTemplateUseCase: CreateChecklistFromTemplateUseCase,
    private val saveChecklistUseCase: SaveChecklistUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
    private val tracker: Tracker
) :
    BaseViewModel<FillChecklistEvent, FillChecklistState, FillChecklistEffect>(
        FillChecklistState.initial
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
            .withSuccessState()
            .mapLatest { (success, event) ->
                tracker.logEvent("check_changed", bundleOf("checked" to event.newCheck))
                val updated = success.updateChecklist {
                    copy(
                        items = items.map {
                            it.withUpdatedCheckRecursive(event.item, event.newCheck)
                        }
                    )
                }
                state.first().copy(checklistLoadingState = updated) to null
            }
    }

    private fun Checkbox.withUpdatedCheckRecursive(checkbox: Checkbox, newIsChecked: Boolean): Checkbox {
        return copy(
            isChecked = if (id == checkbox.id) {
                newIsChecked
            } else {
                isChecked
            },
            children = children.map { it.withUpdatedCheckRecursive(checkbox, newIsChecked) }
        )
    }

    private fun Flow<FillChecklistEvent>.handleChildCheckChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.ChildCheckChanged>()
            .withSuccessState()
            .mapLatest { (success, event) ->
                tracker.logEvent("child_check_changed", bundleOf("checked" to event.newCheck))
                val (checkbox, child, newCheck) = event
                val updated = success.updateChecklist {
                    copy(items = items.map {
                        if (it.id == checkbox.id) {
                            it.copy(children = it.children.map {
                                if (it.id == child.id) {
                                    it.copy(isChecked = newCheck)
                                } else {
                                    it
                                }
                            }
                            )
                        } else {
                            it
                        }
                    }
                    )
                }
                state.first().copy(checklistLoadingState = updated) to null
            }
    }

    private fun Flow<FillChecklistEvent>.handleLoadChecklist(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.LoadChecklist>()
            .flatMapLatest { loadEvent ->
                getChecklistToFillUseCase.getChecklist(loadEvent.checklistId).map {
                    FillChecklistState(ChecklistLoadingState.Success(it)) to null
                }
            }
    }

    private fun Flow<FillChecklistEvent>.handleCreateChecklist(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.CreateChecklistFromTemplate>()
            .flatMapLatest { loadEvent ->
                createChecklistFromTemplateUseCase.createChecklistFromTemplate(loadEvent.checklistTemplateId)
                    .map {
                        FillChecklistState(ChecklistLoadingState.Success(it)) to null
                    }
            }
    }

    private fun Flow<FillChecklistEvent>.handleNotesClicked(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.NotesClicked>()
            .withSuccessState()
            .map {
                state.first() to FillChecklistEffect.ShowNotesEditShelf()
            }
    }

    private fun Flow<FillChecklistEvent>.handleNotesChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.NotesChanged>()
            .withSuccessState()
            .map { (success, event) ->
                val updatedLoadingState = success.updateChecklist {
                    copy(notes = event.notes)
                }
                state.first().copy(checklistLoadingState = updatedLoadingState) to null
            }
    }

    private fun Flow<FillChecklistEvent>.handleEditClicked(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.EditTemplateClicked>()
            .flatMapLatest {
                state.map { it.checklistLoadingState }
                    .filterIsInstance<ChecklistLoadingState.Success>().take(1)
            }
            .map {
                tracker.logEvent("checklist_edit_template_clicked")
                state.first() to FillChecklistEffect.NavigateToEditTemplate(it.checklist.checklistTemplateId)
            }
    }

    private fun Flow<FillChecklistEvent>.handleSaveClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.SaveChecklistClicked>()
            .withSuccessState()
            .map { (success, _) ->
                val checklistToStore = if (success.checklist.isStored) {
                    success.checklist
                } else {
                    val itemsWithoutTemporaryIds = success.checklist.items.map { checkbox ->
                        checkbox.clearIdsRecursive()
                    }
                    success.checklist.copy(items = itemsWithoutTemporaryIds)
                }
                val flattenedItems = checklistToStore.flattenedItems
                val trackingParams =
                    bundleOf("checked_count" to flattenedItems.checkedCount(), "total_count" to flattenedItems.count())
                tracker.logEvent("save_checklist_clicked", trackingParams)
                saveChecklistUseCase.saveChecklist(checklistToStore.copy(notes = checklistToStore.notes.trimEnd()))
                null to FillChecklistEffect.CloseScreen
            }
    }

    private fun Flow<FillChecklistEvent>.handleDeleteClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.DeleteChecklistClicked>()
            .withSuccessState()
            .map {
                tracker.logEvent("delete_checklist_clicked")
                null to FillChecklistEffect.ShowConfirmDeleteDialog()
            }
    }

    private fun Flow<FillChecklistEvent>.handleDeleteConfirmed(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.ConfirmDeleteChecklistClicked>()
            .withSuccessState()
            .map { (success, _) ->
                tracker.logEvent("delete_checklist_confirmation_clicked")
                if (success.checklist.isStored) {
                    deleteChecklistUseCase.deleteChecklist(success.checklist)
                }
                null to FillChecklistEffect.CloseScreen
            }
    }

    private fun Flow<FillChecklistEvent>.handleBackClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.BackClicked>()
            .withSuccessState()
            .map { (success, _) ->
                val event = if (success.isChanged() || !success.checklist.isStored) {
                    FillChecklistEffect.ShowConfirmExitDialog()
                } else {
                    FillChecklistEffect.CloseScreen
                }
                null to event
            }
    }

    private fun Flow<FillChecklistEvent>.confirmExitClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.ConfirmExitClicked>()
            .withSuccessState()
            .map {
                tracker.logEvent("exit_without_saving_confirmation_clicked")
                null to FillChecklistEffect.CloseScreen
            }
    }

    private fun <T> Flow<T>.withSuccessState(): Flow<Pair<ChecklistLoadingState.Success, T>> {
        return flatMapLatest { event ->
            state.map { it.checklistLoadingState }
                .filterIsInstance<ChecklistLoadingState.Success>()
                .map { it to event }
                .take(1)
        }
    }
}

private fun Checkbox.clearIdsRecursive(): Checkbox {
    return copy(
        id = CheckboxId(0),
        children = children.map {
            it.clearIdsRecursive()
        }
    )
}
