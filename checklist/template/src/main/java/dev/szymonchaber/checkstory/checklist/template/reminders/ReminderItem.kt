package dev.szymonchaber.checkstory.checklist.template.reminders

import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ReminderItem(reminder: Reminder, eventCollector: (EditTemplateEvent) -> Unit) {

    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMMM yyy, HH:mm", Locale.getDefault())
    }

    when (reminder) {
        is Reminder.Exact -> {
            Text(
                modifier = Modifier.clickable {
                    eventCollector(EditTemplateEvent.AddReminderClicked)
                },
                text = reminder.startDateTime.format(dateTimeFormatter)
            )
        }
    }
}
