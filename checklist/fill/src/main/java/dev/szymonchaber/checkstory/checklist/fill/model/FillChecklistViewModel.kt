package dev.szymonchaber.checkstory.checklist.fill.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistToFillUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class FillChecklistViewModel @Inject constructor(
    private val getChecklistToFillUseCase: GetChecklistToFillUseCase
) :
    BaseViewModel<FillChecklistEvent, FillChecklistState, FillChecklistEffect>(
        FillChecklistState.initial
    ) {

    init {
        onEvent(FillChecklistEvent.LoadChecklist("listId"))
    }

    override fun buildMviFlow(eventFlow: Flow<FillChecklistEvent>): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        val handleLoadChecklist = eventFlow.handleLoadChecklist()
        val handleCheckChanged = eventFlow.handleCheckChanged()
        return merge(handleLoadChecklist, handleCheckChanged)
    }

    private fun Flow<FillChecklistEvent>.handleCheckChanged(): Flow<Pair<FillChecklistState, Nothing?>> {
        return filterIsInstance<FillChecklistEvent.CheckChanged>()
            .map { checkChanged ->
                val originalState = state.first()
                val state = state.map {
                    it.checklistLoadingState
                }
                    .filterIsInstance<ChecklistLoadingState.Success>()
                    .first()
                val checklist = state.checklist
                val updatedList = state.checklist.items.map {
                    if (it == checkChanged.item) {
                        it.copy(isChecked = checkChanged.newCheck)
                    } else {
                        it
                    }
                }
                originalState.copy(checklistLoadingState = state.copy(checklist = checklist.copy(items = updatedList))) to null
            }
    }

    private fun Flow<FillChecklistEvent>.handleLoadChecklist(): Flow<Pair<FillChecklistState, FillChecklistEffect?>> {
        return filterIsInstance<FillChecklistEvent.LoadChecklist>()
            .flatMapConcat { loadEvent ->
                getChecklistToFillUseCase.getChecklist(loadEvent.checklistId).map {
                    FillChecklistState(ChecklistLoadingState.Success(it)) to null
                }
            }
    }
}
