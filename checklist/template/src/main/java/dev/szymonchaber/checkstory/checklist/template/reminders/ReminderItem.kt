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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.R
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import java.time.DayOfWeek
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
            stringResource(R.string.reminder_one_time, reminder.startDateTime.format(dateTimeFormatter))
        }
        is Reminder.Recurring -> {
            val formattedTime = reminder.startDateTime.format(timeFormatter)
            when (val interval = reminder.interval) {
                Interval.Daily -> {
                    stringResource(R.string.interval_daily_at, formattedTime)
                }
                is Interval.Monthly -> {
                    stringResource(R.string.every_month_on_day_at, interval.dayOfMonth, formattedTime)
                }
                is Interval.Weekly -> {
                    stringResource(R.string.every_week_on_day_at, formatWeekDay(interval.dayOfWeek), formattedTime)
                }
                is Interval.Yearly -> {
                    stringResource(R.string.every_year_on_day_at, interval.dayOfYear, formattedTime)
                }
            }
        }
    }
    Row(
        modifier = Modifier.padding(start = 24.dp)
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .clickable {
                    eventCollector(EditTemplateEvent.ReminderClicked(reminder))
                },
            text = text
        )
        IconButton(onClick = { eventCollector(EditTemplateEvent.DeleteReminderClicked(reminder)) }) {
            Icon(Icons.Filled.Delete, "")
        }
    }
}

@Composable
private fun formatWeekDay(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> stringResource(R.string.monday_accusative)
        DayOfWeek.TUESDAY -> stringResource(R.string.tuesday_accusative)
        DayOfWeek.WEDNESDAY -> stringResource(R.string.wednesday_accusative)
        DayOfWeek.THURSDAY -> stringResource(R.string.thursday_accusative)
        DayOfWeek.FRIDAY -> stringResource(R.string.friday_accusative)
        DayOfWeek.SATURDAY -> stringResource(R.string.saturday_accusative)
        DayOfWeek.SUNDAY -> stringResource(R.string.sunday_accusative)
    }
}
