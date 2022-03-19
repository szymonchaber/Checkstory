package dev.szymonchaber.checkstory.checklist.fill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.szymonchaber.checkstory.checklist.fill.model.ChecklistLoadingState
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistEvent
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistState
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistViewModel
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.checklist

@Composable
fun FillChecklistScreen(
    fillChecklistViewModel: FillChecklistViewModel,
    navController: NavHostController
) {
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
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxHeight()
    ) {
        Text(
            modifier = Modifier.padding(start = 24.dp, top = 24.dp),
            text = checklist.title,
            style = MaterialTheme.typography.h4
        )
        Text(modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp), text = checklist.description)
        Text(
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
            style = MaterialTheme.typography.caption,
            text = "Items",
        )
        checklist.items.forEach {
            CheckboxItem(it, eventCollector)
        }
        Text(
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
            style = MaterialTheme.typography.caption,
            text = "Notes",
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 96.dp),
            value = checklist.notes, onValueChange = {
                eventCollector(FillChecklistEvent.NotesChanged(it))
            }
        )
    }
}

@Composable
fun CheckboxItem(checkbox: Checkbox, eventCollector: (FillChecklistEvent) -> Unit) {
    Row(Modifier.padding(start = 16.dp, end = 16.dp)) {
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
