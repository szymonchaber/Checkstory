package dev.szymonchaber.checkstory.domain.model.checklist.template

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import java.time.LocalDateTime

data class ChecklistTemplate(
    val id: ChecklistTemplateId,
    val title: String,
    val description: String,
    val items: List<TemplateCheckbox>,
    val createdAt: LocalDateTime,
    val checklists: List<Checklist>,
    val reminders: List<Reminder>,
    val isRemoved: Boolean = false
) {

    val flattenedItems: List<TemplateCheckbox>
        get() {
            return items.flatMap {
                flatten(it)
            }
        }

    private fun flatten(checkbox: TemplateCheckbox): List<TemplateCheckbox> {
        return listOf(checkbox) + checkbox.children.flatMap { flatten(it) }
    }

    companion object {

        fun empty(id: ChecklistTemplateId, createdAt: LocalDateTime = LocalDateTime.now()): ChecklistTemplate {
            return ChecklistTemplate(
                id = id,
                title = "",
                description = "",
                items = listOf(),
                createdAt = createdAt,
                checklists = listOf(),
                reminders = listOf()
            )
        }
    }
}

data class TemplateCheckbox(
    val id: TemplateCheckboxId,
    val parentId: TemplateCheckboxId?,
    val title: String,
    val children: List<TemplateCheckbox>,
    val sortPosition: Long,
    val templateId: ChecklistTemplateId
)
