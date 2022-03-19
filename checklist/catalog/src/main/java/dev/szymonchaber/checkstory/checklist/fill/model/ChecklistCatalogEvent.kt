package dev.szymonchaber.checkstory.checklist.fill.model

sealed interface ChecklistCatalogEvent {

    object LoadChecklistCatalog : ChecklistCatalogEvent
}
