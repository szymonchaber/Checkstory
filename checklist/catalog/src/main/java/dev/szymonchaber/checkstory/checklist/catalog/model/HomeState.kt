package dev.szymonchaber.checkstory.checklist.catalog.model

data class HomeState(
    val templatesLoadingState: ChecklistCatalogLoadingState,
    val isRefreshing: Boolean
) {

    companion object {

        val initial = HomeState(
            templatesLoadingState = ChecklistCatalogLoadingState.Loading,
            isRefreshing = false
        )
    }
}
