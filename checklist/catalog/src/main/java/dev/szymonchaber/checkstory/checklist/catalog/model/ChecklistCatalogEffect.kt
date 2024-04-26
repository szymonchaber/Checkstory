package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

sealed interface ChecklistCatalogEffect {

    data class CreateAndNavigateToChecklist(val basedOn: TemplateId) : ChecklistCatalogEffect

    data class NavigateToChecklist(val checklistId: ChecklistId) : ChecklistCatalogEffect

    data class NavigateToTemplateEdit(val templateId: TemplateId) : ChecklistCatalogEffect

    data class NavigateToTemplateHistory(val templateId: TemplateId) : ChecklistCatalogEffect

    data object NavigateToNewTemplate : ChecklistCatalogEffect

    data object NavigateToPaymentScreen : ChecklistCatalogEffect

    data object NavigateToAboutScreen : ChecklistCatalogEffect

    data object NavigateToOnboarding : ChecklistCatalogEffect

    data object ShowUnassignedPaymentDialog : ChecklistCatalogEffect

    data class NavigateToAccountScreen(val triggerPurchaseRestoration: Boolean) : ChecklistCatalogEffect
}
