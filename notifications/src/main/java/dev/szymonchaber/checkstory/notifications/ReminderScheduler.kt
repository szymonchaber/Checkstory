package dev.szymonchaber.checkstory.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(FlowPreview::class)
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TemplateReminderRepository
) {

    private var alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
        val alarmIntent = ReminderReceiver.newIntent(context, reminder.forTemplate)
            .let {
                PendingIntent.getBroadcast(
                    context,
                    reminder.id.id.toInt(),
                    it,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }


        // TODO How to cancel these on removal?
        val toEpochMilli =
            reminder.startDateTime.toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now()))
                .toEpochMilli()
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            toEpochMilli,
            alarmIntent
        )
    }

    private fun scheduleRecurringReminder(reminder: Reminder.Recurring) {
        Log.d("ReminderScheduler", "scheduling recurring reminder: $reminder") // TODO
    }
}
