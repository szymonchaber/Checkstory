package dev.szymonchaber.checkstory.api.event

import dev.szymonchaber.checkstory.api.ConfiguredHttpClient
import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import javax.inject.Inject

class ChecklistsApi @Inject constructor(private val httpClient: ConfiguredHttpClient) {

    suspend fun getChecklists(): List<Checklist> {
        return httpClient.get("checklists")
            .body<List<ApiChecklist>>()
            .map(ApiChecklist::toChecklist)
    }
}

@Serializable
internal data class ApiChecklist(
    val id: DtoUUID,
    val userId: String,
    val templateId: DtoUUID,
    val title: String,
    val description: String,
    val notes: String,
    val tasks: List<TaskDto> = listOf(),
    val createdAt: Instant,
    val isDeleted: Boolean = false
) {

    fun toChecklist(): Checklist {
        return Checklist(
            id = ChecklistId(id),
            checklistTemplateId = ChecklistTemplateId(templateId),
            title = title,
            description = description,
            items = tasks.sortedBy(TaskDto::sortPosition).map(TaskDto::toTask),
            createdAt = createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime(),
            isRemoved = isDeleted,
            notes = notes
        )
    }
}

@Serializable
internal data class TaskDto(
    val id: DtoUUID,
    val checklistId: DtoUUID,
    val title: String,
    val sortPosition: Long,
    val isChecked: Boolean,
    val children: List<TaskDto> = listOf()
) {

    fun toTask(parentId: CheckboxId? = null): Checkbox {
        val id = CheckboxId(id)
        return Checkbox(
            id = id,
            parentId = parentId,
            title = title,
            children = children.sortedBy(TaskDto::sortPosition).map { it.toTask(id) },
            checklistId = ChecklistId(checklistId),
            isChecked = isChecked
        )
    }
}
