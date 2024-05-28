package dev.szymonchaber.checkstory.checklist.catalog.model

import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

sealed interface HomeEvent {

    data object LoadChecklistCatalog : HomeEvent

    data object GoToOnboarding : HomeEvent

    data class UseTemplateClicked(val template: Template) : HomeEvent

    data class RecentChecklistClicked(val checklistId: ChecklistId) : HomeEvent

    data class RecentChecklistClickedInTemplate(val checklistId: ChecklistId) : HomeEvent

    data class EditTemplateClicked(val templateId: TemplateId) : HomeEvent

    data object NewTemplateClicked : HomeEvent

    data object GetCheckstoryProClicked : HomeEvent

    data object AboutClicked : HomeEvent

    data object PulledToRefresh : HomeEvent

    data class TemplateHistoryClicked(val templateId: TemplateId) : HomeEvent

    data object UnassignedPaymentPresent : HomeEvent

    data object CreateAccountForPaymentClicked : HomeEvent

    data object AccountClicked : HomeEvent

    data class DeleteTemplateConfirmed(val templateId: TemplateId) : HomeEvent

    data class DeleteChecklistConfirmed(val checklistId: ChecklistId) : HomeEvent
}
