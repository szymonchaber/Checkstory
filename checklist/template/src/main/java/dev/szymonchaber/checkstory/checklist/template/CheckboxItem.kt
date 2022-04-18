package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

@Composable
fun CheckboxItem(
    modifier: Modifier = Modifier,
    checkbox: EditTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    Row(
        modifier
            .padding(end = 16.dp)
            .fillMaxWidth()
    ) {
        Checkbox(
            modifier = Modifier.align(Alignment.CenterVertically),
            checked = false,
            onCheckedChange = {
                // nop
            }
        )
        TextField(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            value = checkbox.checkbox.title,
            onValueChange = { eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it)) },
            trailingIcon = {
                IconButton(
                    onClick = { eventCollector(EditTemplateEvent.ItemRemoved(checkbox)) }) {
                    Icon(Icons.Filled.Delete, "")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CheckboxItemPreview() {
    val checkbox = EditTemplateCheckbox.Existing(TemplateCheckbox(TemplateCheckboxId(0), "Checkbox 1"))
    CheckboxItem(checkbox = checkbox, eventCollector = {})
}
