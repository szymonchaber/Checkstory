package dev.szymonchaber.checkstory.checklist.template.model

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

sealed interface TemplateLoadingState {

    data class Success(
        val checklistTemplate: ChecklistTemplate,
        val checkboxes: List<ViewTemplateCheckbox>,
        val checkboxesToDelete: List<TemplateCheckbox>
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
            val shouldDeleteFromDatabase = checkbox is ViewTemplateCheckbox.Existing
            val updatedCheckboxesToDelete = if (shouldDeleteFromDatabase) {
                checkboxesToDelete.plus(checkbox.toDomainModel())
            } else {
                checkboxesToDelete
            }
            return copy(
                checkboxes = checkboxes.minus(checkbox),
                checkboxesToDelete = updatedCheckboxesToDelete
            )
        }

        fun plusChildCheckbox(parent: ViewTemplateCheckbox, title: String): Success {
            return copy(
                checkboxes = checkboxes.update(parent) {
                    it.plusChildCheckbox(title)
                }
            )
        }

        fun changeCheckboxTitle(checkbox: ViewTemplateCheckbox, title: String): Success {
            return copy(
                checkboxes = checkboxes.update(checkbox) {
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
                checkboxes = checkboxes.update(parent) {
                    it.editChildCheckboxTitle(child, title)
                }
            )
        }

        fun minusChildCheckbox(parent: ViewTemplateCheckbox, viewTemplateCheckbox: ViewTemplateCheckbox): Success {
            val shouldDeleteFromDatabase = viewTemplateCheckbox is ViewTemplateCheckbox.Existing
            val updatedCheckboxesToDelete = if (shouldDeleteFromDatabase) {
                checkboxesToDelete.plus(viewTemplateCheckbox.toDomainModel())
            } else {
                checkboxesToDelete
            }
            return copy(
                checkboxes = checkboxes.update(parent) {
                    it.minusChildCheckbox(viewTemplateCheckbox)
                },
                checkboxesToDelete = updatedCheckboxesToDelete
            )
        }

        companion object {

            fun fromTemplate(checklistTemplate: ChecklistTemplate): Success {
                return Success(
                    checklistTemplate,
                    checklistTemplate.items.map { ViewTemplateCheckbox.Existing.fromDomainModel(it) },
                    listOf()
                )
            }
        }
    }

    object Loading : TemplateLoadingState
}
