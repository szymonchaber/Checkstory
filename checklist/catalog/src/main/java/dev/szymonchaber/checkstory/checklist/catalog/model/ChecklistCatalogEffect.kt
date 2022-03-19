package dev.szymonchaber.checkstory.checklist.catalog.model

sealed interface ChecklistCatalogEffect {

    data class CreateAndNavigateToChecklist(val basedOn: String) : ChecklistCatalogEffect
}
