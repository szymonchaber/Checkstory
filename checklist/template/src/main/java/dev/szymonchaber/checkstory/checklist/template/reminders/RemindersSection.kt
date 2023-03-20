package dev.szymonchaber.checkstory.checklist.template.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.R
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.views.AddButton
import dev.szymonchaber.checkstory.design.views.SectionLabel
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate

@Composable
fun RemindersSection(
    checklistTemplate: ChecklistTemplate,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row {
            SectionLabel(
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
                    .align(Alignment.CenterVertically),
                text = stringResource(id = R.string.reminders_label),
            )
            Text(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .background(MaterialTheme.colors.primary, RoundedCornerShape(16.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colors.onPrimary,
                text = stringResource(R.string.pro)
            )
        }
        if (checklistTemplate.reminders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        checklistTemplate.reminders.forEach {
            ReminderItem(it, eventCollector)
        }
        AddButton(
            modifier = Modifier.padding(start = 8.dp),
            onClick = {
                eventCollector(EditTemplateEvent.AddReminderClicked)
            },
            text = stringResource(R.string.new_reminder)
        )
    }
}
