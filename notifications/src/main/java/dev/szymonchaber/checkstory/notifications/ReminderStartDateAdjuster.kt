package dev.szymonchaber.checkstory.notifications

import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

object ReminderStartDateAdjuster {

    fun findCorrectStartDateTime(reminder: Reminder.Recurring, currentDateTime: LocalDateTime): LocalDateTime {
        // TODO unit test this - it adds one day instead of TODAY + one etc.!
        val startDateTime = reminder.startDateTime
        return if (startDateTime.isBefore(currentDateTime)) {
            when (val interval = reminder.interval) {
                Interval.Daily -> {
                    val time = startDateTime.toLocalTime()
                    if (time.isBefore(currentDateTime.toLocalTime())) {
                        currentDateTime.plusDays(1)
                    } else {
                        currentDateTime
                    }.with(time)
                }
                is Interval.Weekly -> {
                    startDateTime.with(TemporalAdjusters.next(interval.dayOfWeek))
                }
                is Interval.Monthly -> {
                    startDateTime.plusMonths(1).withDayOfMonth(startDateTime.dayOfMonth)
                }
                is Interval.Yearly -> {
                    val dateTime = startDateTime
                    dateTime.plusYears(1).withDayOfYear(dateTime.dayOfYear)
                }
            }
        } else {
            startDateTime
        }
    }
}
