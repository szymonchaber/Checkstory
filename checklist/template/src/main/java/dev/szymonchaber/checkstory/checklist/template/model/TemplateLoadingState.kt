package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

sealed interface TemplateLoadingState {

    data class Success(val checklistTemplate: ChecklistTemplate, val newCheckboxes: List<TemplateCheckbox> = listOf()) :
        TemplateLoadingState {

        fun updateTemplate(block: ChecklistTemplate.() -> ChecklistTemplate): Success {
            return copy(checklistTemplate = checklistTemplate.block())
        }

        fun plusCheckbox(title: String): Success {
            return copy(
                newCheckboxes = newCheckboxes.plus(
                    TemplateCheckbox(TemplateCheckboxId(newCheckboxes.size.toLong()), title, listOf())
                )
            )
        }
    }

    object Loading : TemplateLoadingState
}
