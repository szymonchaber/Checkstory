package dev.szymonchaber.checkstory.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.fill.TaskId
import java.util.*

@Entity
data class CheckboxEntity(
    @PrimaryKey
    val checkboxId: UUID,
    val checklistId: UUID,
    val checkboxTitle: String,
    val isChecked: Boolean,
    val parentId: UUID?
) {

    fun toDomainTask(children: List<Task>): Task {
        return Task(
            TaskId(checkboxId),
            parentId?.let(::TaskId),
            ChecklistId(checklistId),
            checkboxTitle,
            isChecked,
            children
        )
    }

    companion object {

        fun fromDomainTask(task: Task): CheckboxEntity {
            return with(task) {
                CheckboxEntity(
                    checkboxId = id.id,
                    checklistId = checklistId.id,
                    checkboxTitle = title,
                    isChecked = isChecked,
                    parentId = parentId?.id
                )
            }
        }
    }
}
