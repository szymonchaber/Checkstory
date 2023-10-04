package dev.szymonchaber.checkstory.data.database.dao

import androidx.room.Embedded
import androidx.room.Relation
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.repository.ChecklistRepositoryImpl
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task

data class DeepChecklistEntity(
    @Embedded val checklist: ChecklistEntity,
    @Relation(
        parentColumn = "templateId",
        entityColumn = "id"
    )
    val template: ChecklistTemplateEntity,
    @Relation(
        parentColumn = "checklistId",
        entityColumn = "checklistId"
    )
    val tasks: List<CheckboxEntity>
) {

    fun toDomain(): Checklist {
        return checklist.toDomainChecklist(
            template.title,
            template.description,
            template.isRemoved,
            convertToNestedTasks(tasks)
        )
    }

    private fun convertToNestedTasks(entities: List<CheckboxEntity>): List<Task> {
        val entityMap = entities.associateBy { it.checkboxId }
        val tasks = entities.map { ChecklistRepositoryImpl.TaskToChildren(it) }
        tasks.forEach { task ->
            val parentId = task.task.parentId
            if (parentId != null) {
                val parent = entityMap[parentId]
                if (parent != null) {
                    val parentTask = tasks.firstOrNull { it.task.checkboxId == parent.checkboxId }
                    if (parentTask != null) {
                        parentTask.children += task
                    }
                }
            }
        }
        return tasks.filter { it.task.parentId == null }
            .map(::toDomain)
            .sortedBy(Task::sortPosition)
    }

    private fun toDomain(taskToChildren: ChecklistRepositoryImpl.TaskToChildren): Task {
        return taskToChildren.task
            .toDomainTask(taskToChildren.children.map { toDomain(it) }
                .sortedBy(Task::sortPosition))
    }
}
