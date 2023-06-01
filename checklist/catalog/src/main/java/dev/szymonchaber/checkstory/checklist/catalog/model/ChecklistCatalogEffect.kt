package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

@Suppress("CanSealedSubClassBeObject")
sealed interface ChecklistCatalogEffect {

    data class CreateAndNavigateToChecklist(val basedOn: TemplateId) :
        ChecklistCatalogEffect

    data class NavigateToChecklist(val checklistId: ChecklistId) : ChecklistCatalogEffect

    data class NavigateToTemplateEdit(val templateId: TemplateId) : ChecklistCatalogEffect

    data class NavigateToTemplateHistory(val templateId: TemplateId) : ChecklistCatalogEffect

    class NavigateToNewTemplate : ChecklistCatalogEffect

    class NavigateToPaymentScreen : ChecklistCatalogEffect

    class NavigateToAboutScreen : ChecklistCatalogEffect

    class NavigateToOnboarding : ChecklistCatalogEffect

    class ShowUnassignedPaymentDialog : ChecklistCatalogEffect

    class NavigateToAccountScreen : ChecklistCatalogEffect
}
