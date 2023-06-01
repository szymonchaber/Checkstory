package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

sealed interface ChecklistCatalogEvent {

    object LoadChecklistCatalog : ChecklistCatalogEvent

    object GoToOnboarding : ChecklistCatalogEvent

    data class NewChecklistFromTemplateClicked(val template: Template) : ChecklistCatalogEvent

    data class RecentChecklistClicked(val checklistId: ChecklistId) : ChecklistCatalogEvent

    data class RecentChecklistClickedInTemplate(val checklistId: ChecklistId) : ChecklistCatalogEvent

    data class EditTemplateClicked(val templateId: TemplateId) : ChecklistCatalogEvent

    object NewTemplateClicked : ChecklistCatalogEvent

    object GetCheckstoryProClicked : ChecklistCatalogEvent

    object AboutClicked : ChecklistCatalogEvent

    object PulledToRefresh : ChecklistCatalogEvent

    data class TemplateHistoryClicked(val templateId: TemplateId) : ChecklistCatalogEvent

    object UnassignedPaymentPresent : ChecklistCatalogEvent

    object CreateAccountForPaymentClicked : ChecklistCatalogEvent

    object AccountClicked : ChecklistCatalogEvent
}
