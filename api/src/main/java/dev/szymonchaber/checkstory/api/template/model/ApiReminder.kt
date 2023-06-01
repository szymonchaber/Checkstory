package dev.szymonchaber.checkstory.api.template.model

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface ApiReminder {

    val id: DtoUUID
    val forTemplate: DtoUUID
    val startDateTime: LocalDateTime

    @Serializable
    @SerialName("exact")
    data class Exact(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime
    ) : ApiReminder

    @Serializable
    @SerialName("recurring")
    data class Recurring(
        override val id: DtoUUID,
        override val forTemplate: DtoUUID,
        override val startDateTime: LocalDateTime,
        val interval: ApiInterval
    ) : ApiReminder

    fun toReminder(): Reminder {
        return when (this) {
            is Exact -> {
                Reminder.Exact(
                    id = ReminderId(id),
                    forTemplate = TemplateId(this.forTemplate),
                    startDateTime = this.startDateTime.toJavaLocalDateTime()
                )
            }

            is Recurring -> {
                val interval = when (this.interval) {
                    ApiInterval.Daily -> Interval.Daily
                    is ApiInterval.Monthly -> Interval.Monthly(dayOfMonth = this.interval.dayOfMonth)
                    is ApiInterval.Weekly -> Interval.Weekly(dayOfWeek = this.interval.dayOfWeek)
                    is ApiInterval.Yearly -> Interval.Yearly(dayOfYear = this.interval.dayOfYear)
                }
                Reminder.Recurring(
                    id = ReminderId(this.id),
                    forTemplate = TemplateId(this.forTemplate),
                    startDateTime = this.startDateTime.toJavaLocalDateTime(),
                    interval = interval
                )
            }
        }
    }
}
