package dev.szymonchaber.checkstory.data.database.datasource

import dev.szymonchaber.checkstory.data.api.event.dto.toReminder
import dev.szymonchaber.checkstory.data.api.event.dto.toReminderEntity
import dev.szymonchaber.checkstory.data.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.data.database.dao.CommandDao
import dev.szymonchaber.checkstory.data.database.model.command.CommandEntity
import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Inject

class CommandDataSource @Inject constructor(private val commandDao: CommandDao) {

    fun getAll(): Flow<List<DomainCommand>> {
        return commandDao.getAll()
            .map { commands ->
                withContext(Dispatchers.Default) {
                    commands.map {
                        when (it.type) {
                            "checklistCommand" -> {
                                Json.decodeFromString<ChecklistCommandEntity>(it.jsonData).toDomainCommand()
                            }

                            "templateCommand" -> {
                                Json.decodeFromString<TemplateCommandEntity>(it.jsonData).toDomainCommand()
                            }

                            else -> throw IllegalStateException("Unknown command type in database: ${it.type}")
                        }
                    }
                }
            }
    }

    suspend fun insert(commands: List<DomainCommand>) {
        return withContext(Dispatchers.Default) {
            commandDao.insertAll(commands.map {
                toCommandEntity(it)
            })
        }
    }

    private fun toCommandEntity(command: DomainCommand): CommandEntity {
        val (entity, type) = when (command) {
            is ChecklistDomainCommand -> command.toChecklistCommandEntity() to "checklistCommand"
            is TemplateDomainCommand -> command.toTemplateCommandEntity() to "templateCommand"
        }
        val data = Json.encodeToString(CommandDataEntity.serializer(), entity)
        return CommandEntity(command.commandId, type, data)
    }

    private fun TemplateDomainCommand.toTemplateCommandEntity(): TemplateCommandEntity {
        return when (this) {
            is TemplateDomainCommand.CreateNewTemplate -> TemplateCommandEntity.CreateNewTemplateEntity(
                templateId = templateId.id,
                timestamp = timestamp,
                commandId = commandId
            )

            is TemplateDomainCommand.RenameTemplate -> TemplateCommandEntity.RenameTemplateEntity(
                templateId = templateId.id,
                newTitle = newTitle,
                timestamp = timestamp,
                commandId = commandId
            )

            is TemplateDomainCommand.ChangeTemplateDescription -> TemplateCommandEntity.ChangeTemplateDescriptionEntity(
                templateId = templateId.id,
                newDescription = newDescription,
                timestamp = timestamp,
                commandId = commandId
            )

            is TemplateDomainCommand.AddTemplateTask -> TemplateCommandEntity.AddTemplateTaskEntity(
                templateId = templateId.id,
                taskId = taskId.id,
                parentTaskId = parentTaskId?.id,
                timestamp = timestamp,
                commandId = commandId
            )

            is TemplateDomainCommand.RenameTemplateTask -> TemplateCommandEntity.RenameTemplateTaskEntity(
                templateId = templateId.id,
                taskId = taskId.id,
                newTitle = newTitle,
                timestamp = timestamp,
                commandId = commandId
            )

            is TemplateDomainCommand.DeleteTemplateTask -> TemplateCommandEntity.DeleteTemplateTaskEntity(
                templateId = templateId.id,
                taskId = taskId.id,
                timestamp = timestamp,
                commandId = commandId
            )

            is TemplateDomainCommand.UpdateCheckboxPositions -> TemplateCommandEntity.UpdateCheckboxPositionsEntity(
                localPositions = localPositions.mapKeys { it.key.id },
                timestamp = timestamp,
                commandId = commandId,
                templateId = templateId.id
            )

            is TemplateDomainCommand.MoveTemplateTask -> TemplateCommandEntity.MoveTemplateTaskEntity(
                taskId = taskId.id,
                newParentTaskId = newParentTaskId?.id,
                timestamp = timestamp,
                commandId = commandId,
                templateId = templateId.id
            )

            is TemplateDomainCommand.AddOrReplaceTemplateReminder -> TemplateCommandEntity.AddOrReplaceTemplateReminderEntity(
                templateId = templateId.id,
                reminder = mapToReminderDto(reminder),
                timestamp = timestamp,
                commandId = commandId
            )

            is TemplateDomainCommand.DeleteTemplateReminder -> TemplateCommandEntity.DeleteTemplateReminderEntity(
                templateId = templateId.id,
                reminderId = reminderId.id,
                timestamp = timestamp,
                commandId = commandId
            )

            is TemplateDomainCommand.DeleteTemplate -> TemplateCommandEntity.DeleteTemplateEntity(
                templateId = templateId.id,
                timestamp = timestamp,
                commandId = commandId
            )
        }
    }

    private fun mapToReminderDto(reminder: Reminder): dev.szymonchaber.checkstory.data.api.event.dto.ReminderEntity {
        return reminder.toReminderEntity()
    }

    suspend fun deleteByIds(commandIds: List<UUID>) {
        commandIds.forEach {
            commandDao.deleteById(it)
        }
    }
}

@Serializable
sealed interface CommandDataEntity {

    val timestamp: Long
    val commandId: DtoUUID
}

@Serializable
sealed interface TemplateCommandEntity : CommandDataEntity {

    val templateId: DtoUUID

    @Serializable
    @SerialName("createTemplate")
    data class CreateNewTemplateEntity(
        override val templateId: DtoUUID,
        override val timestamp: Long,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("editTemplateTitle")
    data class RenameTemplateEntity(
        override val templateId: DtoUUID,
        val newTitle: String,
        override val timestamp: Long,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("editTemplateDescription")
    data class ChangeTemplateDescriptionEntity(
        override val templateId: DtoUUID,
        val newDescription: String,
        override val timestamp: Long,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("addTemplateTask")
    data class AddTemplateTaskEntity(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        val parentTaskId: DtoUUID?,
        override val timestamp: Long,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("renameTemplateTask")
    data class RenameTemplateTaskEntity(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        val newTitle: String,
        override val timestamp: Long,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("deleteTemplateTask")
    data class DeleteTemplateTaskEntity(
        override val templateId: DtoUUID,
        val taskId: DtoUUID,
        override val timestamp: Long,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("updateTemplateTasksPositions")
    data class UpdateCheckboxPositionsEntity(
        val localPositions: Map<DtoUUID, Long>,
        override val timestamp: Long,
        override val commandId: DtoUUID,
        override val templateId: DtoUUID
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("moveTemplateTask")
    data class MoveTemplateTaskEntity(
        val taskId: DtoUUID,
        val newParentTaskId: DtoUUID?,
        override val timestamp: Long,
        override val commandId: DtoUUID,
        override val templateId: DtoUUID
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("addOrUpdateTemplateReminder")
    data class AddOrReplaceTemplateReminderEntity(
        override val templateId: DtoUUID,
        val reminder: dev.szymonchaber.checkstory.data.api.event.dto.ReminderEntity,
        override val timestamp: Long,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("deleteTemplateReminder")
    data class DeleteTemplateReminderEntity(
        override val templateId: DtoUUID,
        val reminderId: DtoUUID,
        override val timestamp: Long,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity

    @Serializable
    @SerialName("deleteTemplate")
    data class DeleteTemplateEntity(
        override val templateId: DtoUUID,
        override val timestamp: Long,
        override val commandId: DtoUUID = UUID.randomUUID()
    ) : TemplateCommandEntity
}

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
        override val timestamp: Long,
    ) : ChecklistCommandEntity

    @Serializable
    @SerialName("editChecklistNotes")
    data class EditChecklistNotesEntity(
        override val checklistId: DtoUUID,
        val newNotes: String,
        override val commandId: DtoUUID,
        override val timestamp: Long,
    ) : ChecklistCommandEntity

    @Serializable
    @SerialName("changeTaskChecked")
    data class ChangeTaskCheckedEntity(
        override val checklistId: DtoUUID,
        val taskId: DtoUUID,
        val isChecked: Boolean,
        override val commandId: DtoUUID,
        override val timestamp: Long,
    ) : ChecklistCommandEntity

    @Serializable
    @SerialName("deleteChecklist")
    data class DeleteChecklistEntity(
        override val checklistId: DtoUUID,
        override val commandId: DtoUUID,
        override val timestamp: Long,
    ) : ChecklistCommandEntity
}

fun ChecklistDomainCommand.toChecklistCommandEntity(): ChecklistCommandEntity {
    return when (this) {
        is ChecklistDomainCommand.CreateChecklistCommand -> ChecklistCommandEntity.CreateChecklistEntity(
            checklistId = checklistId.id,
            templateId = templateId.id,
            title = title,
            description = description,
            tasks = tasks.map { it.toTaskEntity() },
            commandId = commandId,
            timestamp = timestamp
        )

        is ChecklistDomainCommand.EditChecklistNotesCommand -> ChecklistCommandEntity.EditChecklistNotesEntity(
            checklistId = checklistId.id,
            newNotes = newNotes,
            commandId = commandId,
            timestamp = timestamp
        )

        is ChecklistDomainCommand.ChangeTaskCheckedCommand -> ChecklistCommandEntity.ChangeTaskCheckedEntity(
            checklistId = checklistId.id,
            taskId = taskId.id,
            isChecked = isChecked,
            commandId = commandId,
            timestamp = timestamp
        )

        is ChecklistDomainCommand.DeleteChecklistCommand -> ChecklistCommandEntity.DeleteChecklistEntity(
            checklistId = checklistId.id,
            commandId = commandId,
            timestamp = timestamp
        )
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

fun TemplateCommandEntity.toDomainCommand(): TemplateDomainCommand {
    return when (this) {
        is TemplateCommandEntity.CreateNewTemplateEntity -> TemplateDomainCommand.CreateNewTemplate(
            templateId = ChecklistTemplateId(templateId),
            timestamp = timestamp,
            commandId = commandId
        )

        is TemplateCommandEntity.RenameTemplateEntity -> TemplateDomainCommand.RenameTemplate(
            templateId = ChecklistTemplateId(templateId),
            newTitle = newTitle,
            timestamp = timestamp,
            commandId = commandId
        )

        is TemplateCommandEntity.ChangeTemplateDescriptionEntity -> TemplateDomainCommand.ChangeTemplateDescription(
            templateId = ChecklistTemplateId(templateId),
            newDescription = newDescription,
            timestamp = timestamp,
            commandId = commandId
        )

        is TemplateCommandEntity.AddTemplateTaskEntity -> TemplateDomainCommand.AddTemplateTask(
            templateId = ChecklistTemplateId(templateId),
            taskId = TemplateCheckboxId(taskId),
            parentTaskId = parentTaskId?.let { TemplateCheckboxId(it) },
            timestamp = timestamp,
            commandId = commandId
        )

        is TemplateCommandEntity.RenameTemplateTaskEntity -> TemplateDomainCommand.RenameTemplateTask(
            templateId = ChecklistTemplateId(templateId),
            taskId = TemplateCheckboxId(taskId),
            newTitle = newTitle,
            timestamp = timestamp,
            commandId = commandId
        )

        is TemplateCommandEntity.DeleteTemplateTaskEntity -> TemplateDomainCommand.DeleteTemplateTask(
            templateId = ChecklistTemplateId(templateId),
            taskId = TemplateCheckboxId(taskId),
            timestamp = timestamp,
            commandId = commandId
        )

        is TemplateCommandEntity.UpdateCheckboxPositionsEntity -> TemplateDomainCommand.UpdateCheckboxPositions(
            localPositions = localPositions.mapKeys { TemplateCheckboxId(it.key) },
            timestamp = timestamp,
            commandId = commandId,
            templateId = ChecklistTemplateId(templateId)
        )

        is TemplateCommandEntity.MoveTemplateTaskEntity -> TemplateDomainCommand.MoveTemplateTask(
            taskId = TemplateCheckboxId(taskId),
            newParentTaskId = newParentTaskId?.let { TemplateCheckboxId(it) },
            timestamp = timestamp,
            commandId = commandId,
            templateId = ChecklistTemplateId(templateId)
        )

        is TemplateCommandEntity.AddOrReplaceTemplateReminderEntity -> TemplateDomainCommand.AddOrReplaceTemplateReminder(
            templateId = ChecklistTemplateId(templateId),
            reminder = reminder.toReminder(),
            timestamp = timestamp,
            commandId = commandId
        )

        is TemplateCommandEntity.DeleteTemplateReminderEntity -> TemplateDomainCommand.DeleteTemplateReminder(
            templateId = ChecklistTemplateId(templateId),
            reminderId = ReminderId(reminderId),
            timestamp = timestamp,
            commandId = commandId
        )

        is TemplateCommandEntity.DeleteTemplateEntity -> TemplateDomainCommand.DeleteTemplate(
            templateId = ChecklistTemplateId(templateId),
            timestamp = timestamp,
            commandId = commandId
        )
    }
}

fun ChecklistCommandEntity.toDomainCommand(): ChecklistDomainCommand {
    return when (this) {
        is ChecklistCommandEntity.CreateChecklistEntity -> {
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

        is ChecklistCommandEntity.EditChecklistNotesEntity -> {
            ChecklistDomainCommand.EditChecklistNotesCommand(
                ChecklistId(checklistId),
                newNotes,
                commandId,
                timestamp
            )
        }

        is ChecklistCommandEntity.ChangeTaskCheckedEntity -> {
            ChecklistDomainCommand.ChangeTaskCheckedCommand(
                ChecklistId(checklistId),
                CheckboxId(taskId),
                isChecked,
                commandId,
                timestamp
            )
        }

        is ChecklistCommandEntity.DeleteChecklistEntity -> {
            ChecklistDomainCommand.DeleteChecklistCommand(
                ChecklistId(checklistId),
                commandId,
                timestamp
            )
        }
    }
}
