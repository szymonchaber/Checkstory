package dev.szymonchaber.checkstory.domain.model.checklist.template

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import java.time.LocalDateTime

data class Template(
    val id: TemplateId,
    val title: String,
    val description: String,
    val tasks: List<TemplateTask>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val checklists: List<Checklist>,
    val reminders: List<Reminder>,
    val isRemoved: Boolean = false
) {

    val flattenedTasks: List<TemplateTask>
        get() {
            return tasks.flatMap {
                flatten(it)
            }
        }

    private fun flatten(task: TemplateTask): List<TemplateTask> {
        return listOf(task) + task.children.flatMap { flatten(it) }
    }

    companion object {

        fun empty(id: TemplateId, createdAt: LocalDateTime = LocalDateTime.now()): Template {
            return Template(
                id = id,
                title = "",
                description = "",
                tasks = listOf(),
                createdAt = createdAt,
                updatedAt = createdAt,
                checklists = listOf(),
                reminders = listOf()
            )
        }
    }
}

data class TemplateTask(
    val id: TemplateTaskId,
    val parentId: TemplateTaskId?,
    val title: String,
    val children: List<TemplateTask>,
    val sortPosition: Long,
    val templateId: TemplateId
)
