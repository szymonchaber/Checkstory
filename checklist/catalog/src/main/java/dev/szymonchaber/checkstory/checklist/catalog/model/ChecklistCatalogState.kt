package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate

data class ChecklistCatalogState(val loadingState: ChecklistCatalogLoadingState) {

    companion object {

        val initial = ChecklistCatalogState(ChecklistCatalogLoadingState.Loading)

        fun success(checklistTemplates: List<ChecklistTemplate>): ChecklistCatalogState {
            return ChecklistCatalogState(ChecklistCatalogLoadingState.Success(checklistTemplates))
        }
    }
}
