package dev.szymonchaber.checkstory.checklist.fill.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.CreateChecklistFromTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistToFillUseCase
import dev.szymonchaber.checkstory.domain.usecase.UpdateChecklistUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
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
        return merge(handleCreateChecklist, handleLoadChecklist, handleCheckChanged, handleNotesChanged)
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
                    if (it == checkChanged.item) {
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
}
