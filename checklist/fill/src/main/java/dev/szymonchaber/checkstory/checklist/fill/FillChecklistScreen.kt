package dev.szymonchaber.checkstory.checklist.fill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.szymonchaber.checkstory.checklist.fill.model.ChecklistLoadingState
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistEvent
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistState
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistViewModel
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.checklist

@Composable
fun FillChecklistScreen(fillChecklistViewModel: FillChecklistViewModel = viewModel()) {
    val state = fillChecklistViewModel.state.collectAsState(initial = FillChecklistState.initial)
    when (val loadingState = state.value.checklistLoadingState) {
        ChecklistLoadingState.Loading -> {
            Text(text = "Loading")
        }
        is ChecklistLoadingState.Success -> {
            FillChecklistView(loadingState.checklist, fillChecklistViewModel::onEvent)
        }
    }
}

@Composable
fun FillChecklistView(checklist: Checklist, eventCollector: (FillChecklistEvent) -> Unit) {
    Column {
        Text(modifier = Modifier.padding(start = 24.dp, top = 24.dp), text = checklist.title, style = MaterialTheme.typography.h4)
        Text(modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp), text = checklist.description)
        checklist.items.forEach {
            CheckboxItem(it, eventCollector)
        }
    }
}

@Composable
fun CheckboxItem(checkbox: Checkbox, eventCollector: (FillChecklistEvent) -> Unit) {
    Row(Modifier.padding(start = 8.dp, end = 16.dp)) {
        Checkbox(
            modifier = Modifier.align(CenterVertically),
            checked = checkbox.isChecked,
            onCheckedChange = {
                eventCollector(FillChecklistEvent.CheckChanged(checkbox, it))
            }
        )
        Text(modifier = Modifier.align(CenterVertically), text = checkbox.title)
    }
}

@Preview(showBackground = true)
@Composable
fun FillChecklistViewPreview() {
    CheckstoryTheme {
        FillChecklistView(checklist) {
            // nop
        }
    }
}
