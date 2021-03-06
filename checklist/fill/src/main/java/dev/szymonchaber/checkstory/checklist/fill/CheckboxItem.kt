package dev.szymonchaber.checkstory.checklist.fill

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox

@Composable
fun CheckboxItem(modifier: Modifier = Modifier, checkbox: Checkbox, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier.padding(start = 8.dp, end = 8.dp)) {
        Checkbox(
            modifier = Modifier.align(Alignment.CenterVertically),
            checked = checkbox.isChecked,
            onCheckedChange = onCheckedChange
        )
        Text(modifier = Modifier.align(Alignment.CenterVertically), text = checkbox.title)
    }
}
