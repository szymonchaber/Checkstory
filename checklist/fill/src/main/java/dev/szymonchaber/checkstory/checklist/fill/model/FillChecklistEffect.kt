package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId

sealed interface FillChecklistEffect {

    data class NavigateToEditTemplate(val templateId: TemplateId) : FillChecklistEffect

    object CloseScreen : FillChecklistEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowNotesEditShelf : FillChecklistEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowConfirmDeleteDialog : FillChecklistEffect

    @Suppress("CanSealedSubClassBeObject")
    class ShowConfirmExitDialog : FillChecklistEffect
}
