package dev.szymonchaber.checkstory.notifications

import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

object ReminderStartDateAdjuster {

    fun findCorrectStartDateTime(reminder: Reminder.Recurring, currentDateTime: LocalDateTime): LocalDateTime {
        val startDateTime = reminder.startDateTime
        return if (startDateTime.isBefore(currentDateTime)) {
            when (val interval = reminder.interval) {
                Interval.Daily -> adjustDailyReminder(startDateTime, currentDateTime)
                is Interval.Weekly -> adjustWeeklyReminder(startDateTime, interval, currentDateTime)
                is Interval.Monthly -> adjustMonthlyDate(startDateTime, interval, currentDateTime)
                is Interval.Yearly -> adjustYearlyDate(startDateTime, interval, currentDateTime)
            }
        } else {
            startDateTime
        }
    }

    private fun adjustDailyReminder(
        startDateTime: LocalDateTime,
        currentDateTime: LocalDateTime
    ): LocalDateTime {
        val adjustedStartDateTime = currentDateTime.with(startDateTime.toLocalTime())
        return if (adjustedStartDateTime < currentDateTime) {
            adjustedStartDateTime.plusDays(1)
        } else {
            adjustedStartDateTime
        }
    }

    private fun adjustWeeklyReminder(
        startDateTime: LocalDateTime,
        interval: Interval.Weekly,
        currentDateTime: LocalDateTime
    ): LocalDateTime {
        val adjustedStartDateTime =
            currentDateTime.with(startDateTime.toLocalTime()).with(TemporalAdjusters.nextOrSame(interval.dayOfWeek))
        return if (adjustedStartDateTime < currentDateTime) {
            adjustedStartDateTime.with(TemporalAdjusters.next(interval.dayOfWeek))
        } else {
            adjustedStartDateTime
        }
    }

    private fun adjustYearlyDate(
        startDateTime: LocalDateTime,
        interval: Interval.Yearly,
        currentDateTime: LocalDateTime
    ): LocalDateTime {
        val adjustedStartDateTime = currentDateTime.with(startDateTime.toLocalTime()).withDayOfYear(interval.dayOfYear)
        return if (adjustedStartDateTime < currentDateTime) {
            adjustedStartDateTime.plusYears(1).withDayOfYear(interval.dayOfYear)
        } else {
            adjustedStartDateTime
        }
    }

    private fun adjustMonthlyDate(
        startDateTime: LocalDateTime,
        interval: Interval.Monthly,
        currentDateTime: LocalDateTime
    ): LocalDateTime {
        val adjustedStartDateTime =
            currentDateTime.with(startDateTime.toLocalTime()).withDayOfMonth(interval.dayOfMonth)
        return if (adjustedStartDateTime < currentDateTime) {
            adjustedStartDateTime.plusMonths(1).withDayOfMonth(interval.dayOfMonth)
        } else {
            adjustedStartDateTime
        }
    }
}
