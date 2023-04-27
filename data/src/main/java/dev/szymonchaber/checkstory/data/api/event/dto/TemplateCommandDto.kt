package dev.szymonchaber.checkstory.data.api.event.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface TemplateCommandDto : CommandDto {

    val templateId: String
}

@Serializable
@SerialName("createTemplate")
data class CreateTemplateCommandDto(
    override val templateId: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto {

    override val commandType: String = "createTemplate"
}

@Serializable
@SerialName("editTemplateTitle")
data class EditTemplateTitleCommandDto(
    override val templateId: String,
    val newTitle: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto {

    override val commandType: String = "editTemplateTitle"
}

@Serializable
@SerialName("editTemplateDescription")
data class EditTemplateDescriptionCommandDto(
    override val templateId: String,
    val newDescription: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto {

    override val commandType: String = "editTemplateDescription"
}

@Serializable
@SerialName("addTemplateTask")
data class AddTemplateTaskCommandDto(
    override val templateId: String,
    val taskId: String,
    val parentTaskId: String?,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto {

    override val commandType: String = "addTemplateTask"
}

@Serializable
@SerialName("renameTemplateTask")
data class RenameTemplateTaskCommandDto(
    override val templateId: String,
    val taskId: String,
    val newTitle: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto {

    override val commandType: String = "renameTemplateTask"
}

@Serializable
@SerialName("deleteTemplateTask")
data class DeleteTemplateTaskCommandDto(
    override val templateId: String,
    val taskId: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto {

    override val commandType: String = "deleteTemplateTask"
}

@Serializable
@SerialName("updateTemplateTasksPositions")
data class UpdateTasksPositionsCommandDto(
    override val templateId: String,
    val taskIdToLocalPosition: Map<String, Long>,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto {

    override val commandType: String = "updateTemplateTasksPositions"
}
