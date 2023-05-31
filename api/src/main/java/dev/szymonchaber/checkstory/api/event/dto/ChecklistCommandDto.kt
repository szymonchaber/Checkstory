package dev.szymonchaber.checkstory.api.event.dto

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ChecklistCommandDto : CommandDto {

    val checklistId: DtoUUID
}

@Serializable
@SerialName("createChecklist")
internal data class CreateChecklistCommand(
    override val checklistId: DtoUUID,
    val templateId: DtoUUID,
    val title: String,
    val description: String,
    val tasks: List<TaskDto>,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : ChecklistCommandDto

@Serializable
@SerialName("editChecklistNotes")
internal data class EditChecklistNotesCommand(
    override val checklistId: DtoUUID,
    val newNotes: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : ChecklistCommandDto

@Serializable
@SerialName("changeTaskChecked")
internal data class ChangeTaskCheckedCommand(
    override val checklistId: DtoUUID,
    val taskId: DtoUUID,
    val isChecked: Boolean,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : ChecklistCommandDto

@Serializable
@SerialName("deleteChecklist")
internal data class DeleteChecklistCommand(
    override val checklistId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : ChecklistCommandDto

@Serializable
internal data class TaskDto(
    val id: DtoUUID,
    val checklistId: DtoUUID,
    val title: String,
    val sortPosition: Long,
    val isChecked: Boolean,
    val children: List<TaskDto> = listOf()
)
