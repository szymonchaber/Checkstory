package dev.szymonchaber.checkstory.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import dev.szymonchaber.checkstory.domain.usecase.GetChecklistTemplateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationsManager: NotificationsManager

    @Inject
    lateinit var getChecklistTemplateUseCase: GetChecklistTemplateUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(KEY_TEMPLATE_ID, -1)
        if (id < 0) {
            return
        }
        val templateId = ChecklistTemplateId(id)
        CoroutineScope(Dispatchers.Main)
            .launch {
                val template = getChecklistTemplateUseCase.getChecklistTemplate(templateId).first()
                notificationsManager.sendNotification("Time to fill out ${template.title} checklist!")
            }
    }

    companion object {

        private const val KEY_TEMPLATE_ID = "TEMPLATE_ID"

        fun newIntent(context: Context, templateId: ChecklistTemplateId): Intent {
            return Intent(context, ReminderReceiver::class.java)
                .putExtra(KEY_TEMPLATE_ID, templateId.id)
        }
    }
}