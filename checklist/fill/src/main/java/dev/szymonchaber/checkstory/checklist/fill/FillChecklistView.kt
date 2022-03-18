package dev.szymonchaber.checkstory.checklist.fill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.design.theme.CheckstoryTheme

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
        FillChecklistView(
            Checklist(
                "Cleaning living room",
                "I love to have a clean living room, but tend to forget about some hard-to-reach places",
                listOf(
                    Checkbox("Table", true),
                    Checkbox("Desk", true),
                    Checkbox("Floor covers", false)
                )
            )
        )
    }
}

class Checklist(val title: String, val description: String, val items: List<Checkbox>)

class Checkbox(val title: String, val isChecked: Boolean)
