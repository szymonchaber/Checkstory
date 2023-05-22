package dev.szymonchaber.checkstory.data.api.event.dto

import dev.szymonchaber.checkstory.data.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
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
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("editTemplateTitle")
data class EditTemplateTitleCommandDto(
    override val templateId: DtoUUID,
    val newTitle: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("editTemplateDescription")
data class EditTemplateDescriptionCommandDto(
    override val templateId: DtoUUID,
    val newDescription: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("addTemplateTask")
data class AddTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val parentTaskId: String?,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("renameTemplateTask")
data class RenameTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val newTitle: String,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("deleteTemplateTask")
data class DeleteTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("updateTemplateTasksPositions")
data class UpdateTasksPositionsCommandDto(
    override val templateId: DtoUUID,
    val taskIdToLocalPosition: Map<DtoUUID, Long>,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("moveTemplateTask")
data class MoveTemplateTaskCommandDto(
    override val templateId: DtoUUID,
    val taskId: DtoUUID,
    val newParentTaskId: DtoUUID?,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("addOrUpdateTemplateReminder")
data class AddOrUpdateTemplateReminderCommandDto(
    override val templateId: DtoUUID,
    val reminder: ReminderDto,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("deleteTemplateReminder")
data class DeleteTemplateReminderCommandDto(
    override val templateId: DtoUUID,
    val reminderId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

@Serializable
@SerialName("deleteTemplate")
data class DeleteTemplateCommandDto(
    override val templateId: DtoUUID,
    override val commandId: DtoUUID,
    override val timestamp: Instant
) : TemplateCommandDto

fun Reminder.toReminderDto(): ReminderDto {
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

fun Reminder.toReminderEntity(): ReminderEntity {
    return when (this) {
        is Reminder.Exact -> {
            ReminderEntity.ExactEntity(
                this.id.id,
                this.forTemplate.id,
                this.startDateTime.toKotlinLocalDateTime()
            )
        }

        is Reminder.Recurring -> {
            ReminderEntity.RecurringEntity(
                this.id.id, this.forTemplate.id, this.startDateTime.toKotlinLocalDateTime(),
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

fun ReminderEntity.toReminder(): Reminder {
    return when (this) {
        is ReminderEntity.ExactEntity -> {
            Reminder.Exact(
                id = ReminderId(id),
                forTemplate = ChecklistTemplateId(this.forTemplate),
                startDateTime = this.startDateTime.toJavaLocalDateTime()
            )
        }

        is ReminderEntity.RecurringEntity -> {
            val interval = when (this.interval) {
                IntervalEntity.DailyEntity -> Interval.Daily
                is IntervalEntity.MonthlyEntity -> Interval.Monthly(dayOfMonth = this.interval.dayOfMonth)
                is IntervalEntity.WeeklyEntity -> Interval.Weekly(dayOfWeek = this.interval.dayOfWeek)
                is IntervalEntity.YearlyEntity -> Interval.Yearly(dayOfYear = this.interval.dayOfYear)
            }
            Reminder.Recurring(
                id = ReminderId(this.id),
                forTemplate = ChecklistTemplateId(this.forTemplate),
                startDateTime = this.startDateTime.toJavaLocalDateTime(),
                interval = interval
            )
        }
    }
}

@Serializable
sealed interface ReminderDto {

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


@Serializable
sealed interface ReminderEntity {

    val id: DtoUUID
    val forTemplate: DtoUUID
    val startDateTime: LocalDateTime

    @Serializable
    @SerialName("exact")
    data class ExactEntity(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime
    ) : ReminderEntity

    @Serializable
    @SerialName("recurring")
    data class RecurringEntity(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime,
        val interval: IntervalEntity
    ) : ReminderEntity
}

@Serializable
sealed interface IntervalEntity {

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
