package dev.szymonchaber.checkstory.data.api.event

import dev.szymonchaber.checkstory.data.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import java.util.*
import javax.inject.Inject

internal class ChecklistsApi @Inject constructor(private val httpClient: HttpClient) {

    suspend fun getChecklists(): List<Checklist> {
        return httpClient.get("http://10.0.2.2:8080/checklists")
            .body<List<ApiChecklist>>()
            .map(ApiChecklist::toChecklist)
    }
}

@Serializable
data class ApiChecklist(
    val id: DtoUUID,
    val userId: String,
    val templateId: DtoUUID,
    val title: String,
    val description: String,
    val notes: String,
    val tasks: List<TaskDto> = listOf(),
    val isDeleted: Boolean = false
) {

    fun toChecklist(): Checklist {
        return Checklist(
            id = ChecklistId(id),
            checklistTemplateId = ChecklistTemplateId(templateId),
            title = title,
            description = description,
            items = tasks.map { it.toTask() },
            createdAt = java.time.LocalDateTime.now(), // TODO get from backend
            isRemoved = isDeleted,
            notes = notes
        )
    }
}

@Serializable
data class TaskDto(
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
            children = children.map { it.toTask(id) },
//            sortPosition = sortPosition
            checklistId = ChecklistId(UUID.randomUUID()),
            isChecked = isChecked
        )
    }
}
