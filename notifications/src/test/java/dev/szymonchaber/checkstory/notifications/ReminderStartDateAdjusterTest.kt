package dev.szymonchaber.checkstory.notifications

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.Month

class ReminderStartDateAdjusterTest {

    @Test
    fun `given daily reminder with start date 5 days before today and time before current, when called, then should return tomorrow date`() {
        // given
        val date5DaysBefore = CURRENT.minusDays(5).minusMinutes(10)
        val interval = Interval.Daily
        val dailyReminder = reminder(date5DaysBefore, interval)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            dailyReminder.startDateTime,
            dailyReminder.interval
        )

        // then
        assertEquals(CURRENT.plusDays(1).withMinute(10), correctDateTime)
    }

    @Test
    fun `given daily reminder with start date 5 days before today and time after current, when called, then should return today date`() {
        // given
        val date5DaysBefore = CURRENT.minusDays(5).plusMinutes(10)
        val dailyReminder = reminder(date5DaysBefore, Interval.Daily)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            dailyReminder.startDateTime,
            dailyReminder.interval
        )

        // then
        assertEquals(CURRENT.withMinute(30), correctDateTime)
    }

    @Test
    fun `given weekly reminder with start date 14 days before today and time before current, when called, then should return next day of week date`() {
        // given
        val date14DaysBefore = CURRENT.minusDays(14).minusMinutes(10)
        val interval = Interval.Weekly(DayOfWeek.SATURDAY)
        val weeklyReminder = reminder(date14DaysBefore, interval)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            weeklyReminder.startDateTime,
            weeklyReminder.interval
        )

        // then
        assertEquals(CURRENT.plusDays(7).withMinute(10), correctDateTime)
    }

    @Test
    fun `given weekly reminder with start date 14 days before today and time after current, when called, then should return today date`() {
        // given
        val date14DaysBefore = CURRENT.minusDays(14).plusMinutes(10)
        val interval = Interval.Weekly(DayOfWeek.SATURDAY)
        val weeklyReminder = reminder(date14DaysBefore, interval)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            weeklyReminder.startDateTime,
            weeklyReminder.interval
        )

        // then
        assertEquals(CURRENT.withMinute(30), correctDateTime)
    }

    @Test
    fun `given monthly reminder is due today in 10 minutes, when called, then should return date today in 10 minutes`() {
        // given
        val startDate = CURRENT.minusDays(35).plusMinutes(10)
        val dayOfMonth = CURRENT.dayOfMonth
        val interval = Interval.Monthly(dayOfMonth)
        val reminder = reminder(startDate, interval)

        // when
        val date = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            reminder.startDateTime,
            reminder.interval
        )

        // then
        assertEquals(CURRENT.plusMinutes(10), date)
    }

    @Test
    fun `given monthly reminder was due today 10 minutes before, when called, then should return date in a month`() {
        // given
        val startDate = CURRENT.minusDays(35).minusMinutes(10)
        val dayOfMonth = CURRENT.dayOfMonth
        val interval = Interval.Monthly(dayOfMonth)
        val reminder = reminder(startDate, interval)

        // when
        val date = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            reminder.startDateTime,
            reminder.interval
        )

        // then
        assertEquals(CURRENT.plusMonths(1).withDayOfMonth(dayOfMonth).minusMinutes(10), date)
    }

    @Test
    fun `given monthly reminder with day of month today and time before current, when called, then should return next day of month date`() {
        // given
        val date14DaysBefore = CURRENT.minusDays(35).minusMinutes(10)
        val interval = Interval.Monthly(18)
        val monthlyReminder = reminder(date14DaysBefore, interval)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            monthlyReminder.startDateTime,
            monthlyReminder.interval
        )

        // then
        assertEquals(CURRENT.plusMonths(1).withDayOfMonth(18).withMinute(10), correctDateTime)
    }

    @Test
    fun `given monthly reminder with day of month today and time after current, when called, then should return today date`() {
        // given
        val date14DaysBefore = CURRENT.minusDays(35).plusMinutes(10)
        val interval = Interval.Monthly(18)
        val monthlyReminder = reminder(date14DaysBefore, interval)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            monthlyReminder.startDateTime,
            monthlyReminder.interval
        )

        // then
        assertEquals(CURRENT.withMinute(30), correctDateTime)
    }

    @Test
    fun `given yearly reminder with day of year today and time before current, when called, then should return next day of year date`() {
        // given
        val date14DaysBefore = CURRENT.minusDays(370).minusMinutes(10)
        val interval = Interval.Yearly(169)
        val yearlyReminder = reminder(date14DaysBefore, interval)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            yearlyReminder.startDateTime,
            yearlyReminder.interval
        )

        // then
        assertEquals(CURRENT.plusYears(1).withDayOfYear(169).withMinute(10), correctDateTime)
    }

    @Test
    fun `given yearly reminder with day of year today and time after current, when called, then should return today date`() {
        // given
        val date14DaysBefore = CURRENT.minusDays(370).plusMinutes(10)
        val interval = Interval.Yearly(169)
        val yearlyReminder = reminder(date14DaysBefore, interval)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(
            CURRENT,
            yearlyReminder.startDateTime,
            yearlyReminder.interval
        )

        // then
        assertEquals(CURRENT.withMinute(30), correctDateTime)
    }

    private fun reminder(startDateTime: LocalDateTime, interval: Interval): Reminder.Recurring {
        return Reminder.Recurring(ReminderId(0), ChecklistTemplateId(0), startDateTime, interval)
    }

    companion object {

        private val CURRENT = LocalDateTime.of(2022, Month.JUNE, 18, 20, 20)
    }
}
