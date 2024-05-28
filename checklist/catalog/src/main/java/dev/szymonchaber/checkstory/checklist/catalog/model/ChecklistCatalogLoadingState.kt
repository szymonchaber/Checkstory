package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.Template

sealed interface ChecklistCatalogLoadingState {

    data class Success(val templates: List<Template>, val canAddTemplate: Boolean) : ChecklistCatalogLoadingState

    data object Loading : ChecklistCatalogLoadingState
}
