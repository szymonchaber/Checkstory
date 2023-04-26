package dev.szymonchaber.checkstory.data.api.event.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface TemplateEventDto : EventDto {

    val templateId: String
}

@Serializable
@SerialName("createTemplate")
data class CreateTemplateEventDto(
    override val templateId: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateEventDto {

    override val eventType: String = "createTemplate"
}

@Serializable
@SerialName("editTemplateTitle")
data class EditTemplateTitleEventDto(
    override val templateId: String,
    val newTitle: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateEventDto {

    override val eventType: String = "editTemplateTitle"
}

@Serializable
@SerialName("editTemplateDescription")
data class EditTemplateDescriptionEventDto(
    override val templateId: String,
    val newDescription: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateEventDto {

    override val eventType: String = "editTemplateDescription"
}

@Serializable
@SerialName("addTemplateTask")
data class AddTemplateTaskEventDto(
    override val templateId: String,
    val taskId: String,
    val parentTaskId: String?,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateEventDto {

    override val eventType: String = "addTemplateTask"
}

@Serializable
@SerialName("renameTemplateTask")
data class RenameTemplateTaskEventDto(
    override val templateId: String,
    val taskId: String,
    val newTitle: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateEventDto {

    override val eventType: String = "renameTemplateTask"
}

@Serializable
@SerialName("deleteTemplateTask")
data class DeleteTemplateTaskEventDto(
    override val templateId: String,
    val taskId: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateEventDto {

    override val eventType: String = "deleteTemplateTask"
}
