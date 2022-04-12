package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate

sealed interface TemplateLoadingState {

    data class Success(val checklistTemplate: ChecklistTemplate) : TemplateLoadingState {

        fun updateTemplate(block: ChecklistTemplate.() -> ChecklistTemplate): Success {
            return copy(checklistTemplate = checklistTemplate.block())
        }
    }

    object Loading : TemplateLoadingState
}
