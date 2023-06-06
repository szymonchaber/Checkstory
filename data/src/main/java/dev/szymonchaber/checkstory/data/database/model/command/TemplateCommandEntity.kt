package dev.szymonchaber.checkstory.data.database.model.command

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
internal sealed interface TemplateCommandEntity : CommandDataEntity {

    val templateId: DtoUUID

    @Serializable
    @SerialName("createTemplate")
    data class CreateNewTemplateEntity(
        override val templateId: DtoUUID,
        override val timestamp: Instant,
        override val commandId: DtoUUID = UUID.randomUUID(),
        val existingData: ExistingTemplateEntity?
    ) : TemplateCommandEntity

    @Serializable
    data class ExistingTemplateEntity(
        val id: DtoUUID,
        val title: String,
        val description: String,
        val items: List<CommandTemplateTaskEntity>,
        val createdAt: Instant,
        val reminders: List<CommandReminderEntity>
    ) {

        fun toTemplate(): Template {
            return Template(
                TemplateId(id),
                title,
                description,
                items.map(CommandTemplateTaskEntity::toTask),
                createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime(),
                listOf(),
                reminders.map {
                    it.toReminder()
                }
            )
        }

        companion object {

            fun from(template: Template): ExistingTemplateEntity {
                return with(template) {
                    ExistingTemplateEntity(
                        id.id,
                        title,
                        description,
                        tasks.map {
                            it.toCommandTemplateTask()
                        },
                        createdAt.toKotlinLocalDateTime().toInstant(TimeZone.currentSystemDefault()),
                        reminders.map(Reminder::toCommandReminderEntity)
                    )
                }
            }

            private fun TemplateTask.toCommandTemplateTask(): CommandTemplateTaskEntity {
                return CommandTemplateTaskEntity(
                    id = id.id,
                    templateId = templateId.id,
                    parentTaskId = parentId?.id,
                    title = title,
                    children = children.map { it.toCommandTemplateTask() },
                    sortPosition = sortPosition
                )
            }
        }
    }

    @Serializable
    data class CommandTemplateTaskEntity(
        val id: DtoUUID,
        val templateId: DtoUUID,
        val parentTaskId: DtoUUID?,
        val title: String,
        val children: List<CommandTemplateTaskEntity>,
        val sortPosition: Long,
    ) {

        fun toTask(): TemplateTask {
            return TemplateTask(
                TemplateTaskId(id),
                parentTaskId?.let { TemplateTaskId(it) },
                title,
                children.map { it.toTask() },
                sortPosition,
                TemplateId(templateId),
            )
        }
    }

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
        val reminder: CommandReminderEntity,
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

    fun toDomainCommand(): TemplateCommand {
        return when (this) {
            is CreateNewTemplateEntity -> {
                TemplateCommand.CreateNewTemplate(
                    templateId = TemplateId(templateId),
                    timestamp = timestamp,
                    commandId = commandId,
                    existingData = existingData?.toTemplate()
                )
            }

            is RenameTemplateEntity -> TemplateCommand.RenameTemplate(
                templateId = TemplateId(templateId),
                newTitle = newTitle,
                timestamp = timestamp,
                commandId = commandId
            )

            is ChangeTemplateDescriptionEntity -> TemplateCommand.ChangeTemplateDescription(
                templateId = TemplateId(templateId),
                newDescription = newDescription,
                timestamp = timestamp,
                commandId = commandId
            )

            is AddTemplateTaskEntity -> TemplateCommand.AddTemplateTask(
                templateId = TemplateId(templateId),
                taskId = TemplateTaskId(taskId),
                parentTaskId = parentTaskId?.let { TemplateTaskId(it) },
                timestamp = timestamp,
                commandId = commandId
            )

            is RenameTemplateTaskEntity -> TemplateCommand.RenameTemplateTask(
                templateId = TemplateId(templateId),
                taskId = TemplateTaskId(taskId),
                newTitle = newTitle,
                timestamp = timestamp,
                commandId = commandId
            )

            is DeleteTemplateTaskEntity -> TemplateCommand.DeleteTemplateTask(
                templateId = TemplateId(templateId),
                taskId = TemplateTaskId(taskId),
                timestamp = timestamp,
                commandId = commandId
            )

            is UpdateCheckboxPositionsEntity -> TemplateCommand.UpdateTaskPositions(
                localPositions = localPositions.mapKeys { TemplateTaskId(it.key) },
                timestamp = timestamp,
                commandId = commandId,
                templateId = TemplateId(templateId)
            )

            is MoveTemplateTaskEntity -> TemplateCommand.MoveTemplateTask(
                taskId = TemplateTaskId(taskId),
                newParentTaskId = newParentTaskId?.let { TemplateTaskId(it) },
                timestamp = timestamp,
                commandId = commandId,
                templateId = TemplateId(templateId)
            )

            is AddOrReplaceTemplateReminderEntity -> TemplateCommand.AddOrReplaceTemplateReminder(
                templateId = TemplateId(templateId),
                reminder = reminder.toReminder(),
                timestamp = timestamp,
                commandId = commandId
            )

            is DeleteTemplateReminderEntity -> TemplateCommand.DeleteTemplateReminder(
                templateId = TemplateId(templateId),
                reminderId = ReminderId(reminderId),
                timestamp = timestamp,
                commandId = commandId
            )

            is DeleteTemplateEntity -> TemplateCommand.DeleteTemplate(
                templateId = TemplateId(templateId),
                timestamp = timestamp,
                commandId = commandId
            )
        }
    }

    companion object {

        fun fromDomainCommand(templateCommand: TemplateCommand): TemplateCommandEntity {
            return when (templateCommand) {
                is TemplateCommand.CreateNewTemplate -> {
                    CreateNewTemplateEntity(
                        templateId = templateCommand.templateId.id,
                        timestamp = templateCommand.timestamp,
                        commandId = templateCommand.commandId,
                        existingData = templateCommand.existingData?.let { ExistingTemplateEntity.from(it) }
                    )
                }

                is TemplateCommand.RenameTemplate -> RenameTemplateEntity(
                    templateId = templateCommand.templateId.id,
                    newTitle = templateCommand.newTitle,
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId
                )

                is TemplateCommand.ChangeTemplateDescription -> ChangeTemplateDescriptionEntity(
                    templateId = templateCommand.templateId.id,
                    newDescription = templateCommand.newDescription,
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId
                )

                is TemplateCommand.AddTemplateTask -> AddTemplateTaskEntity(
                    templateId = templateCommand.templateId.id,
                    taskId = templateCommand.taskId.id,
                    parentTaskId = templateCommand.parentTaskId?.id,
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId
                )

                is TemplateCommand.RenameTemplateTask -> RenameTemplateTaskEntity(
                    templateId = templateCommand.templateId.id,
                    taskId = templateCommand.taskId.id,
                    newTitle = templateCommand.newTitle,
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId
                )

                is TemplateCommand.DeleteTemplateTask -> DeleteTemplateTaskEntity(
                    templateId = templateCommand.templateId.id,
                    taskId = templateCommand.taskId.id,
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId
                )

                is TemplateCommand.UpdateTaskPositions -> UpdateCheckboxPositionsEntity(
                    localPositions = templateCommand.localPositions.mapKeys { it.key.id },
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId,
                    templateId = templateCommand.templateId.id
                )

                is TemplateCommand.MoveTemplateTask -> MoveTemplateTaskEntity(
                    taskId = templateCommand.taskId.id,
                    newParentTaskId = templateCommand.newParentTaskId?.id,
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId,
                    templateId = templateCommand.templateId.id
                )

                is TemplateCommand.AddOrReplaceTemplateReminder -> AddOrReplaceTemplateReminderEntity(
                    templateId = templateCommand.templateId.id,
                    reminder = templateCommand.reminder.toCommandReminderEntity(),
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId
                )

                is TemplateCommand.DeleteTemplateReminder -> DeleteTemplateReminderEntity(
                    templateId = templateCommand.templateId.id,
                    reminderId = templateCommand.reminderId.id,
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId
                )

                is TemplateCommand.DeleteTemplate -> DeleteTemplateEntity(
                    templateId = templateCommand.templateId.id,
                    timestamp = templateCommand.timestamp,
                    commandId = templateCommand.commandId
                )
            }
        }
    }
}

@Serializable
internal sealed interface CommandReminderEntity {

    val id: DtoUUID
    val forTemplate: DtoUUID
    val startDateTime: LocalDateTime

    @Serializable
    @SerialName("exact")
    data class ExactEntity(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime
    ) : CommandReminderEntity

    @Serializable
    @SerialName("recurring")
    data class RecurringEntity(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime,
        val interval: IntervalEntity
    ) : CommandReminderEntity
}

@Serializable
internal sealed interface IntervalEntity {

    @Serializable
    @SerialName("daily")
    object DailyEntity : IntervalEntity

    @Serializable
    @SerialName("weekly")
    data class WeeklyEntity(val dayOfWeek: DayOfWeek) : IntervalEntity

    @Serializable
    @SerialName("monthly")
    data class MonthlyEntity(val dayOfMonth: Int) : IntervalEntity

    @Serializable
    @SerialName("yearly")
    data class YearlyEntity(val dayOfYear: Int) : IntervalEntity
}

internal fun Reminder.toCommandReminderEntity(): CommandReminderEntity {
    return when (this) {
        is Reminder.Exact -> {
            CommandReminderEntity.ExactEntity(
                id.id,
                forTemplate.id,
                startDateTime.toKotlinLocalDateTime()
            )
        }

        is Reminder.Recurring -> {
            CommandReminderEntity.RecurringEntity(
                id.id,
                forTemplate.id,
                startDateTime.toKotlinLocalDateTime(),
                when (val actualInterval = interval) {
                    Interval.Daily -> IntervalEntity.DailyEntity
                    is Interval.Monthly -> IntervalEntity.MonthlyEntity(actualInterval.dayOfMonth)
                    is Interval.Weekly -> IntervalEntity.WeeklyEntity(actualInterval.dayOfWeek)
                    is Interval.Yearly -> IntervalEntity.YearlyEntity(actualInterval.dayOfYear)
                }
            )
        }
    }
}

internal fun CommandReminderEntity.toReminder(): Reminder {
    return when (this) {
        is CommandReminderEntity.ExactEntity -> {
            Reminder.Exact(
                id = ReminderId(id),
                forTemplate = TemplateId(forTemplate),
                startDateTime = startDateTime.toJavaLocalDateTime()
            )
        }

        is CommandReminderEntity.RecurringEntity -> {
            val interval = when (this.interval) {
                IntervalEntity.DailyEntity -> Interval.Daily
                is IntervalEntity.MonthlyEntity -> Interval.Monthly(dayOfMonth = this.interval.dayOfMonth)
                is IntervalEntity.WeeklyEntity -> Interval.Weekly(dayOfWeek = this.interval.dayOfWeek)
                is IntervalEntity.YearlyEntity -> Interval.Yearly(dayOfYear = this.interval.dayOfYear)
            }
            Reminder.Recurring(
                id = ReminderId(id),
                forTemplate = TemplateId(forTemplate),
                startDateTime = startDateTime.toJavaLocalDateTime(),
                interval = interval
            )
        }
    }
}
