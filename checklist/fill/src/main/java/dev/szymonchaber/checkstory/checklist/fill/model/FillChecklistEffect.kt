package dev.szymonchaber.checkstory.checklist.fill.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

sealed interface FillChecklistEffect {

    data class NavigateToEditTemplate(val templateId: ChecklistTemplateId) : FillChecklistEffect

    object CloseScreen : FillChecklistEffect
}
