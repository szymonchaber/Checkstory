package dev.szymonchaber.checkstory.data.database.model.command

import dev.szymonchaber.checkstory.data.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
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

    fun toDomainCommand(): ChecklistDomainCommand {
        return when (this) {
            is CreateChecklistEntity -> {
                ChecklistDomainCommand.CreateChecklistCommand(
                    ChecklistId(checklistId),
                    ChecklistTemplateId(templateId),
                    title,
                    description,
                    tasks.map(TaskEntity::toCheckbox),
                    commandId,
                    timestamp
                )
            }

            is EditChecklistNotesEntity -> {
                ChecklistDomainCommand.EditChecklistNotesCommand(
                    ChecklistId(checklistId),
                    newNotes,
                    commandId,
                    timestamp
                )
            }

            is ChangeTaskCheckedEntity -> {
                ChecklistDomainCommand.ChangeTaskCheckedCommand(
                    ChecklistId(checklistId),
                    CheckboxId(taskId),
                    isChecked,
                    commandId,
                    timestamp
                )
            }

            is DeleteChecklistEntity -> {
                ChecklistDomainCommand.DeleteChecklistCommand(
                    ChecklistId(checklistId),
                    commandId,
                    timestamp
                )
            }
        }
    }

    companion object {

        fun fromDomainCommand(checklistDomainCommand: ChecklistDomainCommand): ChecklistCommandEntity {
            with(checklistDomainCommand) {
                return when (this) {
                    is ChecklistDomainCommand.CreateChecklistCommand -> CreateChecklistEntity(
                        checklistId = checklistId.id,
                        templateId = templateId.id,
                        title = title,
                        description = description,
                        tasks = tasks.map { it.toTaskEntity() },
                        commandId = commandId,
                        timestamp = timestamp
                    )

                    is ChecklistDomainCommand.EditChecklistNotesCommand -> EditChecklistNotesEntity(
                        checklistId = checklistId.id,
                        newNotes = newNotes,
                        commandId = commandId,
                        timestamp = timestamp
                    )

                    is ChecklistDomainCommand.ChangeTaskCheckedCommand -> ChangeTaskCheckedEntity(
                        checklistId = checklistId.id,
                        taskId = taskId.id,
                        isChecked = isChecked,
                        commandId = commandId,
                        timestamp = timestamp
                    )

                    is ChecklistDomainCommand.DeleteChecklistCommand -> DeleteChecklistEntity(
                        checklistId = checklistId.id,
                        commandId = commandId,
                        timestamp = timestamp
                    )
                }
            }
        }
    }
}

private fun Checkbox.toTaskEntity(): TaskEntity {
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

    fun toCheckbox(parentId: CheckboxId? = null): Checkbox {
        val id = CheckboxId(id)
        return Checkbox(
            id,
            parentId,
            ChecklistId(checklistId),
            title,
            isChecked,
            children.map { it.toCheckbox(id) }
        )
    }
}
