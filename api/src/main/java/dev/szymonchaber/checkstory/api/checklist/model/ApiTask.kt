package dev.szymonchaber.checkstory.api.checklist.model

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.fill.TaskId
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiTask(
    val id: DtoUUID,
    val checklistId: DtoUUID,
    val title: String,
    val sortPosition: Long,
    val isChecked: Boolean,
    val children: List<ApiTask> = listOf()
) {

    fun toTask(parentId: TaskId? = null): Task {
        val id = TaskId(id)
        return Task(
            id = id,
            parentId = parentId,
            title = title,
            children = children.sortedBy(ApiTask::sortPosition).map { it.toTask(id) },
            checklistId = ChecklistId(checklistId),
            isChecked = isChecked,
            sortPosition = sortPosition
        )
    }
}
