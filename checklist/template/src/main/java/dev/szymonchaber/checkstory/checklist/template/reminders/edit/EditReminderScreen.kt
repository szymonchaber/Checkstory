package dev.szymonchaber.checkstory.checklist.template.reminders.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import dev.szymonchaber.checkstory.checklist.template.R
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEffect
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEvent
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderLoadingState
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderState
import dev.szymonchaber.checkstory.checklist.template.reminders.EditReminderViewModel
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder.Exact
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder.Recurring
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
@Preview(showBackground = true)
fun EditReminderScreen(
    reminderId: ReminderId? = null
) {
    val viewModel = hiltViewModel<EditReminderViewModel>()
    LaunchedEffect(reminderId) {
        reminderId?.let {
            viewModel.onEvent(EditReminderEvent.EditReminder(it))
        } ?: run {
            viewModel.onEvent(EditReminderEvent.CreateReminder)
        }
    }

    val state by viewModel.state.collectAsState(initial = EditReminderState.initial)

    val effect by viewModel.effect.collectAsState(initial = null)
    LaunchedEffect(effect) {
        when (val value = effect) {
            is EditReminderEffect.CloseScreen -> {
//                navController.navigateUp() TODO close bottom sheet
            }
            null -> Unit
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.new_reminder), style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(16.dp))
        when (val loadingState = state.reminderLoadingState) {
            EditReminderLoadingState.Loading -> {
                // TODO
            }
            is EditReminderLoadingState.Success -> {
                EditReminderView(loadingState.reminder) { viewModel.onEvent(it) }
            }
        }
    }
}

@Composable
private fun EditReminderView(reminder: Reminder, onEvent: (EditReminderEvent) -> Unit) {
    ReminderTypeSelector(reminder, onEvent)
}

@Composable
fun ReminderTypeSelector(reminder: Reminder, onEvent: (EditReminderEvent) -> Unit) {
    val options = ReminderType.values().map {
        when (it) {
            ReminderType.EXACT -> ToggleOption(it, "One time")
            ReminderType.RECURRING -> ToggleOption(it, "Recurring")
        }
    }
    val currentSelection = when (reminder) {
        is Exact -> ReminderType.EXACT
        is Recurring -> ReminderType.RECURRING
    }
    MultiToggleButton(
        currentSelection,
        options,
        {
            onEvent(EditReminderEvent.ReminderTypeSelected(it))
        },
        Modifier.padding(horizontal = 8.dp)
    )
    when (reminder) {
        is Exact -> ExactReminderView(reminder, onEvent)
        is Recurring -> {
        }// TODO
    }
}

@Composable
fun ExactReminderView(reminder: Exact, onEvent: (EditReminderEvent) -> Unit) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())
    }
    ReminderTimeSection(reminder, onEvent)
}

@Composable
private fun ReminderTimeSection(
    reminder: Exact,
    onEvent: (EditReminderEvent) -> Unit
) {
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    }
    val dialogState = rememberMaterialDialogState()
    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton("Ok")
            negativeButton("Cancel")
        }
    ) {
        timepicker(is24HourClock = true, initialTime = reminder.startDateTime.toLocalTime()) { time ->
            onEvent(EditReminderEvent.ReminderTimeSet(time))
        }
    }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clickable {
                dialogState.show()
            },
        value = reminder.startDateTime.format(timeFormatter),
        label = { Text(text = "Time") }, // TODO string resource
        onValueChange = {},
        readOnly = true
    )
}

enum class ReminderType {

    EXACT, RECURRING
}
