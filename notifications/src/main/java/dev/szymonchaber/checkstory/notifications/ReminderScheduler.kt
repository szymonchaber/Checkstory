package dev.szymonchaber.checkstory.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(FlowPreview::class)
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TemplateReminderRepository
) {

    private var alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        GlobalScope.launch {
            repository.getAllReminders()
                .flatMapConcat {
                    flowOf(*it.toTypedArray())
                }
                .collect {
                    scheduleReminder(it)
                }
        }
    }

    private fun scheduleReminder(reminder: Reminder) {
        when (reminder) {
            is Reminder.Exact -> scheduleExactReminder(reminder)
            is Reminder.Recurring -> scheduleRecurringReminder(reminder)
        }
    }

    private fun scheduleExactReminder(reminder: Reminder.Exact) {
        val startDateTime = reminder.startDateTime
        if (startDateTime.isBefore(LocalDateTime.now())) {
            return
        }
        val toEpochMilli = startDateTime.toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
            .toEpochMilli()
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            toEpochMilli,
            createIntent(reminder)
        )
    }

    private fun scheduleRecurringReminder(reminder: Reminder.Recurring) {
        Timber.d("scheduling recurring reminder: $reminder") // TODO
        val startDateTime = findCorrectStartDateTime(reminder)

        val toEpochMilli = startDateTime.toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
            .toEpochMilli()
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            toEpochMilli,
            findCorrectInterval(reminder.interval),
            createIntent(reminder)
        )
    }

    private fun findCorrectInterval(interval: Interval): Long {
        // TODO this should possibly only be used to schedule actual alarms for monthly and yearly
        return when (interval) {
            Interval.Daily -> TimeUnit.DAYS.toMillis(1)
            is Interval.Monthly -> TimeUnit.DAYS.toMillis(7)
            is Interval.Weekly -> TimeUnit.DAYS.toMillis(30)
            is Interval.Yearly -> TimeUnit.DAYS.toMillis(365)
        }
    }

    private fun findCorrectStartDateTime(reminder: Reminder.Recurring): LocalDateTime {
        return if (reminder.startDateTime.isBefore(LocalDateTime.now())) {
            when (reminder.interval) {
                Interval.Daily -> {
                    reminder.startDateTime.plusDays(1)
                }
                is Interval.Weekly -> {
                    reminder.startDateTime.plusWeeks(1)
                }
                is Interval.Monthly -> {
                    reminder.startDateTime.plusMonths(1).withDayOfMonth(reminder.startDateTime.dayOfMonth)
                }
                is Interval.Yearly -> {
                    val dateTime = reminder.startDateTime
                    dateTime.plusYears(1).withDayOfYear(dateTime.dayOfYear)
                }
            }
        } else {
            reminder.startDateTime
        }
    }

    private fun createIntent(reminder: Reminder): PendingIntent {
        return ReminderReceiver.newIntent(context, reminder.forTemplate)
            .let {
                PendingIntent.getBroadcast(
                    context,
                    reminder.id.id.toInt(),
                    it,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
    }
}
