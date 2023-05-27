package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

@Suppress("CanSealedSubClassBeObject")
sealed interface ChecklistCatalogEffect {

    data class CreateAndNavigateToChecklist(val basedOn: ChecklistTemplateId) :
        ChecklistCatalogEffect

    data class NavigateToChecklist(val checklistId: ChecklistId) : ChecklistCatalogEffect

    data class NavigateToTemplateEdit(val templateId: ChecklistTemplateId) : ChecklistCatalogEffect

    data class NavigateToTemplateHistory(val templateId: ChecklistTemplateId) : ChecklistCatalogEffect

    class NavigateToNewTemplate : ChecklistCatalogEffect

    class NavigateToPaymentScreen : ChecklistCatalogEffect

    class NavigateToAboutScreen : ChecklistCatalogEffect

    class NavigateToOnboarding : ChecklistCatalogEffect

    class ShowUnassignedPaymentDialog : ChecklistCatalogEffect

    class NavigateToAccountScreen : ChecklistCatalogEffect
}
