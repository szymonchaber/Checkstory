package dev.szymonchaber.checkstory.domain.model.checklist.template

import java.util.*

data class ChecklistTemplate(
    val id: ChecklistTemplateId,
    val title: String,
    val description: String,
    val items: List<TemplateCheckbox>
)

data class TemplateCheckbox(val title: String)

object ChecklistFactory {

    fun createChecklistTemplate(
        title: String,
        description: String,
        vararg checkboxes: String,
        id: ChecklistTemplateId = ChecklistTemplateId(UUID.randomUUID().toString()),
    ): ChecklistTemplate {
        return ChecklistTemplate(
            id,
            title,
            description,
            checkboxes.map {
                TemplateCheckbox(it)
            }
        )
    }
}
