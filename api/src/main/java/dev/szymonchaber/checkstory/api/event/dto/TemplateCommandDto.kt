package dev.szymonchaber.checkstory.api.event.dto

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
internal sealed interface TemplateCommandDto : CommandDto {

    val templateId: DtoUUID
}

@Serializable
@SerialName("createTemplate")
internal data class CreateTemplateCommandDto(
    override val templateId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("editTemplateTitle")
internal data class EditTemplateTitleCommandDto(
    override val templateId: DtoUUID,
    val newTitle: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("editTemplateDescription")
internal data class EditTemplateDescriptionCommandDto(
    override val templateId: DtoUUID,
    val newDescription: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("addTemplateTask")
internal data class AddTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val parentTaskId: String?,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("renameTemplateTask")
internal data class RenameTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val newTitle: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("deleteTemplateTask")
internal data class DeleteTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("updateTemplateTasksPositions")
internal data class UpdateTasksPositionsCommandDto(
    override val templateId: DtoUUID,
    val taskIdToLocalPosition: Map<DtoUUID, Long>,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("moveTemplateTask")
internal data class MoveTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val newParentTaskId: DtoUUID?,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("addOrUpdateTemplateReminder")
internal data class AddOrUpdateTemplateReminderCommandDto(
    override val templateId: DtoUUID,
    val reminder: ReminderDto,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("deleteTemplateReminder")
internal data class DeleteTemplateReminderCommandDto(
    override val templateId: DtoUUID,
    val reminderId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("deleteTemplate")
internal data class DeleteTemplateCommandDto(
    override val templateId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

internal fun Reminder.toReminderDto(): ReminderDto {
    return when (this) {
        is Reminder.Exact -> {
            ReminderDto.ExactDto(
                this.id.id,
                this.forTemplate.id,
                this.startDateTime.toKotlinLocalDateTime()
            )
        }

        is Reminder.Recurring -> {
            ReminderDto.RecurringDto(
                this.id.id, this.forTemplate.id, this.startDateTime.toKotlinLocalDateTime(),
                when (val actualInterval = interval) {
                    Interval.Daily -> IntervalDto.DailyDto
                    is Interval.Monthly -> IntervalDto.MonthlyDto(actualInterval.dayOfMonth)
                    is Interval.Weekly -> IntervalDto.WeeklyDto(actualInterval.dayOfWeek)
                    is Interval.Yearly -> IntervalDto.YearlyDto(actualInterval.dayOfYear)
                }
            )
        }
    }
}

@Serializable
internal sealed interface ReminderDto {

    val id: DtoUUID
    val forTemplate: DtoUUID
    val startDateTime: LocalDateTime

    @Serializable
    @SerialName("exact")
    data class ExactDto(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime
    ) : ReminderDto

    @Serializable
    @SerialName("recurring")
    data class RecurringDto(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime,
        val interval: IntervalDto
    ) : ReminderDto
}

@Serializable
internal sealed interface IntervalDto {

    @Serializable
    @SerialName("daily")
    object DailyDto : IntervalDto

    @Serializable
    @SerialName("weekly")
    data class WeeklyDto(val dayOfWeek: DayOfWeek) : IntervalDto

    @Serializable
    @SerialName("monthly")
    data class MonthlyDto(val dayOfMonth: Int) : IntervalDto

    @Serializable
    @SerialName("yearly")
    data class YearlyDto(val dayOfYear: Int) : IntervalDto
}
