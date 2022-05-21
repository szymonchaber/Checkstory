package dev.szymonchaber.checkstory.checklist.template.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.R
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.views.AddButton
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate

@Composable
fun RemindersSection(
    checklistTemplate: ChecklistTemplate,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(stringResource(id = R.string.reminders_label))
        Spacer(modifier = Modifier.height(8.dp))
        checklistTemplate.reminders.forEach {
            ReminderItem(it, eventCollector)
        }
        AddButton(
            modifier = Modifier.padding(top = 8.dp),
            onClick = {
                eventCollector(EditTemplateEvent.AddReminderClicked)
            },
            text = stringResource(R.string.new_reminder)
        )
    }
}
