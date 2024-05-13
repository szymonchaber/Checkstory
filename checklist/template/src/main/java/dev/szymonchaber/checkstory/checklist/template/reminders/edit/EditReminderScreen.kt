package dev.szymonchaber.checkstory.checklist.template.reminders.edit

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEffect
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderEvent
import dev.szymonchaber.checkstory.checklist.template.edit.model.EditReminderLoadingState
import dev.szymonchaber.checkstory.checklist.template.reminders.EditReminderViewModel
import dev.szymonchaber.checkstory.design.R
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Interval
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder.Exact
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder.Recurring
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun EditReminderScreen(
    viewModel: EditReminderViewModel,
    onReminderSaved: (Reminder) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                is EditReminderEffect.RelayReminderToSave -> onReminderSaved(it.reminder)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.new_reminder),
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        when (val loadingState = state.reminderLoadingState) {
            EditReminderLoadingState.Loading -> {
                // TODO
            }

            is EditReminderLoadingState.Success -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionEnsuringEditReminderView(loadingState.reminder, viewModel::onEvent)
                } else {
                    EditReminderView(loadingState.reminder, viewModel::onEvent)
                }
            }
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
private fun ColumnScope.PermissionEnsuringEditReminderView(reminder: Reminder, onEvent: (EditReminderEvent) -> Unit) {
    val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    if (permissionState.status.isGranted) {
        EditReminderView(reminder, onEvent)
    } else {
        Column {
            val textToShow = if (permissionState.status.shouldShowRationale) {
                "We need a permission to show notifications to remind you about your tasks."
            } else {
                "Notification permission is not granted. Without it, you won't receive reminders."
            }
            Text(textToShow)
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@Composable
private fun ColumnScope.EditReminderView(reminder: Reminder, onEvent: (EditReminderEvent) -> Unit) {
    val options = ReminderType.entries.map {
        when (it) {
            ReminderType.EXACT -> ToggleOption(it, stringResource(R.string.one_time))
            ReminderType.RECURRING -> ToggleOption(it, stringResource(R.string.recurring))
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
        }
    )
    when (reminder) {
        is Exact -> ExactReminderView(reminder, onEvent)
        is Recurring -> RecurringReminderView(reminder, onEvent)
    }

    Button(
        modifier = Modifier
            .align(CenterHorizontally)
            .padding(top = 16.dp),
        onClick = { onEvent(EditReminderEvent.SaveReminderClicked) }) {
        Text(stringResource(R.string.save_reminder))
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
    IntervalSpecificSection(reminder, onEvent)
    ReminderTimeSection(reminder, onEvent)
}

@Composable
fun IntervalSpecificSection(reminder: Recurring, onEvent: (EditReminderEvent) -> Unit) {
    when (val interval = reminder.interval) {
        Interval.Daily -> Unit
        is Interval.Weekly -> {
            DaysOfWeekSelector(
                selectedDaysOfWeek = listOf(interval.dayOfWeek),
                onSelectionChange = { onEvent(EditReminderEvent.DaysOfWeekSelected(it)) }
            )
        }

        is Interval.Monthly -> {
            var textInput by remember { mutableStateOf(interval.dayOfMonth.toString()) }
            DayOfUnitInput(stringResource(R.string.day_of_month), textInput) {
                val filteredInput = it.filter(Char::isDigit).take(2)
                textInput = filteredInput
                if (filteredInput.isNotEmpty()) {
                    onEvent(EditReminderEvent.DayOfMonthSelected(it.filter(Char::isDigit).toInt()))
                }
            }
        }

        is Interval.Yearly -> {
            var textInput by remember { mutableStateOf(interval.dayOfYear.toString()) }
            DayOfUnitInput(stringResource(R.string.day_of_year), textInput) {
                val filteredInput = it.filter(Char::isDigit).take(3)
                textInput = filteredInput
                if (filteredInput.isNotEmpty()) {
                    onEvent(EditReminderEvent.DayOfYearSelected(it.filter(Char::isDigit).toInt()))
                }
            }
        }
    }
}

@Composable
private fun DayOfUnitInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        value = value,
        label = { Text(text = label) },
        onValueChange = onValueChange,
    )
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
            positiveButton(stringResource(R.string.ok))
            negativeButton(stringResource(R.string.cancel))
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
        label = { Text(text = stringResource(R.string.time)) },
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
            positiveButton(stringResource(R.string.ok))
            negativeButton(stringResource(R.string.cancel))
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
        label = { Text(text = stringResource(R.string.date)) },
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
        ToggleOption(IntervalType.DAILY, stringResource(R.string.interval_daily)),
        ToggleOption(IntervalType.WEEKLY, stringResource(R.string.interval_weekly)),
        ToggleOption(IntervalType.MONTHLY, stringResource(R.string.interval_monthly)),
        ToggleOption(IntervalType.YEARLY, stringResource(R.string.interval_yearly)),
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
                label = { Text(text = stringResource(R.string.interval)) },
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

@Composable
fun DaysOfWeekSelector(
    selectedDaysOfWeek: List<DayOfWeek>,
    onSelectionChange: (selectedDaysOfWeek: List<DayOfWeek>) -> Unit
) {
    val options = remember(::daysOfWeekToSelectOptions)
    MultiSelectCircleRow(
        selectedDaysOfWeek,
        options,
        {
            onSelectionChange(it)
        },
        Modifier
            .padding(horizontal = 8.dp)
            .padding(top = 16.dp)
    )
}

private fun daysOfWeekToSelectOptions(): List<SelectOption<DayOfWeek>> {
    val locale = Locale.getDefault()
    return DayOfWeek.values().map {
        SelectOption(it, it.getDisplayName(TextStyle.SHORT, locale))
    }
}
