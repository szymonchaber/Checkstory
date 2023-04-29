package dev.szymonchaber.checkstory.data.api.event.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ChecklistCommandDto : CommandDto {

    val checklistId: String
}

@Serializable
@SerialName("createChecklist")
data class CreateChecklistCommand(
    override val checklistId: String,
    val templateId: String,
    val title: String,
    val description: String,
    val tasks: List<TaskDto>,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : ChecklistCommandDto

@Serializable
@SerialName("editChecklistNotes")
data class EditChecklistNotesCommand(
    override val checklistId: String,
    val newNotes: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : ChecklistCommandDto

@Serializable
@SerialName("changeTaskChecked")
data class ChangeTaskCheckedCommand(
    override val checklistId: String,
    val taskId: String,
    val isChecked: Boolean,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : ChecklistCommandDto

@Serializable
@SerialName("deleteChecklist")
data class DeleteChecklistCommand(
    override val checklistId: String,
    override val eventId: String,
    override val timestamp: Long,
    override val userId: String
) : ChecklistCommandDto

@Serializable
data class TaskDto(
    val id: String,
    val checklistId: String,
    val title: String,
    val sortPosition: Long,
    val isChecked: Boolean,
    val children: List<TaskDto> = listOf()
)
