package dev.szymonchaber.checkstory.checklist.template.reminders.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.R

@Composable
@Preview(showBackground = true)
fun BottomSheetContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.new_reminder), style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(16.dp))
        ReminderTypeSelector()
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderTypeSelector() {
    val options = ReminderType.values().map {
        when (it) {
            ReminderType.ONE_TIME -> ToggleOption(it, "One time")
            ReminderType.RECURRING -> ToggleOption(it, "Recurring")
        }
    }
    var currentSelection by remember { mutableStateOf(options.first()) }
    MultiToggleButton(
        currentSelection,
        options,
        {
            currentSelection = it
        },
        Modifier.padding(horizontal = 8.dp)
    )
}

enum class ReminderType {

    ONE_TIME, RECURRING
}
