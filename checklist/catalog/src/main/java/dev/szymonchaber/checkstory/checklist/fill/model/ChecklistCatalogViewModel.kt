package dev.szymonchaber.checkstory.checklist.fill.model

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.szymonchaber.checkstory.common.mvi.BaseViewModel
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplatesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
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
        return merge(handleLoadChecklist)
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
}
