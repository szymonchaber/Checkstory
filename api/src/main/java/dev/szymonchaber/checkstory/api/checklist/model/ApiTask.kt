package dev.szymonchaber.checkstory.api.checklist.model

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
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

    fun toTask(parentId: CheckboxId? = null): Checkbox {
        val id = CheckboxId(id)
        return Checkbox(
            id = id,
            parentId = parentId,
            title = title,
            children = children.sortedBy(ApiTask::sortPosition).map { it.toTask(id) },
            checklistId = ChecklistId(checklistId),
            isChecked = isChecked
        )
    }
}
