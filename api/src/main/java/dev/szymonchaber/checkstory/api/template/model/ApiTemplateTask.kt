package dev.szymonchaber.checkstory.api.template.model

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiTemplateTask(
    val id: DtoUUID,
    val templateId: DtoUUID,
    val title: String,
    val sortPosition: Long,
    val children: List<ApiTemplateTask> = listOf()
) {

    fun toTask(parentId: TemplateCheckboxId? = null): TemplateCheckbox {
        val id = TemplateCheckboxId(id)
        return TemplateCheckbox(
            id = id,
            parentId = parentId,
            title = title,
            children = children.map { it.toTask(id) },
            sortPosition = sortPosition,
            templateId = TemplateId(templateId)
        )
    }
}
