package dev.szymonchaber.checkstory.domain.model.checklist.template.reminder

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import java.io.Serializable
import java.time.DayOfWeek
import java.time.LocalDateTime

sealed interface Reminder {

    val id: ReminderId
    val forTemplate: ChecklistTemplateId

    val isStored: Boolean
        get() = id.id != 0L

    data class Exact(
        override val id: ReminderId,
        override val forTemplate: ChecklistTemplateId,
        val dateTime: LocalDateTime
    ) : Reminder

    data class Recurring(
        override val id: ReminderId,
        override val forTemplate: ChecklistTemplateId,
        val startDateTime: LocalDateTime,
        val interval: Interval
    ) : Reminder
}

@JvmInline
value class ReminderId(val id: Long) : Serializable

sealed interface Interval {

    object Daily : Interval

    data class Weekly(val daysOfWeek: List<DayOfWeek>) : Interval

    data class Monthly(val dayOfMonth: Int) : Interval

    data class Yearly(val dayOfYear: Int) : Interval
}
