package dev.szymonchaber.checkstory.checklist.history

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.LoadChecklistHistoryUseCase
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ChecklistHistoryViewModel @Inject constructor(
    private val loadChecklistHistoryUseCase: LoadChecklistHistoryUseCase
) : BaseViewModel<ChecklistHistoryEvent, ChecklistHistoryState, ChecklistHistoryEffect>(
    ChecklistHistoryState.initial
) {

    override fun buildMviFlow(eventFlow: Flow<ChecklistHistoryEvent>): Flow<Pair<ChecklistHistoryState?, ChecklistHistoryEffect?>> {
        return merge(
            eventFlow.handleLoadChecklistHistory(),
            eventFlow.handleChecklistClicked()
        )
    }

    private fun Flow<ChecklistHistoryEvent>.handleLoadChecklistHistory(): Flow<Pair<ChecklistHistoryState?, ChecklistHistoryEffect?>> {
        return filterIsInstance<ChecklistHistoryEvent.LoadChecklistHistory>()
            .flatMapLatest { event ->
                loadChecklistHistoryUseCase.loadChecklistHistory(event.templateId).map {
                    state.first().copy(historyLoadingState = HistoryLoadingState.Success(it)) to null
                }
            }
    }

    private fun Flow<ChecklistHistoryEvent>.handleChecklistClicked(): Flow<Pair<ChecklistHistoryState?, ChecklistHistoryEffect?>> {
        return filterIsInstance<ChecklistHistoryEvent.ChecklistClicked>()
            .map { event ->
                null to ChecklistHistoryEffect.NavigateToFillChecklistScreen(event.checklistId)
            }
    }
}
