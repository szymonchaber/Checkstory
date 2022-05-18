package dev.szymonchaber.checkstory.data.database.model.reminder

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.philjay.Frequency
import com.philjay.RRule
import com.philjay.Weekday
import com.philjay.WeekdayNum
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import java.time.DayOfWeek
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Entity
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val reminderId: Long,
    val templateId: Long,
    val startDateUtc: ZonedDateTime,
    val isRecurring: Boolean,
    val recurrencePattern: String?
) {

    fun toDomainReminder(): Reminder {
        return if (isRecurring) {
            Reminder.Recurring(
                ReminderId(reminderId),
                ChecklistTemplateId(templateId),
                startDateUtc.toLocalDateTime(), // TODO check if storage works correctly
                parseInterval()
            )
        } else {
            Reminder.Exact(
                ReminderId(reminderId),
                ChecklistTemplateId(templateId),
                startDateUtc.toLocalDateTime(), // TODO check if storage works correctly
            )
        }
    }

    private fun parseInterval(): Interval {
        return with(RRule(recurrencePattern!!)) {
            when (freq) {
                Frequency.Daily -> Interval.Daily
                Frequency.Weekly -> Interval.Weekly(byDay.map { toDayOfWeek(it) })
                Frequency.Monthly -> Interval.Monthly(byMonthDay.first())
                Frequency.Yearly -> Interval.Yearly(byYearDay.first())
            }
        }
    }

    private fun toDayOfWeek(weekdayNum: WeekdayNum): DayOfWeek {
        return when (weekdayNum.weekday) {
            Weekday.Monday -> DayOfWeek.MONDAY
            Weekday.Tuesday -> DayOfWeek.TUESDAY
            Weekday.Wednesday -> DayOfWeek.WEDNESDAY
            Weekday.Thursday -> DayOfWeek.THURSDAY
            Weekday.Friday -> DayOfWeek.FRIDAY
            Weekday.Saturday -> DayOfWeek.SATURDAY
            Weekday.Sunday -> DayOfWeek.SUNDAY
        }
    }

    companion object {

        fun fromDomainReminder(reminder: Reminder): ReminderEntity {
            return when (reminder) {
                is Reminder.Exact -> {
                    ReminderEntity(
                        reminder.id.id,
                        reminder.forTemplate.id,
                        reminder.startDateTime.atZone(ZoneOffset.UTC), // TODO check if storage works correctly
                        false,
                        null
                    )
                }
                is Reminder.Recurring -> {
                    ReminderEntity(
                        reminder.id.id,
                        reminder.forTemplate.id,
                        reminder.startDateTime.atZone(ZoneOffset.UTC), // TODO check if storage works correctly
                        true,
                        toRecurrencePattern(reminder.interval)
                    )
                }
            }
        }

        private fun toRecurrencePattern(interval: Interval): String {
            return RRule().apply {
                wkst = Weekday.Monday
                freq = when (interval) {
                    Interval.Daily -> {
                        Frequency.Daily
                    }
                    is Interval.Weekly -> {
                        byDay.addAll(toWeekdays(interval.daysOfWeek))
                        Frequency.Weekly
                    }
                    is Interval.Monthly -> {
                        byMonthDay.add(interval.dayOfMonth)
                        Frequency.Monthly
                    }
                    is Interval.Yearly -> {
                        byYearDay.add(interval.dayOfYear)
                        Frequency.Yearly
                    }
                }
            }.toRFC5545String()
        }

        private fun toWeekdays(daysOfWeek: List<DayOfWeek>): List<WeekdayNum> {
            return daysOfWeek.map {
                val weekday = when (it) {
                    DayOfWeek.MONDAY -> Weekday.Monday
                    DayOfWeek.TUESDAY -> Weekday.Tuesday
                    DayOfWeek.WEDNESDAY -> Weekday.Wednesday
                    DayOfWeek.THURSDAY -> Weekday.Thursday
                    DayOfWeek.FRIDAY -> Weekday.Friday
                    DayOfWeek.SATURDAY -> Weekday.Saturday
                    DayOfWeek.SUNDAY -> Weekday.Sunday
                }
                WeekdayNum(0, weekday)
            }
        }
    }
}