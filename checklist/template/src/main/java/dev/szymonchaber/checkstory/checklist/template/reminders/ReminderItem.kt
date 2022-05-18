package dev.szymonchaber.checkstory.checklist.template.reminders

import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder

@Composable
fun ReminderItem(reminder: Reminder, eventCollector: (EditTemplateEvent) -> Unit) {
    when (reminder) {
        is Reminder.Exact -> {
            Text(
                modifier = Modifier.clickable {
                    eventCollector(EditTemplateEvent.AddReminderClicked)
                },
                text = reminder.startDateTime.toString()
            )
        }
    }
}
