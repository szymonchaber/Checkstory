package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template

sealed interface ChecklistCatalogLoadingState {

    class Success(val templates: List<Template>) : ChecklistCatalogLoadingState
    object Loading : ChecklistCatalogLoadingState
}
