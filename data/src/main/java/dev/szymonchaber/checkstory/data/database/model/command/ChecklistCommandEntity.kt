package dev.szymonchaber.checkstory.data.database.model.command

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.fill.TaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ChecklistCommandEntity : CommandDataEntity {

    val checklistId: DtoUUID

    @Serializable
    @SerialName("createChecklist")
    data class CreateChecklistEntity(
        override val checklistId: DtoUUID,
        val templateId: DtoUUID,
        val title: String,
        val description: String,
        val tasks: List<TaskEntity>,
        override val commandId: DtoUUID,
        override val timestamp: Instant,
        val notes: String?
    ) : ChecklistCommandEntity

    @Serializable
    @SerialName("editChecklistNotes")
    data class EditChecklistNotesEntity(
        override val checklistId: DtoUUID,
        val newNotes: String,
        override val commandId: DtoUUID,
        override val timestamp: Instant,
    ) : ChecklistCommandEntity

    @Serializable
    @SerialName("changeTaskChecked")
    data class ChangeTaskCheckedEntity(
        override val checklistId: DtoUUID,
        val taskId: DtoUUID,
        val isChecked: Boolean,
        override val commandId: DtoUUID,
        override val timestamp: Instant,
    ) : ChecklistCommandEntity

    @Serializable
    @SerialName("deleteChecklist")
    data class DeleteChecklistEntity(
        override val checklistId: DtoUUID,
        override val commandId: DtoUUID,
        override val timestamp: Instant,
    ) : ChecklistCommandEntity

    fun toDomainCommand(): ChecklistCommand {
        return when (this) {
            is CreateChecklistEntity -> {
                ChecklistCommand.CreateChecklistCommand(
                    ChecklistId(checklistId),
                    TemplateId(templateId),
                    title,
                    description,
                    tasks.map(TaskEntity::toTask),
                    commandId,
                    timestamp,
                    notes
                )
            }

            is EditChecklistNotesEntity -> {
                ChecklistCommand.EditChecklistNotesCommand(
                    ChecklistId(checklistId),
                    newNotes,
                    commandId,
                    timestamp
                )
            }

            is ChangeTaskCheckedEntity -> {
                ChecklistCommand.ChangeTaskCheckedCommand(
                    ChecklistId(checklistId),
                    TaskId(taskId),
                    isChecked,
                    commandId,
                    timestamp
                )
            }

            is DeleteChecklistEntity -> {
                ChecklistCommand.DeleteChecklistCommand(
                    ChecklistId(checklistId),
                    commandId,
                    timestamp
                )
            }
        }
    }

    companion object {

        fun fromDomainCommand(checklistCommand: ChecklistCommand): ChecklistCommandEntity {
            with(checklistCommand) {
                return when (this) {
                    is ChecklistCommand.CreateChecklistCommand -> CreateChecklistEntity(
                        checklistId = checklistId.id,
                        templateId = templateId.id,
                        title = title,
                        description = description,
                        tasks = tasks.map { it.toTaskEntity() },
                        commandId = commandId,
                        timestamp = timestamp,
                        notes = notes
                    )

                    is ChecklistCommand.EditChecklistNotesCommand -> EditChecklistNotesEntity(
                        checklistId = checklistId.id,
                        newNotes = newNotes,
                        commandId = commandId,
                        timestamp = timestamp
                    )

                    is ChecklistCommand.ChangeTaskCheckedCommand -> ChangeTaskCheckedEntity(
                        checklistId = checklistId.id,
                        taskId = taskId.id,
                        isChecked = isChecked,
                        commandId = commandId,
                        timestamp = timestamp
                    )

                    is ChecklistCommand.DeleteChecklistCommand -> DeleteChecklistEntity(
                        checklistId = checklistId.id,
                        commandId = commandId,
                        timestamp = timestamp
                    )
                }
            }
        }
    }
}

private fun Task.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = id.id,
        checklistId = checklistId.id,
        title = title,
        sortPosition = 0,
        isChecked = isChecked,
        children = children.map { it.toTaskEntity() }
    )
}

@Serializable
data class TaskEntity(
    val id: DtoUUID,
    val checklistId: DtoUUID,
    val title: String,
    val sortPosition: Long,
    val isChecked: Boolean,
    val children: List<TaskEntity> = listOf()
) {

    fun toTask(parentId: TaskId? = null): Task {
        val id = TaskId(id)
        return Task(
            id,
            parentId,
            ChecklistId(checklistId),
            title,
            isChecked,
            children.map { it.toTask(id) },
            sortPosition
        )
    }
}
