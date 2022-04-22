package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

sealed interface TemplateLoadingState {

    data class Success(
        val checklistTemplate: ChecklistTemplate,
        val checkboxes: List<ViewTemplateCheckbox>
    ) : TemplateLoadingState {

        fun updateTemplate(block: ChecklistTemplate.() -> ChecklistTemplate): Success {
            return copy(checklistTemplate = checklistTemplate.block())
        }

        fun plusNewCheckbox(title: String): Success {
            return copy(
                checkboxes = checkboxes.plus(
                    ViewTemplateCheckbox.New(
                        TemplateCheckboxId(checkboxes.size.toLong()),
                        null,
                        title,
                        listOf()
                    )
                )
            )
        }

        fun minusCheckbox(checkbox: ViewTemplateCheckbox): Success {
            return copy(
                checkboxes = checkboxes.minus(checkbox)
            )
        }

        fun plusChildCheckbox(parent: ViewTemplateCheckbox, title: String): Success {
            return copy(
                checkboxes = checkboxes.updateById(parent.id) {
                    it.plusChildCheckbox(title)
                }
            )
        }

        fun changeCheckboxTitle(checkbox: ViewTemplateCheckbox, title: String): Success {
            return copy(
                checkboxes = checkboxes.updateById(checkbox.id) {
                    it.withUpdatedTitle(title)
                }
            )
        }

        fun changeChildCheckboxTitle(
            parent: ViewTemplateCheckbox,
            child: ViewTemplateCheckbox,
            title: String
        ): Success {
            return copy(
                checkboxes = checkboxes.updateById(parent.id) {
                    it.editChildCheckboxTitle(child, title)
                }
            )
        }

        fun minusChildCheckbox(parent: ViewTemplateCheckbox, viewTemplateCheckbox: ViewTemplateCheckbox): Success {
            return copy(
                checkboxes = checkboxes.updateById(parent.id) {
                    it.minusChildCheckbox(viewTemplateCheckbox)
                }
            )
        }

        companion object {

            fun fromTemplate(checklistTemplate: ChecklistTemplate): Success {
                return Success(
                    checklistTemplate,
                    checklistTemplate.items.map { ViewTemplateCheckbox.Existing.fromDomainModel(it) })
            }
        }
    }

    object Loading : TemplateLoadingState
}
