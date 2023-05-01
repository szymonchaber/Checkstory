package dev.szymonchaber.checkstory.data.api.event.dto

import dev.szymonchaber.checkstory.data.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface TemplateCommandDto : CommandDto {

    val templateId: DtoUUID
}

@Serializable
@SerialName("createTemplate")
data class CreateTemplateCommandDto(
    override val templateId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("editTemplateTitle")
data class EditTemplateTitleCommandDto(
    override val templateId: DtoUUID,
    val newTitle: String,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("editTemplateDescription")
data class EditTemplateDescriptionCommandDto(
    override val templateId: DtoUUID,
    val newDescription: String,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("addTemplateTask")
data class AddTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val parentTaskId: String?,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("renameTemplateTask")
data class RenameTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val newTitle: String,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("deleteTemplateTask")
data class DeleteTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("updateTemplateTasksPositions")
data class UpdateTasksPositionsCommandDto(
    override val templateId: DtoUUID,
    val taskIdToLocalPosition: Map<DtoUUID, Long>,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("moveTemplateTask")
data class MoveTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val newParentTaskId: DtoUUID?,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("addOrUpdateTemplateReminder")
data class AddOrUpdateTemplateReminderCommandDto(
    override val templateId: DtoUUID,
    val reminder: ReminderDto,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("deleteTemplateReminder")
data class DeleteTemplateReminderCommandDto(
    override val templateId: DtoUUID,
    val reminderId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

@Serializable
@SerialName("deleteTemplate")
data class DeleteTemplateCommandDto(
    override val templateId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Long,
    override val userId: String
) : TemplateCommandDto

fun Reminder.toReminderDto(): ReminderDto {
    return when (this) {
        is Reminder.Exact -> {
            ReminderDto.ExactDto(
                this.id.id.toString(),
                this.forTemplate.id.toString(),
                this.startDateTime.toKotlinLocalDateTime()
            )
        }

        is Reminder.Recurring -> {
            ReminderDto.RecurringDto(
                this.id.id.toString(), this.forTemplate.id.toString(), this.startDateTime.toKotlinLocalDateTime(),
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
sealed interface ReminderDto {

    val id: String
    val forTemplate: String
    val startDateTime: LocalDateTime

    @Serializable
    @SerialName("exact")
    data class ExactDto(
        override val id: String,
        override val forTemplate: String,
        override val startDateTime: LocalDateTime
    ) : ReminderDto

    @Serializable
    @SerialName("recurring")
    data class RecurringDto(
        override val id: String,
        override val forTemplate: String,
        override val startDateTime: LocalDateTime,
        val interval: IntervalDto
    ) : ReminderDto
}

@Serializable
sealed interface IntervalDto {

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
