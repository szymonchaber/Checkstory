package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

sealed interface HomeEffect {

    data class CreateAndNavigateToChecklist(val basedOn: TemplateId) : HomeEffect

    data class NavigateToChecklist(val checklistId: ChecklistId) : HomeEffect

    data class NavigateToTemplateEdit(val templateId: TemplateId) : HomeEffect

    data class NavigateToTemplateHistory(val templateId: TemplateId) : HomeEffect

    data object NavigateToNewTemplate : HomeEffect

    data object NavigateToPaymentScreen : HomeEffect

    data object NavigateToAboutScreen : HomeEffect

    data object NavigateToOnboarding : HomeEffect

    data object ShowUnassignedPaymentDialog : HomeEffect

    data class NavigateToAccountScreen(val triggerPurchaseRestoration: Boolean) : HomeEffect
}
