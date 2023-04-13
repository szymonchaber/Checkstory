package dev.szymonchaber.checkstory.domain.model.checklist.template.reminder

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import java.io.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

sealed interface Reminder {

    val id: ReminderId
    val forTemplate: ChecklistTemplateId
    val startDateTime: LocalDateTime

    val isStored: Boolean
        get() = false //id.id != 0L TODO how else to check it?

    data class Exact(
        override val id: ReminderId,
        override val forTemplate: ChecklistTemplateId,
        override val startDateTime: LocalDateTime
    ) : Reminder

    data class Recurring(
        override val id: ReminderId,
        override val forTemplate: ChecklistTemplateId,
        override val startDateTime: LocalDateTime,
        val interval: Interval
    ) : Reminder

    fun updateTime(startTime: LocalTime): Reminder {
        return updateDateTime(startDateTime.with(startTime))
    }

    fun updateDate(startDate: LocalDate): Reminder {
        return updateDateTime(startDateTime.with(startDate))
    }

    fun updateDateTime(startDateTime: LocalDateTime): Reminder {
        return when (this) {
            is Exact -> copy(startDateTime = startDateTime)
            is Recurring -> copy(startDateTime = startDateTime)
        }
    }
}

@JvmInline
value class ReminderId(val id: UUID) : Serializable

sealed interface Interval {

    object Daily : Interval

    data class Weekly(val dayOfWeek: DayOfWeek) : Interval

    data class Monthly(val dayOfMonth: Int) : Interval

    data class Yearly(val dayOfYear: Int) : Interval
}
