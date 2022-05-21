package dev.szymonchaber.checkstory.checklist.template.reminders.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
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
        is Recurring -> RecurringReminderView(reminder, onEvent)
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
fun RecurringReminderView(reminder: Recurring, onEvent: (EditReminderEvent) -> Unit) {
    IntervalSection(reminder.interval, onEvent)
    ReminderTimeSection(reminder, onEvent)
}

@Composable
private fun ReminderTimeSection(
    reminder: Reminder,
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

@Composable
fun IntervalSection(interval: Interval, onEvent: (EditReminderEvent) -> Unit) {
    val intervalOptions = listOf(
        // TODO resources
        ToggleOption(IntervalType.DAILY, "Daily"),
//        ToggleOption(IntervalType.WEEKLY, "Weekly"),
//        ToggleOption(IntervalType.MONTHLY, "Monthly"),
//        ToggleOption(IntervalType.YEARLY, "Yearly"),
    )
    val intervalType = when (interval) {
        Interval.Daily -> IntervalType.DAILY
        is Interval.Weekly -> IntervalType.WEEKLY
        is Interval.Monthly -> IntervalType.MONTHLY
        is Interval.Yearly -> IntervalType.YEARLY
    }
    var isOpen by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(top = 16.dp)) {
        Column {
            OutlinedTextField(
                value = intervalOptions.first { it.tag == intervalType }.text,
                onValueChange = { },
                label = { Text(text = "Interval") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            DropDownList(
                requestToOpen = isOpen,
                list = intervalOptions,
                {
                    isOpen = it
                }
            ) {
                onEvent(EditReminderEvent.IntervalSelected(it))
            }
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .padding(10.dp)
                .clickable(
                    onClick = { isOpen = true }
                )
        )
    }
}

@Composable
fun <T> DropDownList(
    requestToOpen: Boolean = false,
    list: List<ToggleOption<T>>,
    request: (Boolean) -> Unit,
    onItemSelected: (T) -> Unit
) {
    DropdownMenu(
        modifier = Modifier.fillMaxWidth(),
        expanded = requestToOpen,
        onDismissRequest = { request(false) },
    ) {
        list.forEach {
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    request(false)
                    onItemSelected(it.tag)
                }
            ) {
                Text(it.text, modifier = Modifier.wrapContentWidth())
            }
        }
    }
}

enum class IntervalType {
    DAILY, WEEKLY, MONTHLY, YEARLY
}
