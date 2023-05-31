package dev.szymonchaber.checkstory.api.command.dto

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ChecklistApiCommand : ApiCommand {

    val checklistId: DtoUUID
}

@Serializable
@SerialName("createChecklist")
internal data class CreateChecklistApiCommand(
    override val checklistId: DtoUUID,
    val templateId: DtoUUID,
    val title: String,
    val description: String,
    val tasks: List<TaskDto>,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : ChecklistApiCommand

@Serializable
@SerialName("editChecklistNotes")
internal data class EditChecklistNotesApiCommand(
    override val checklistId: DtoUUID,
    val newNotes: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : ChecklistApiCommand

@Serializable
@SerialName("changeTaskChecked")
internal data class ChangeTaskCheckedApiCommand(
    override val checklistId: DtoUUID,
    val taskId: DtoUUID,
    val isChecked: Boolean,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : ChecklistApiCommand

@Serializable
@SerialName("deleteChecklist")
internal data class DeleteChecklistApiCommand(
    override val checklistId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : ChecklistApiCommand

@Serializable
internal data class TaskDto(
    val id: DtoUUID,
    val checklistId: DtoUUID,
    val title: String,
    val sortPosition: Long,
    val isChecked: Boolean,
    val children: List<TaskDto> = listOf()
)
