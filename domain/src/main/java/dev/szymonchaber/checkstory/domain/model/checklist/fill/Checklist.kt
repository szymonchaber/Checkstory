package dev.szymonchaber.checkstory.domain.model.checklist.fill

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import java.time.LocalDateTime

data class Checklist(
    val id: ChecklistId,
    val checklistTemplateId: ChecklistTemplateId,
    val title: String,
    val description: String,
    val items: List<Checkbox>,
    val notes: String,
    val createdAt: LocalDateTime,
    val isRemoved: Boolean = false
) {

    val flattenedItems: List<Checkbox>
        get() {
            return items.flatMap {
                flatten(it)
            }
        }

    private fun flatten(checkbox: Checkbox): List<Checkbox> {
        return listOf(checkbox) + checkbox.children.flatMap { flatten(it) }
    }

    companion object {

        fun empty(id: ChecklistId): Checklist {
            return Checklist(
                id = id,
                checklistTemplateId = ChecklistTemplateId.new(),
                title = "",
                description = "",
                items = listOf(),
                notes = "",
                createdAt = LocalDateTime.now()
            )
        }
    }
}
