package dev.szymonchaber.checkstory.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.AlarmManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TemplateReminderRepository
) {

    private var alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        GlobalScope.launch {
            repository.getAllReminders()
                .collect {
                    it.forEach(::scheduleReminder)
                }
        }
    }

    private fun scheduleReminder(reminder: Reminder) {
        when (reminder) {
            is Reminder.Exact -> scheduleExactReminder(reminder)
            is Reminder.Recurring -> scheduleRecurringReminder(reminder, skipNearest = false)
        }
    }

    private fun scheduleExactReminder(reminder: Reminder.Exact) {
        val startDateTime = reminder.startDateTime
        if (startDateTime.isBefore(LocalDateTime.now())) {
            return
        }
        val toEpochMilli = startDateTime.toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
            .toEpochMilli()
        logReminderScheduling(toEpochMilli, reminder)
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            toEpochMilli,
            createIntent(reminder)
        )
    }

    fun scheduleNextOccurrence(reminder: Reminder.Recurring) {
        scheduleRecurringReminder(reminder, skipNearest = true)
    }

    private fun scheduleRecurringReminder(reminder: Reminder.Recurring, skipNearest: Boolean) {
        val startDateTime = findCorrectStartDateTime(reminder, skipNearest = skipNearest)
        val toEpochMilli = startDateTime.toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
            .toEpochMilli()
        logReminderScheduling(toEpochMilli, reminder)

        // alarmManager.setRepeating() doesn't work when the device is idle.
        // Instead, we re-schedule recurring alarms within ReminderReceiver on each notification sent
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            toEpochMilli,
            createIntent(reminder)
        )
    }

    private fun logReminderScheduling(toEpochMilli: Long, reminder: Reminder) {
        val minuteDelta = (toEpochMilli - Instant.now().toEpochMilli()) / 1000 / 60
        if (minuteDelta < 0) {
            throw IllegalStateException("Cannot schedule reminders for past date. Attempted minute delta: $minuteDelta")
        }
        Timber.d("Scheduling reminder: $reminder\nFiring in $minuteDelta minutes")
    }

    private fun findCorrectStartDateTime(reminder: Reminder.Recurring, skipNearest: Boolean): LocalDateTime {
        val now = LocalDateTime.now()
        val currentDateTime = if (skipNearest) {
            // we schedule the next occurrence of the reminder when its displayed.
            // Without this line it would just schedule the reminder for the same time
            now.plusMinutes(1)
        } else {
            now
        }
        return ReminderStartDateAdjuster.findCorrectStartDateTime(reminder, currentDateTime)
    }

    private fun createIntent(reminder: Reminder): PendingIntent {
        return ReminderReceiver.newIntent(context, reminder.forTemplate, reminder.id)
            .let {
                PendingIntent.getBroadcast(
                    context,
                    reminder.id.id.hashCode(),
                    it,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
    }
}
