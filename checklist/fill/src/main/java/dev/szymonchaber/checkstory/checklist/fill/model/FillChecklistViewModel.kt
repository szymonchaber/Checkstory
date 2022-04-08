package dev.szymonchaber.checkstory.checklist.fill.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.CreateChecklistFromTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistToFillUseCase
import dev.szymonchaber.checkstory.domain.usecase.UpdateChecklistUseCase
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FillChecklistViewModel @Inject constructor(
    private val getChecklistToFillUseCase: GetChecklistToFillUseCase,
    private val createChecklistFromTemplateUseCase: CreateChecklistFromTemplateUseCase,
    private val updateChecklistUseCase: UpdateChecklistUseCase
) :
    BaseViewModel<FillChecklistEvent, FillChecklistState, FillChecklistEffect>(
        FillChecklistState.initial
    ) {

    override fun buildMviFlow(eventFlow: Flow<FillChecklistEvent>): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        val handleCreateChecklist = eventFlow.handleCreateChecklist()
        val handleLoadChecklist = eventFlow.handleLoadChecklist()
        val handleCheckChanged = eventFlow.handleCheckChanged()
        val handleNotesChanged = eventFlow.handleNotesChanged()
        val handleEditClicked = eventFlow.handleEditClicked()
        return merge(
            handleCreateChecklist,
            handleLoadChecklist,
            handleCheckChanged,
            handleNotesChanged,
            handleEditClicked
        )
    }

    private fun Flow<FillChecklistEvent>.handleCheckChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.CheckChanged>()
            .flatMapLatest { checkChanged ->
                val originalState = state.first()
                val state = state.map {
                    it.checklistLoadingState
                }
                    .filterIsInstance<ChecklistLoadingState.Success>()
                    .first()
                val updatedList = state.checklist.items.map {
                    if (it.id == checkChanged.item.id) {
                        it.copy(isChecked = checkChanged.newCheck)
                    } else {
                        it
                    }
                }
                val updatedState = state.checklist.copy(items = updatedList)
                updateChecklistUseCase.updateChecklist(updatedState)
                    .map {
                        originalState.copy(checklistLoadingState = state.copy(checklist = updatedState)) to null
                    }
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
                createChecklistFromTemplateUseCase.createChecklistFromTemplate(loadEvent.checklistTemplateId).map {
                    FillChecklistState(ChecklistLoadingState.Success(it)) to null
                }
            }
    }

    private fun Flow<FillChecklistEvent>.handleNotesChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.NotesChanged>()
            .flatMapLatest { notesChanged ->
                val originalState = state.first()
                val state = state
                    .map {
                        it.checklistLoadingState
                    }
                    .filterIsInstance<ChecklistLoadingState.Success>()
                    .first()
                updateChecklistUseCase.updateChecklist(state.checklist.copy(notes = notesChanged.notes))
                    .map {
                        originalState.copy(checklistLoadingState = state.copy(checklist = state.checklist.copy(notes = notesChanged.notes))) to null
                    }
            }
    }

    private fun Flow<FillChecklistEvent>.handleEditClicked(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.EditTemplateClicked>()
            .flatMapLatest {
                state.map { it.checklistLoadingState }.filterIsInstance<ChecklistLoadingState.Success>().take(1)
            }
            .map {
                state.first() to FillChecklistEffect.NavigateToEditTemplate(it.checklist.checklistTemplateId)
            }
    }
}
