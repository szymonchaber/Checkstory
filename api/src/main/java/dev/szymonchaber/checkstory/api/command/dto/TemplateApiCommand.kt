package dev.szymonchaber.checkstory.api.command.dto

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
internal sealed interface TemplateApiCommand : ApiCommand {

    val templateId: DtoUUID

    @Serializable
    @SerialName("createTemplate")
    data class CreateTemplateApiCommand(
        override val templateId: DtoUUID,
        override val commandId: DtoUUID,
        override val timestamp: Instant,
        val existingTemplateData: ApiExistingTemplateData?
    ) : TemplateApiCommand

    @Serializable
    @SerialName("editTemplateTitle")
    data class EditTemplateTitleApiCommand(
        override val templateId: DtoUUID,
        val newTitle: String,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand

    @Serializable
    @SerialName("editTemplateDescription")
    data class EditTemplateDescriptionApiCommand(
        override val templateId: DtoUUID,
        val newDescription: String,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand

    @Serializable
    @SerialName("addTemplateTask")
    data class AddTemplateTaskApiCommand(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        val parentTaskId: String?,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand

    @Serializable
    @SerialName("renameTemplateTask")
    data class RenameTemplateTaskApiCommand(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        val newTitle: String,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand

    @Serializable
    @SerialName("deleteTemplateTask")
    data class DeleteTemplateTaskApiCommand(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand

    @Serializable
    @SerialName("updateTemplateTasksPositions")
    data class UpdateTasksPositionsApiCommand(
        override val templateId: DtoUUID,
        val taskIdToLocalPosition: Map<DtoUUID, Long>,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand

    @Serializable
    @SerialName("moveTemplateTask")
    data class MoveTemplateTaskApiCommand(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        val newParentTaskId: DtoUUID?,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand

    @Serializable
    @SerialName("addOrUpdateTemplateReminder")
    data class AddOrUpdateTemplateReminderApiCommand(
        override val templateId: DtoUUID,
        val reminder: ApiTemplateCommandReminder,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand

    @Serializable
    @SerialName("deleteTemplateReminder")
    data class DeleteTemplateReminderApiCommand(
        override val templateId: DtoUUID,
        val reminderId: DtoUUID,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand

    @Serializable
    @SerialName("deleteTemplate")
    data class DeleteTemplateApiCommand(
        override val templateId: DtoUUID,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : TemplateApiCommand
}

@Serializable
internal data class ApiExistingTemplateData(
    val title: String,
    val description: String,
    val tasks: List<ApiExistingTemplateTask>,
    val reminders: List<ApiTemplateCommandReminder>,
) {

    companion object {

        fun fromTemplate(existingData: Template): ApiExistingTemplateData {
            return with(existingData) {
                ApiExistingTemplateData(
                    title,
                    description,
                    tasks.map(ApiExistingTemplateTask.Companion::fromDomain),
                    reminders.map(ApiTemplateCommandReminder.Companion::fromReminder)
                )
            }
        }
    }
}

@Serializable
internal data class ApiExistingTemplateTask(
    val id: DtoUUID,
    val parentId: DtoUUID?,
    val title: String,
    val children: List<ApiExistingTemplateTask>,
    val sortPosition: Long,
) {

    companion object {

        fun fromDomain(task: TemplateTask): ApiExistingTemplateTask {
            return with(task) {
                ApiExistingTemplateTask(
                    id.id,
                    parentId?.id,
                    title,
                    children.map { fromDomain(it) },
                    sortPosition,
                )
            }
        }
    }
}
