package dev.szymonchaber.checkstory.checklist.catalog.model

data class ChecklistCatalogState(
    val templatesLoadingState: ChecklistCatalogLoadingState,
    val isRefreshing: Boolean
) {

    companion object {

        val initial = ChecklistCatalogState(
            templatesLoadingState = ChecklistCatalogLoadingState.Loading,
            isRefreshing = false
        )
    }
}
