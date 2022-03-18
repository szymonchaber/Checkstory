package dev.szymonchaber.checkstory.checklist.fill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistState
import dev.szymonchaber.checkstory.checklist.fill.model.FillChecklistViewModel
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.checklist

@Composable
fun FillChecklistScreen(fillChecklistViewModel: FillChecklistViewModel = viewModel()) {
    val state = fillChecklistViewModel.state.collectAsState(initial = FillChecklistState(checklist))
    FillChecklistView(state.value.checklist)
}

@Composable
fun FillChecklistView(checklist: Checklist) {
    Column {
        Text(modifier = Modifier.padding(start = 16.dp), text = checklist.title)
        Text(modifier = Modifier.padding(start = 16.dp, top = 8.dp), text = checklist.description)
        checklist.items.forEach {
            CheckboxItem(it)
        }
    }
}

@Composable
fun CheckboxItem(checkbox: Checkbox) {
    Row(Modifier.padding(end = 16.dp)) {
        Checkbox(modifier = Modifier.align(CenterVertically), checked = checkbox.isChecked, onCheckedChange = {})
        Text(modifier = Modifier.align(CenterVertically), text = checkbox.title)
    }
}

@Preview(showBackground = true)
@Composable
fun FillChecklistViewPreview() {
    CheckstoryTheme {
        FillChecklistView(checklist)
    }
}
