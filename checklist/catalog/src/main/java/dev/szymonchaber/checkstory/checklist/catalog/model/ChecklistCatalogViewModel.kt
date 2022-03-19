package dev.szymonchaber.checkstory.checklist.catalog.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplatesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class ChecklistCatalogViewModel @Inject constructor(
    private val getChecklistTemplatesUseCase: GetChecklistTemplatesUseCase
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
        return merge(handleLoadChecklist, handleChecklistClicked)
    }

    private fun Flow<ChecklistCatalogEvent>.handleLoadChecklist(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.LoadChecklistCatalog>()
            .flatMapConcat {
                getChecklistTemplatesUseCase.getChecklistTemplates()
                    .map {
                        ChecklistCatalogState.success(it) to null
                    }
            }
    }

    private fun Flow<ChecklistCatalogEvent>.handleChecklistClicked(): Flow<Pair<ChecklistCatalogState, ChecklistCatalogEffect?>> {
        return filterIsInstance<ChecklistCatalogEvent.ChecklistTemplateClicked>()
            .map {
                state.first() to ChecklistCatalogEffect.CreateAndNavigateToChecklist(basedOn = it.checklistTemplateId)
            }
    }
}
