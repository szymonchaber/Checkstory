package dev.szymonchaber.checkstory.api.command.dto

import dev.szymonchaber.checkstory.api.command.mapper.toTaskDto
import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
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

        internal fun fromCommand(checklistCommand: ChecklistCommand): ChecklistApiCommand {
            return when (checklistCommand) {
                is ChecklistCommand.CreateChecklistCommand -> CreateChecklistApiCommand(
                    checklistCommand.checklistId.id,
                    checklistCommand.templateId.id,
                    checklistCommand.title,
                    checklistCommand.description,
                    checklistCommand.tasks.map {
                        it.toTaskDto()
                    },
                    checklistCommand.commandId,
                    checklistCommand.timestamp,
                )

                is ChecklistCommand.ChangeTaskCheckedCommand -> ChangeTaskCheckedApiCommand(
                    checklistId = checklistCommand.checklistId.id,
                    taskId = checklistCommand.taskId.id,
                    isChecked = checklistCommand.isChecked,
                    commandId = checklistCommand.commandId,
                    timestamp = checklistCommand.timestamp,
                )

                is ChecklistCommand.DeleteChecklistCommand -> DeleteChecklistApiCommand(
                    checklistId = checklistCommand.checklistId.id,
                    commandId = checklistCommand.commandId,
                    timestamp = checklistCommand.timestamp,
                )

                is ChecklistCommand.EditChecklistNotesCommand -> EditChecklistNotesApiCommand(
                    checklistId = checklistCommand.checklistId.id,
                    newNotes = checklistCommand.newNotes,
                    commandId = checklistCommand.commandId,
                    timestamp = checklistCommand.timestamp,
                )
            }
        }
    }
}
