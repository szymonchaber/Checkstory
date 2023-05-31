package dev.szymonchaber.checkstory.api.checklist.model

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiChecklist(
    val id: DtoUUID,
    val userId: String,
    val templateId: DtoUUID,
    val title: String,
    val description: String,
    val notes: String,
    val tasks: List<ApiTask> = listOf(),
    val createdAt: Instant,
    val isDeleted: Boolean = false
) {

    fun toChecklist(): Checklist {
        return Checklist(
            id = ChecklistId(id),
            checklistTemplateId = ChecklistTemplateId(templateId),
            title = title,
            description = description,
            items = tasks.sortedBy(ApiTask::sortPosition).map(ApiTask::toTask),
            createdAt = createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime(),
            isRemoved = isDeleted,
            notes = notes
        )
    }
}
