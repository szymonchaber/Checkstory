package dev.szymonchaber.checkstory.notifications

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month

class ReminderStartDateAdjusterTest {

    @Test
    fun `given daily reminder with start date 5 days before today and time before current, when called, then should return tomorrow date`() {
        // given
        val date5DaysBefore = CURRENT.minusDays(5).minusMinutes(10)
        val dailyReminder = Reminder.Recurring(ReminderId(0), ChecklistTemplateId(0), date5DaysBefore, Interval.Daily)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(dailyReminder, CURRENT)

        // then
        assertEquals(CURRENT.plusDays(1).withMinute(10), correctDateTime)
    }

    @Test
    fun `given daily reminder with start date 5 days before today and time after current, when called, then should return today date`() {
        // given
        val date5DaysBefore = CURRENT.minusDays(5).plusMinutes(10)
        val dailyReminder = Reminder.Recurring(ReminderId(0), ChecklistTemplateId(0), date5DaysBefore, Interval.Daily)

        // when
        val correctDateTime = ReminderStartDateAdjuster.findCorrectStartDateTime(dailyReminder, CURRENT)

        // then
        assertEquals(CURRENT.withMinute(30), correctDateTime)
    }

    companion object {

        private val CURRENT = LocalDateTime.of(2022, Month.JUNE, 18, 20, 20)
    }
}
