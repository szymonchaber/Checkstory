package dev.szymonchaber.checkstory.checklist.history

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.Tracker
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.LoadChecklistHistoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class ChecklistHistoryViewModel @Inject constructor(
    private val loadChecklistHistoryUseCase: LoadChecklistHistoryUseCase,
    private val tracker: Tracker
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
            .mapLatest { event ->
                val checklists = loadChecklistHistoryUseCase.loadChecklistHistory(event.templateId).orEmpty()
                state.first().copy(loadingState = HistoryLoadingState.Success(checklists)) to null
            }
    }

    private fun Flow<ChecklistHistoryEvent>.handleChecklistClicked(): Flow<Pair<ChecklistHistoryState?, ChecklistHistoryEffect?>> {
        return filterIsInstance<ChecklistHistoryEvent.ChecklistClicked>()
            .map { event ->
                tracker.logEvent("checklist_history_checklist_clicked")
                null to ChecklistHistoryEffect.NavigateToFillChecklistScreen(event.checklistId)
            }
    }
}
