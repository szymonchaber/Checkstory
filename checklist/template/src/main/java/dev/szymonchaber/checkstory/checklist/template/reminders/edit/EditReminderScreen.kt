package dev.szymonchaber.checkstory.checklist.template.reminders.edit

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
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
import com.vanpra.composematerialdialogs.datetime.date.datepicker
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
@Preview(showBackground = true)
fun EditReminderScreen(
    reminderId: ReminderId? = null,
    onReminderSaved: (Reminder) -> Unit = {}
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
            is EditReminderEffect.RelayReminderToSave -> onReminderSaved(value.reminder)
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
        }
    }
    Button(onClick = { onEvent(EditReminderEvent.SaveReminderClicked) }) {
        Text("Save")
    }
}

@Composable
fun ExactReminderView(reminder: Exact, onEvent: (EditReminderEvent) -> Unit) {
    ReminderTimeSection(reminder, onEvent)
    ReminderDateSection(reminder, onEvent)
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
            .padding(top = 16.dp), interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            dialogState.show()
                        }
                    }
                }
            },
        value = reminder.startDateTime.format(timeFormatter),
        label = { Text(text = "Time") }, // TODO string resource
        onValueChange = {},
        readOnly = true
    )
}

@Composable
private fun ReminderDateSection(
    reminder: Exact,
    onEvent: (EditReminderEvent) -> Unit
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())
    }
    val dialogState = rememberMaterialDialogState()
    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton("Ok")
            negativeButton("Cancel")
        }
    ) {
        datepicker(
            allowedDateValidator = { it.isAfter(LocalDate.now().minusDays(1)) },
            initialDate = reminder.startDateTime.toLocalDate()
        ) { date ->
            onEvent(EditReminderEvent.ReminderDateSet(date))
        }
    }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        if (it is PressInteraction.Release) {
                            dialogState.show()
                        }
                    }
                }
            },
        value = reminder.startDateTime.format(dateFormatter),
        label = { Text(text = "Date") }, // TODO string resource
        onValueChange = {},
        readOnly = true
    )
}

enum class ReminderType {

    EXACT, RECURRING
}
