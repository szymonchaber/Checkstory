package dev.szymonchaber.checkstory.checklist.fill.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.CreateChecklistFromTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.DeleteChecklistUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistToFillUseCase
import dev.szymonchaber.checkstory.domain.usecase.UpdateChecklistUseCase
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
    private val updateChecklistUseCase: UpdateChecklistUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase
) :
    BaseViewModel<FillChecklistEvent, FillChecklistState, FillChecklistEffect>(
        FillChecklistState.initial
    ) {

    override fun buildMviFlow(eventFlow: Flow<FillChecklistEvent>): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return merge(
            eventFlow.handleCreateChecklist(),
            eventFlow.handleLoadChecklist(),
            eventFlow.handleCheckChanged(),
            eventFlow.handleNotesChanged(),
            eventFlow.handleEditClicked(),
            eventFlow.handleSaveClicked(),
            eventFlow.handleDeleteClicked(),
            eventFlow.handleChildCheckChanged()
        )
    }

    private fun Flow<FillChecklistEvent>.handleCheckChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.CheckChanged>()
            .withSuccessState()
            .mapLatest { (success, event) ->
                val updated = success.updateChecklist {
                    copy(items = items.map {
                        if (it.id == event.item.id) {
                            it.copy(isChecked = event.newCheck)
                        } else {
                            it
                        }
                    }
                    )
                }
                state.first().copy(checklistLoadingState = updated) to null
            }
    }

    private fun Flow<FillChecklistEvent>.handleChildCheckChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.ChildCheckChanged>()
            .withSuccessState()
            .mapLatest { (success, event) ->
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
                state.first() to FillChecklistEffect.NavigateToEditTemplate(it.checklist.checklistTemplateId)
            }
    }

    private fun Flow<FillChecklistEvent>.handleSaveClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.SaveChecklistClicked>()
            .withSuccessState()
            .map { (success, _) ->
                updateChecklistUseCase.updateChecklist(success.checklist)
                null to FillChecklistEffect.CloseScreen
            }
    }

    private fun Flow<FillChecklistEvent>.handleDeleteClicked(): Flow<Pair<FillChecklistState?, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.DeleteChecklistClicked>()
            .withSuccessState()
            .map { (success, _) ->
                deleteChecklistUseCase.deleteChecklist(success.checklist)
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
