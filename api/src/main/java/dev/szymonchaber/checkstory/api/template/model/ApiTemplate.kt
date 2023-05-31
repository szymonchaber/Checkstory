package dev.szymonchaber.checkstory.api.template.model

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiTemplate(
    val id: DtoUUID,
    val userId: String,
    val name: String,
    val description: String,
    val tasks: List<ApiTemplateTask> = listOf(),
    val reminders: List<ApiReminder> = listOf(),
    val createdAt: Instant,
    val isDeleted: Boolean = false
) {

    fun toTemplate(): ChecklistTemplate {
        return ChecklistTemplate(
            id = ChecklistTemplateId(id),
            title = name,
            description = description,
            items = tasks.map { it.toTask() },
            createdAt = createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime(),
            checklists = listOf(),
            reminders = reminders.map { it.toReminder() },
            isRemoved = isDeleted,
        )
    }
}
