package dev.szymonchaber.checkstory.data.api.event

import dev.szymonchaber.checkstory.data.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

internal class TemplatesApi @Inject constructor(private val httpClient: HttpClient) {

    suspend fun getTemplates(): List<ChecklistTemplate> {
        return httpClient.get("http://10.0.2.2:8080/templates")
            .body<List<ApiTemplate>>()
            .map(ApiTemplate::toTemplate)
    }
}

@Serializable
data class ApiTemplate(
    val id: DtoUUID,
    val userId: String,
    val name: String,
    val description: String,
    val tasks: List<TemplateTaskDto> = listOf(),
    val reminders: List<ReminderDto> = listOf(),
    val isDeleted: Boolean = false
) {

    fun toTemplate(): ChecklistTemplate {
        return ChecklistTemplate(
            id = ChecklistTemplateId(id),
            title = name,
            description = description,
            items = tasks.map { it.toTask() },
            createdAt = java.time.LocalDateTime.now(), // TODO get from backend
            checklists = listOf(),
            reminders = reminders.map { it.toReminder() },
            isRemoved = isDeleted,
        )
    }
}

fun ReminderDto.toReminder(): Reminder {
    return when (this) {
        is ReminderDto.Exact -> {
            Reminder.Exact(
                id = ReminderId(id),
                forTemplate = ChecklistTemplateId(this.forTemplate),
                startDateTime = this.startDateTime.toJavaLocalDateTime()
            )
        }

        is ReminderDto.Recurring -> {
            val interval = when (this.interval) {
                IntervalDto.Daily -> Interval.Daily
                is IntervalDto.Monthly -> Interval.Monthly(dayOfMonth = this.interval.dayOfMonth)
                is IntervalDto.Weekly -> Interval.Weekly(dayOfWeek = this.interval.dayOfWeek)
                is IntervalDto.Yearly -> Interval.Yearly(dayOfYear = this.interval.dayOfYear)
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
    data class Exact(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime
    ) : ReminderDto

    @Serializable
    @SerialName("recurring")
    data class Recurring(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime,
        val interval: IntervalDto
    ) : ReminderDto
}

@Serializable
data class TemplateTaskDto(
    val id: DtoUUID,
    val templateId: DtoUUID,
    val title: String,
    val sortPosition: Long,
    val children: List<TemplateTaskDto> = listOf()
) {

    fun toTask(parentId: TemplateCheckboxId? = null): TemplateCheckbox {
        val id = TemplateCheckboxId(id)
        return TemplateCheckbox(
            id = id,
            parentId = parentId,
            title = title,
            children = children.map { it.toTask(id) },
            sortPosition = sortPosition
        )
    }
}


@Serializable
sealed interface IntervalDto {

    @Serializable
    @SerialName("daily")
    object Daily : IntervalDto

    @Serializable
    @SerialName("weekly")
    data class Weekly(val dayOfWeek: DayOfWeek) : IntervalDto

    @Serializable
    @SerialName("monthly")
    data class Monthly(val dayOfMonth: Int) : IntervalDto

    @Serializable
    @SerialName("yearly")
    data class Yearly(val dayOfYear: Int) : IntervalDto
}
