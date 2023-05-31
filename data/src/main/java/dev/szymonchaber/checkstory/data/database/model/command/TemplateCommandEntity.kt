package dev.szymonchaber.checkstory.data.database.model.command

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.data.api.event.dto.ReminderEntity
import dev.szymonchaber.checkstory.data.api.event.dto.toReminder
import dev.szymonchaber.checkstory.data.api.event.dto.toReminderEntity
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface TemplateCommandEntity : CommandDataEntity {

    val templateId: DtoUUID

    @Serializable
    @SerialName("createTemplate")
    data class CreateNewTemplateEntity(
        override val templateId: DtoUUID,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("editTemplateTitle")
    data class RenameTemplateEntity(
        override val templateId: DtoUUID,
        val newTitle: String,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("editTemplateDescription")
    data class ChangeTemplateDescriptionEntity(
        override val templateId: DtoUUID,
        val newDescription: String,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("addTemplateTask")
    data class AddTemplateTaskEntity(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        val parentTaskId: DtoUUID?,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("renameTemplateTask")
    data class RenameTemplateTaskEntity(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        val newTitle: String,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("deleteTemplateTask")
    data class DeleteTemplateTaskEntity(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("updateTemplateTasksPositions")
    data class UpdateCheckboxPositionsEntity(
        val localPositions: Map<DtoUUID, Long>,
        override val timestamp: Instant,
        override val commandId: DtoUUID,
        override val templateId: DtoUUID
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("moveTemplateTask")
    data class MoveTemplateTaskEntity(
        val taskId: DtoUUID,
        val newParentTaskId: DtoUUID?,
        override val timestamp: Instant,
        override val commandId: DtoUUID,
        override val templateId: DtoUUID
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("addOrUpdateTemplateReminder")
    data class AddOrReplaceTemplateReminderEntity(
        override val templateId: DtoUUID,
        val reminder: ReminderEntity,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("deleteTemplateReminder")
    data class DeleteTemplateReminderEntity(
        override val templateId: DtoUUID,
        val reminderId: DtoUUID,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("deleteTemplate")
    data class DeleteTemplateEntity(
        override val templateId: DtoUUID,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    fun toDomainCommand(): TemplateDomainCommand {
        return when (this) {
            is CreateNewTemplateEntity -> TemplateDomainCommand.CreateNewTemplate(
                templateId = ChecklistTemplateId(templateId),
                timestamp = timestamp,
                commandId = commandId
            )

            is RenameTemplateEntity -> TemplateDomainCommand.RenameTemplate(
                templateId = ChecklistTemplateId(templateId),
                newTitle = newTitle,
                timestamp = timestamp,
                commandId = commandId
            )

            is ChangeTemplateDescriptionEntity -> TemplateDomainCommand.ChangeTemplateDescription(
                templateId = ChecklistTemplateId(templateId),
                newDescription = newDescription,
                timestamp = timestamp,
                commandId = commandId
            )

            is AddTemplateTaskEntity -> TemplateDomainCommand.AddTemplateTask(
                templateId = ChecklistTemplateId(templateId),
                taskId = TemplateCheckboxId(taskId),
                parentTaskId = parentTaskId?.let { TemplateCheckboxId(it) },
                timestamp = timestamp,
                commandId = commandId
            )

            is RenameTemplateTaskEntity -> TemplateDomainCommand.RenameTemplateTask(
                templateId = ChecklistTemplateId(templateId),
                taskId = TemplateCheckboxId(taskId),
                newTitle = newTitle,
                timestamp = timestamp,
                commandId = commandId
            )

            is DeleteTemplateTaskEntity -> TemplateDomainCommand.DeleteTemplateTask(
                templateId = ChecklistTemplateId(templateId),
                taskId = TemplateCheckboxId(taskId),
                timestamp = timestamp,
                commandId = commandId
            )

            is UpdateCheckboxPositionsEntity -> TemplateDomainCommand.UpdateCheckboxPositions(
                localPositions = localPositions.mapKeys { TemplateCheckboxId(it.key) },
                timestamp = timestamp,
                commandId = commandId,
                templateId = ChecklistTemplateId(templateId)
            )

            is MoveTemplateTaskEntity -> TemplateDomainCommand.MoveTemplateTask(
                taskId = TemplateCheckboxId(taskId),
                newParentTaskId = newParentTaskId?.let { TemplateCheckboxId(it) },
                timestamp = timestamp,
                commandId = commandId,
                templateId = ChecklistTemplateId(templateId)
            )

            is AddOrReplaceTemplateReminderEntity -> TemplateDomainCommand.AddOrReplaceTemplateReminder(
                templateId = ChecklistTemplateId(templateId),
                reminder = reminder.toReminder(),
                timestamp = timestamp,
                commandId = commandId
            )

            is DeleteTemplateReminderEntity -> TemplateDomainCommand.DeleteTemplateReminder(
                templateId = ChecklistTemplateId(templateId),
                reminderId = ReminderId(reminderId),
                timestamp = timestamp,
                commandId = commandId
            )

            is DeleteTemplateEntity -> TemplateDomainCommand.DeleteTemplate(
                templateId = ChecklistTemplateId(templateId),
                timestamp = timestamp,
                commandId = commandId
            )
        }
    }

    companion object {

        fun fromDomainCommand(templateDomainCommand: TemplateDomainCommand): TemplateCommandEntity {
            return when (templateDomainCommand) {
                is TemplateDomainCommand.CreateNewTemplate -> CreateNewTemplateEntity(
                    templateId = templateDomainCommand.templateId.id,
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId
                )

                is TemplateDomainCommand.RenameTemplate -> RenameTemplateEntity(
                    templateId = templateDomainCommand.templateId.id,
                    newTitle = templateDomainCommand.newTitle,
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId
                )

                is TemplateDomainCommand.ChangeTemplateDescription -> ChangeTemplateDescriptionEntity(
                    templateId = templateDomainCommand.templateId.id,
                    newDescription = templateDomainCommand.newDescription,
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId
                )

                is TemplateDomainCommand.AddTemplateTask -> AddTemplateTaskEntity(
                    templateId = templateDomainCommand.templateId.id,
                    taskId = templateDomainCommand.taskId.id,
                    parentTaskId = templateDomainCommand.parentTaskId?.id,
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId
                )

                is TemplateDomainCommand.RenameTemplateTask -> RenameTemplateTaskEntity(
                    templateId = templateDomainCommand.templateId.id,
                    taskId = templateDomainCommand.taskId.id,
                    newTitle = templateDomainCommand.newTitle,
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId
                )

                is TemplateDomainCommand.DeleteTemplateTask -> DeleteTemplateTaskEntity(
                    templateId = templateDomainCommand.templateId.id,
                    taskId = templateDomainCommand.taskId.id,
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId
                )

                is TemplateDomainCommand.UpdateCheckboxPositions -> UpdateCheckboxPositionsEntity(
                    localPositions = templateDomainCommand.localPositions.mapKeys { it.key.id },
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId,
                    templateId = templateDomainCommand.templateId.id
                )

                is TemplateDomainCommand.MoveTemplateTask -> MoveTemplateTaskEntity(
                    taskId = templateDomainCommand.taskId.id,
                    newParentTaskId = templateDomainCommand.newParentTaskId?.id,
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId,
                    templateId = templateDomainCommand.templateId.id
                )

                is TemplateDomainCommand.AddOrReplaceTemplateReminder -> AddOrReplaceTemplateReminderEntity(
                    templateId = templateDomainCommand.templateId.id,
                    reminder = templateDomainCommand.reminder.toReminderEntity(),
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId
                )

                is TemplateDomainCommand.DeleteTemplateReminder -> DeleteTemplateReminderEntity(
                    templateId = templateDomainCommand.templateId.id,
                    reminderId = templateDomainCommand.reminderId.id,
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId
                )

                is TemplateDomainCommand.DeleteTemplate -> DeleteTemplateEntity(
                    templateId = templateDomainCommand.templateId.id,
                    timestamp = templateDomainCommand.timestamp,
                    commandId = templateDomainCommand.commandId
                )
            }
        }
    }
}
