package dev.szymonchaber.checkstory.checklist.template.reminders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ReminderItem(reminder: Reminder, eventCollector: (EditTemplateEvent) -> Unit) {
    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMMM yyy, HH:mm", Locale.getDefault())
    }
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    }

    val text = when (reminder) {
        is Reminder.Exact -> {
            "One time on ${reminder.startDateTime.format(dateTimeFormatter)}"
        }
        is Reminder.Recurring -> {
            "Daily at ${reminder.startDateTime.format(timeFormatter)}"
        }
    }
    Row(
        modifier = Modifier.padding(start = 16.dp)
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .clickable {
                    eventCollector(EditTemplateEvent.AddReminderClicked)
                },
            text = text
        )
        IconButton(onClick = { eventCollector(EditTemplateEvent.DeleteReminderClicked(reminder)) }) {
            Icon(Icons.Filled.Delete, "")
        }
    }
}
