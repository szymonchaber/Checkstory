package dev.szymonchaber.checkstory.domain.model.checklist.fill

import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import java.time.LocalDateTime

data class Checklist(
    val id: ChecklistId,
    val templateId: TemplateId,
    val title: String,
    val description: String,
    val items: List<Task>,
    val notes: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isRemoved: Boolean = false
) {

    val flattenedItems: List<Task>
        get() {
            return items.flatMap {
                flatten(it)
            }
        }

    private fun flatten(task: Task): List<Task> {
        return listOf(task) + task.children.flatMap { flatten(it) }
    }

    companion object {

        fun empty(id: ChecklistId): Checklist {
            val now = LocalDateTime.now()
            return Checklist(
                id = id,
                templateId = TemplateId.new(),
                title = "",
                description = "",
                items = listOf(),
                notes = "",
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
