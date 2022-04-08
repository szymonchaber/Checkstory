package dev.szymonchaber.checkstory.domain.model.checklist.template

data class ChecklistTemplate(
    val id: ChecklistTemplateId,
    val title: String,
    val description: String,
    val items: List<TemplateCheckbox>
)

data class TemplateCheckbox(val id: TemplateCheckboxId, val title: String)

object ChecklistFactory {

    fun createChecklistTemplate(
        title: String,
        description: String,
        vararg checkboxes: String,
        id: ChecklistTemplateId = ChecklistTemplateId(0),
    ): ChecklistTemplate {
        return ChecklistTemplate(
            id,
            title,
            description,
            checkboxes.map {
                TemplateCheckbox(TemplateCheckboxId(0), it)
            }
        )
    }
}
