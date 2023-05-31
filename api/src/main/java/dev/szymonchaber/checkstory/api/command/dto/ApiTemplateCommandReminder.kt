package dev.szymonchaber.checkstory.api.command.dto

import dev.szymonchaber.checkstory.api.serializers.DtoUUID
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

    companion object {

        internal fun fromReminder(reminder: Reminder): ApiTemplateCommandReminder {
            return when (reminder) {
                is Reminder.Exact -> {
                    Exact(
                        reminder.id.id,
                        reminder.forTemplate.id,
                        reminder.startDateTime.toKotlinLocalDateTime()
                    )
                }

                is Reminder.Recurring -> {
                    Recurring(
                        reminder.id.id, reminder.forTemplate.id, reminder.startDateTime.toKotlinLocalDateTime(),
                        when (val actualInterval = reminder.interval) {
                            Interval.Daily -> ApiTemplateCommandInterval.Daily
                            is Interval.Monthly -> ApiTemplateCommandInterval.Monthly(actualInterval.dayOfMonth)
                            is Interval.Weekly -> ApiTemplateCommandInterval.Weekly(actualInterval.dayOfWeek)
                            is Interval.Yearly -> ApiTemplateCommandInterval.Yearly(actualInterval.dayOfYear)
                        }
                    )
                }
            }
        }
    }
}
