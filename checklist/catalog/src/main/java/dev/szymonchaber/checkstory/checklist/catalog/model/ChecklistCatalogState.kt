package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist

data class ChecklistCatalogState(
    val templatesLoadingState: ChecklistCatalogLoadingState,
    val recentChecklistsLoadingState: RecentChecklistsLoadingState
) {

    companion object {

        val initial = ChecklistCatalogState(ChecklistCatalogLoadingState.Loading, RecentChecklistsLoadingState.Loading)
    }
}

sealed interface RecentChecklistsLoadingState {

    class Success(val checklists: List<Checklist>) : RecentChecklistsLoadingState

    object Loading : RecentChecklistsLoadingState
}
