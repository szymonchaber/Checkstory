package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed interface ChecklistCatalogEvent {

    object LoadChecklistCatalog : ChecklistCatalogEvent

    data class ChecklistTemplateClicked(val checklistTemplateId: ChecklistTemplateId) : ChecklistCatalogEvent

    class RecentChecklistClicked(val checklistId: ChecklistId) : ChecklistCatalogEvent
}
