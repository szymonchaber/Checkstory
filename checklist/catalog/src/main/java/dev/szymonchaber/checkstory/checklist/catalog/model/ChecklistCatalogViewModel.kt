package dev.szymonchaber.checkstory.checklist.catalog.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplatesUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetRecentChecklistsUseCase
import kotlinx.coroutines.flow.*
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
        val loadChecklist = eventFlow.handleLoadChecklist()
        val templateClicked = eventFlow.handleTemplateClicked()
        val recentChecklistClicked = eventFlow.handleRecentChecklistClicked()
        val editTemplateClicked = eventFlow.handleEditTemplateClicked()
        return merge(loadChecklist, templateClicked, recentChecklistClicked, editTemplateClicked)
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

    private fun Flow<ChecklistCatalogEvent>.handleTemplateClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.ChecklistTemplateClicked>()
            .map {
                state.first() to ChecklistCatalogEffect.CreateAndNavigateToChecklist(basedOn = it.templateId)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleRecentChecklistClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.RecentChecklistClicked>()
            .map {
                state.first() to ChecklistCatalogEffect.NavigateToChecklist(checklistId = it.checklistId)
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleEditTemplateClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.EditChecklistTemplateClicked>()
            .map {
                state.first() to ChecklistCatalogEffect.NavigateToTemplateEdit(templateId = it.templateId)
            }
    }
}
