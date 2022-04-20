package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.layout.Column
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
import dev.szymonchaber.checkstory.checklist.template.views.AddCheckboxButton
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

@Composable
fun ParentCheckboxItem(
    modifier: Modifier = Modifier,
    checkbox: EditTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    Column {
        CheckboxItem(
            modifier = modifier,
            checkbox = checkbox,
            onTitleChange = {
                eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
            },
            onDeleteClick = {
                eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
            }
        )
        checkbox.checkbox.children.forEach { child ->
            CheckboxItem(Modifier.padding(start = 32.dp, top = 8.dp),
                EditTemplateCheckbox.Existing(child),
                { eventCollector(EditTemplateEvent.ChildItemTitleChanged(checkbox, child, it)) },
                { eventCollector(EditTemplateEvent.ChildItemDeleted(checkbox, child)) }
            )
        }
        AddCheckboxButton(modifier = Modifier.padding(start = 32.dp, top = 4.dp), onClick = {}) // TODO
    }
}

@Composable
private fun CheckboxItem(
    modifier: Modifier,
    checkbox: EditTemplateCheckbox,
    onTitleChange: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth()
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
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            value = checkbox.checkbox.title,
            onValueChange = onTitleChange,
            trailingIcon = {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, "")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CheckboxItemPreview() {
    val checkbox = EditTemplateCheckbox.Existing(
        TemplateCheckbox(
            TemplateCheckboxId(0),
            null,
            "Checkbox 1",
            listOf()
        )
    )
    ParentCheckboxItem(checkbox = checkbox, eventCollector = {})
}
