package dev.szymonchaber.checkstory.api.template.model

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiTemplateTask(
    val id: DtoUUID,
    val templateId: DtoUUID,
    val title: String,
    val sortPosition: Long,
    val children: List<ApiTemplateTask> = listOf()
) {

    fun toTask(parentId: TemplateTaskId? = null): TemplateTask {
        val id = TemplateTaskId(id)
        return TemplateTask(
            id = id,
            parentId = parentId,
            title = title,
            children = children.map { it.toTask(id) },
            sortPosition = sortPosition,
            templateId = TemplateId(templateId)
        )
    }
}
