package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed interface ChecklistCatalogEffect {

    data class CreateAndNavigateToChecklist(val basedOn: ChecklistTemplateId) :
        ChecklistCatalogEffect

    data class NavigateToChecklist(val checklistId: ChecklistId) : ChecklistCatalogEffect

    data class NavigateToTemplateEdit(val templateId: ChecklistTemplateId) : ChecklistCatalogEffect

    data class NavigateToTemplateHistory(val templateId: ChecklistTemplateId) : ChecklistCatalogEffect

    @Suppress("CanSealedSubClassBeObject")
    class NavigateToNewTemplate : ChecklistCatalogEffect

    @Suppress("CanSealedSubClassBeObject")
    class NavigateToPaymentScreen : ChecklistCatalogEffect

    @Suppress("CanSealedSubClassBeObject")
    class NavigateToAboutScreen : ChecklistCatalogEffect

    @Suppress("CanSealedSubClassBeObject")
    class NavigateToOnboarding : ChecklistCatalogEffect
}
