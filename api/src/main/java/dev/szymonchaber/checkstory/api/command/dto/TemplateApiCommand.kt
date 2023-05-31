package dev.szymonchaber.checkstory.api.command.dto

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
internal sealed interface TemplateApiCommand : ApiCommand {

    val templateId: DtoUUID
}

@Serializable
@SerialName("createTemplate")
internal data class CreateTemplateApiCommand(
    override val templateId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("editTemplateTitle")
internal data class EditTemplateTitleApiCommand(
    override val templateId: DtoUUID,
    val newTitle: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("editTemplateDescription")
internal data class EditTemplateDescriptionApiCommand(
    override val templateId: DtoUUID,
    val newDescription: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("addTemplateTask")
internal data class AddTemplateTaskApiCommand(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val parentTaskId: String?,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("renameTemplateTask")
internal data class RenameTemplateTaskApiCommand(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val newTitle: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("deleteTemplateTask")
internal data class DeleteTemplateTaskApiCommand(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("updateTemplateTasksPositions")
internal data class UpdateTasksPositionsApiCommand(
    override val templateId: DtoUUID,
    val taskIdToLocalPosition: Map<DtoUUID, Long>,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("moveTemplateTask")
internal data class MoveTemplateTaskApiCommand(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val newParentTaskId: DtoUUID?,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("addOrUpdateTemplateReminder")
internal data class AddOrUpdateTemplateReminderApiCommand(
    override val templateId: DtoUUID,
    val reminder: ApiTemplateCommandReminder,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("deleteTemplateReminder")
internal data class DeleteTemplateReminderApiCommand(
    override val templateId: DtoUUID,
    val reminderId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

@Serializable
@SerialName("deleteTemplate")
internal data class DeleteTemplateApiCommand(
    override val templateId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateApiCommand

internal fun Reminder.toReminderDto(): ApiTemplateCommandReminder {
    return when (this) {
        is Reminder.Exact -> {
            ApiTemplateCommandReminder.Exact(
                this.id.id,
                this.forTemplate.id,
                this.startDateTime.toKotlinLocalDateTime()
            )
        }

        is Reminder.Recurring -> {
            ApiTemplateCommandReminder.Recurring(
                this.id.id, this.forTemplate.id, this.startDateTime.toKotlinLocalDateTime(),
                when (val actualInterval = interval) {
                    Interval.Daily -> ApiTemplateCommandInterval.Daily
                    is Interval.Monthly -> ApiTemplateCommandInterval.Monthly(actualInterval.dayOfMonth)
                    is Interval.Weekly -> ApiTemplateCommandInterval.Weekly(actualInterval.dayOfWeek)
                    is Interval.Yearly -> ApiTemplateCommandInterval.Yearly(actualInterval.dayOfYear)
                }
            )
        }
    }
}

@Serializable
internal sealed interface ApiTemplateCommandReminder {

    val id: DtoUUID
    val forTemplate: DtoUUID
    val startDateTime: LocalDateTime

    @Serializable
    @SerialName("exact")
    data class Exact(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime
    ) : ApiTemplateCommandReminder

    @Serializable
    @SerialName("recurring")
    data class Recurring(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime,
        val interval: ApiTemplateCommandInterval
    ) : ApiTemplateCommandReminder
}

@Serializable
internal sealed interface ApiTemplateCommandInterval {

    @Serializable
    @SerialName("daily")
    object Daily : ApiTemplateCommandInterval

    @Serializable
    @SerialName("weekly")
    data class Weekly(val dayOfWeek: DayOfWeek) : ApiTemplateCommandInterval

    @Serializable
    @SerialName("monthly")
    data class Monthly(val dayOfMonth: Int) : ApiTemplateCommandInterval

    @Serializable
    @SerialName("yearly")
    data class Yearly(val dayOfYear: Int) : ApiTemplateCommandInterval
}
