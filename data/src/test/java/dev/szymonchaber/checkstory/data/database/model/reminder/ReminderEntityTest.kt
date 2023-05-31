package dev.szymonchaber.checkstory.data.database.model.reminder

import com.google.common.truth.Truth
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*

class ReminderEntityTest {

    @Test
    fun `should store daily reminder correctly`() {
        // given
        val reminder = reminder(Interval.Daily)

        // when
        val entity = ReminderEntity.fromDomainReminder(reminder)

        // then
        Truth.assertThat(entity.isRecurring).isTrue()
        Truth.assertThat(entity.recurrencePattern).isEqualTo("RRULE:FREQ=DAILY;WKST=MO")
    }

    @Test
    fun `should store weekly reminder correctly`() {
        // given
        val reminder = reminder(
            Interval.Weekly(
                DayOfWeek.MONDAY,
            )
        )

        // when
        val entity = ReminderEntity.fromDomainReminder(reminder)

        // then
        Truth.assertThat(entity.isRecurring).isTrue()
//        Truth.assertThat(entity.recurrencePattern).isEqualTo("RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;WKST=MO")
        Truth.assertThat(entity.recurrencePattern).isEqualTo("RRULE:FREQ=WEEKLY;BYDAY=MO;WKST=MO")
    }

    @Test
    fun `should store monthly reminder correctly`() {
        // given
        val reminder = reminder(Interval.Monthly(15))

        // when
        val entity = ReminderEntity.fromDomainReminder(reminder)

        // then
        Truth.assertThat(entity.isRecurring).isTrue()
        Truth.assertThat(entity.recurrencePattern).isEqualTo("RRULE:FREQ=MONTHLY;BYMONTHDAY=15;WKST=MO")
    }

    @Test
    fun `should store yearly reminder correctly`() {
        // given
        val reminder = reminder(Interval.Yearly(150))

        // when
        val entity = ReminderEntity.fromDomainReminder(reminder)

        // then
        Truth.assertThat(entity.isRecurring).isTrue()
        Truth.assertThat(entity.recurrencePattern).isEqualTo("RRULE:FREQ=YEARLY;BYYEARDAY=150;WKST=MO")
    }

    @Test
    fun `should restore daily reminder correctly`() {
        // given
        val rrule = "RRULE:FREQ=DAILY;WKST=MO"
        val reminderEntity = reminderEntity(rrule)

        // when
        val reminder = reminderEntity.toDomainReminder()

        // then
        Truth.assertThat((reminder as Reminder.Recurring).interval).isEqualTo(Interval.Daily)
    }

    @Test
    fun `should restore weekly reminder correctly`() {
        // given
//        val rrule = "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;WKST=MO"
        val rrule = "RRULE:FREQ=WEEKLY;BYDAY=MO;WKST=MO"
        val reminderEntity = reminderEntity(rrule)

        // when
        val reminder = reminderEntity.toDomainReminder()

        // then
        Truth.assertThat((reminder as Reminder.Recurring).interval).isEqualTo(
            Interval.Weekly(
//                listOf(
                DayOfWeek.MONDAY,
//                    DayOfWeek.TUESDAY,
//                    DayOfWeek.WEDNESDAY,
//                    DayOfWeek.THURSDAY,
//                    DayOfWeek.FRIDAY
//                )
            )
        )
    }

    @Test
    fun `should restore monthly reminder correctly`() {
        // given
        val rrule = "RRULE:FREQ=MONTHLY;BYMONTHDAY=15;WKST=MO"
        val reminderEntity = reminderEntity(rrule)

        // when
        val reminder = reminderEntity.toDomainReminder()

        // then
        Truth.assertThat((reminder as Reminder.Recurring).interval).isEqualTo(Interval.Monthly(15))
    }

    @Test
    fun `should restore yearly reminder correctly`() {
        // given
        val rrule = "RRULE:FREQ=YEARLY;BYYEARDAY=150;WKST=MO"
        val reminderEntity = reminderEntity(rrule)

        // when
        val reminder = reminderEntity.toDomainReminder()

        // then
        Truth.assertThat((reminder as Reminder.Recurring).interval).isEqualTo(Interval.Yearly(150))
    }

    private fun reminderEntity(rrule: String): ReminderEntity {
        return ReminderEntity(UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), true, rrule)
    }

    private fun reminder(interval: Interval): Reminder.Recurring {
        return Reminder.Recurring(
            ReminderId(UUID.randomUUID()),
            ChecklistTemplateId.new(),
            LocalDateTime.of(2022, 4, 18, 14, 0),
            interval = interval
        )
    }
}
