package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate

sealed interface ChecklistCatalogLoadingState {

    class Success(val checklistTemplates: List<ChecklistTemplate>) : ChecklistCatalogLoadingState
    object Loading : ChecklistCatalogLoadingState
}
