package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed interface ChecklistCatalogEvent {

    object LoadChecklistCatalog : ChecklistCatalogEvent

    object GoToOnboarding : ChecklistCatalogEvent

    data class NewChecklistFromTemplateClicked(val template: ChecklistTemplate) : ChecklistCatalogEvent

    data class RecentChecklistClicked(val checklistId: ChecklistId) : ChecklistCatalogEvent

    data class RecentChecklistClickedInTemplate(val checklistId: ChecklistId) : ChecklistCatalogEvent

    data class EditTemplateClicked(val templateId: ChecklistTemplateId) : ChecklistCatalogEvent

    object NewTemplateClicked : ChecklistCatalogEvent

    object GetCheckstoryProClicked : ChecklistCatalogEvent

    object AboutClicked : ChecklistCatalogEvent

    data class TemplateHistoryClicked(val templateId: ChecklistTemplateId) : ChecklistCatalogEvent
}
