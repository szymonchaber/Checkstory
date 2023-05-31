package dev.szymonchaber.checkstory.api.command.dto

import dev.szymonchaber.checkstory.api.command.mapper.toTaskDto
import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ChecklistApiCommand : ApiCommand {

    val checklistId: DtoUUID

    @Serializable
    @SerialName("createChecklist")
    data class CreateChecklistApiCommand(
        override val checklistId: DtoUUID,
        val templateId: DtoUUID,
        val title: String,
        val description: String,
        val tasks: List<ApiChecklistCommandTask>,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : ChecklistApiCommand

    @Serializable
    @SerialName("editChecklistNotes")
    data class EditChecklistNotesApiCommand(
        override val checklistId: DtoUUID,
        val newNotes: String,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : ChecklistApiCommand

    @Serializable
    @SerialName("changeTaskChecked")
    data class ChangeTaskCheckedApiCommand(
        override val checklistId: DtoUUID,
        val taskId: DtoUUID,
        val isChecked: Boolean,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : ChecklistApiCommand

    @Serializable
    @SerialName("deleteChecklist")
    data class DeleteChecklistApiCommand(
        override val checklistId: DtoUUID,
        override val commandId: DtoUUID,
        override val timestamp: Instant
    ) : ChecklistApiCommand

    companion object {

        internal fun fromCommand(checklistDomainCommand: ChecklistDomainCommand): ChecklistApiCommand {
            return when (checklistDomainCommand) {
                is ChecklistDomainCommand.CreateChecklistCommand -> CreateChecklistApiCommand(
                    checklistDomainCommand.checklistId.id,
                    checklistDomainCommand.templateId.id,
                    checklistDomainCommand.title,
                    checklistDomainCommand.description,
                    checklistDomainCommand.tasks.map {
                        it.toTaskDto()
                    },
                    checklistDomainCommand.commandId,
                    checklistDomainCommand.timestamp,
                )

                is ChecklistDomainCommand.ChangeTaskCheckedCommand -> ChangeTaskCheckedApiCommand(
                    checklistId = checklistDomainCommand.checklistId.id,
                    taskId = checklistDomainCommand.taskId.id,
                    isChecked = checklistDomainCommand.isChecked,
                    commandId = checklistDomainCommand.commandId,
                    timestamp = checklistDomainCommand.timestamp,
                )

                is ChecklistDomainCommand.DeleteChecklistCommand -> DeleteChecklistApiCommand(
                    checklistId = checklistDomainCommand.checklistId.id,
                    commandId = checklistDomainCommand.commandId,
                    timestamp = checklistDomainCommand.timestamp,
                )

                is ChecklistDomainCommand.EditChecklistNotesCommand -> EditChecklistNotesApiCommand(
                    checklistId = checklistDomainCommand.checklistId.id,
                    newNotes = checklistDomainCommand.newNotes,
                    commandId = checklistDomainCommand.commandId,
                    timestamp = checklistDomainCommand.timestamp,
                )
            }
        }
    }
}
