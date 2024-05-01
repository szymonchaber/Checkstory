package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

sealed interface ChecklistCatalogEvent {

    data object LoadChecklistCatalog : ChecklistCatalogEvent

    data object GoToOnboarding : ChecklistCatalogEvent

    data class UseTemplateClicked(val template: Template) : ChecklistCatalogEvent

    data class RecentChecklistClicked(val checklistId: ChecklistId) : ChecklistCatalogEvent

    data class RecentChecklistClickedInTemplate(val checklistId: ChecklistId) : ChecklistCatalogEvent

    data class EditTemplateClicked(val templateId: TemplateId) : ChecklistCatalogEvent

    data object NewTemplateClicked : ChecklistCatalogEvent

    data object GetCheckstoryProClicked : ChecklistCatalogEvent

    data object AboutClicked : ChecklistCatalogEvent

    data object PulledToRefresh : ChecklistCatalogEvent

    data class TemplateHistoryClicked(val templateId: TemplateId) : ChecklistCatalogEvent

    data object UnassignedPaymentPresent : ChecklistCatalogEvent

    data object CreateAccountForPaymentClicked : ChecklistCatalogEvent

    data object AccountClicked : ChecklistCatalogEvent

    data class DeleteTemplateConfirmed(val templateId: TemplateId) : ChecklistCatalogEvent

    data class DeleteChecklistConfirmed(val checklistId: ChecklistId) : ChecklistCatalogEvent
}
