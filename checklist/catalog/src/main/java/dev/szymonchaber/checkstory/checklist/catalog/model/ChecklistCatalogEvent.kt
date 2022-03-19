package dev.szymonchaber.checkstory.checklist.catalog.model

sealed interface ChecklistCatalogEvent {

    object LoadChecklistCatalog : ChecklistCatalogEvent

    data class ChecklistTemplateClicked(val checklistTemplateId: String) : ChecklistCatalogEvent
}
