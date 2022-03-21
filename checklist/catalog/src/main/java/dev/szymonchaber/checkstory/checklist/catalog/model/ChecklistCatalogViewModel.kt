package dev.szymonchaber.checkstory.checklist.catalog.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplatesUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetRecentChecklistsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class ChecklistCatalogViewModel @Inject constructor(
    private val getChecklistTemplatesUseCase: GetChecklistTemplatesUseCase,
    private val getRecentChecklistsUseCase: GetRecentChecklistsUseCase
) :
    BaseViewModel<
            ChecklistCatalogEvent,
            ChecklistCatalogState,
            ChecklistCatalogEffect
            >(
        ChecklistCatalogState.initial
    ) {

    init {
        onEvent(ChecklistCatalogEvent.LoadChecklistCatalog)
    }

    override fun buildMviFlow(eventFlow: Flow<ChecklistCatalogEvent>): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        val handleLoadChecklist = eventFlow.handleLoadChecklist()
        val handleChecklistClicked = eventFlow.handleChecklistClicked()
        val handleRecentChecklistClicked = eventFlow.handleRecentChecklistClicked()
        return merge(handleLoadChecklist, handleChecklistClicked, handleRecentChecklistClicked)
    }

    private fun Flow<ChecklistCatalogEvent>.handleLoadChecklist(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.LoadChecklistCatalog>()
            .flatMapLatest {
                val templatesLoading = getChecklistTemplatesUseCase.getChecklistTemplates()
                    .map {
                        ChecklistCatalogLoadingState.Success(it)
                    }.onStart<ChecklistCatalogLoadingState> {
                        emit(ChecklistCatalogLoadingState.Loading)
                    }
                val recentChecklistsLoading = getRecentChecklistsUseCase.getRecentChecklists()
                    .map {
                        RecentChecklistsLoadingState.Success(it)
                    }.onStart<RecentChecklistsLoadingState> {
                        emit(RecentChecklistsLoadingState.Loading)
                    }

                templatesLoading.combine(recentChecklistsLoading) { templates, checklists ->
                    state.first()
                        .copy(templatesLoadingState = templates, recentChecklistsLoadingState = checklists) to null
                }
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleChecklistClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.ChecklistTemplateClicked>()
            .map {
                state.first() to ChecklistCatalogEffect.CreateAndNavigateToChecklist(basedOn = it.checklistTemplateId)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleRecentChecklistClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.RecentChecklistClicked>()
            .map {
                state.first() to ChecklistCatalogEffect.NavigateToChecklist(checklistId = it.checklistId)
            }
    }
}