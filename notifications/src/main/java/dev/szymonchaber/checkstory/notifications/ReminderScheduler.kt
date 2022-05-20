package dev.szymonchaber.checkstory.notifications

import android.util.Log
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.repository.TemplateReminderRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(FlowPreview::class)
@Singleton
class ReminderScheduler @Inject constructor(private val repository: TemplateReminderRepository) {

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

    private fun scheduleRecurringReminder(reminder: Reminder.Recurring) {
        Log.d("ReminderScheduler", "scheduling recurring reminder: $reminder") // TODO
    }

    private fun scheduleExactReminder(reminder: Reminder.Exact) {
        Log.d("ReminderScheduler", "scheduling exact reminder: $reminder") // TODO
    }
}
