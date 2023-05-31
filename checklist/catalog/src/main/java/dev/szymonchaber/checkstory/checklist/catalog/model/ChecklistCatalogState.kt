package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist

data class ChecklistCatalogState(
    val templatesLoadingState: ChecklistCatalogLoadingState,
    val recentChecklistsLoadingState: RecentChecklistsLoadingState,
    val isRefreshing: Boolean
) {

    companion object {

        val initial = ChecklistCatalogState(
            templatesLoadingState = ChecklistCatalogLoadingState.Loading,
            recentChecklistsLoadingState = RecentChecklistsLoadingState.Loading,
            isRefreshing = false
        )
    }
}

sealed interface RecentChecklistsLoadingState {

    class Success(val checklists: List<Checklist>) : RecentChecklistsLoadingState

    object Loading : RecentChecklistsLoadingState
}
