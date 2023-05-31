package dev.szymonchaber.checkstory.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplateUseCase
import dev.szymonchaber.checkstory.domain.usecase.GetReminderUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationsManager: NotificationsManager

    @Inject
    lateinit var getChecklistTemplateUseCase: GetChecklistTemplateUseCase

    @Inject
    lateinit var getReminderUseCase: GetReminderUseCase

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val templateId = intent.getStringExtra(KEY_TEMPLATE_ID)?.let(ChecklistTemplateId::fromUuidString) ?: return
        val reminderId = intent.getStringExtra(KEY_REMINDER_ID)?.let(ReminderId::fromUuidString) ?: return
        CoroutineScope(Dispatchers.Main)
            .launch {
                val reminder = getReminderUseCase.getReminder(reminderId) ?: return@launch
                val template = getChecklistTemplateUseCase.getTemplate(templateId) ?: return@launch
                val newChecklistIntent = Intent(
                    Intent.ACTION_VIEW,
                    "app://checkstory/checklist/new/${template.id.id}".toUri()
                )
                val recentChecklistIntent = template.checklists.firstOrNull()?.let {
                    Intent(
                        Intent.ACTION_VIEW,
                        "app://checkstory/checklist/fill/${it.id.id}".toUri()
                    )
                }
                notificationsManager.sendReminderNotification(
                    reminderId,
                    context.getString(R.string.notification_reminder_body, template.title),
                    newChecklistIntent,
                    recentChecklistIntent
                )
                if (reminder is Reminder.Recurring) {
                    reminderScheduler.scheduleNextOccurrence(reminder)
                }
            }
    }

    companion object {

        private const val KEY_TEMPLATE_ID = "TEMPLATE_ID"
        private const val KEY_REMINDER_ID = "REMINDER_ID"

        fun newIntent(context: Context, templateId: ChecklistTemplateId, reminderId: ReminderId): Intent {
            return Intent(context, ReminderReceiver::class.java)
                .putExtra(KEY_TEMPLATE_ID, templateId.id.toString())
                .putExtra(KEY_REMINDER_ID, reminderId.id.toString())
        }
    }
}
