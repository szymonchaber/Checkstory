package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.checklist.template.views.AddCheckboxButton
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

@Composable
fun ParentCheckboxItem(
    modifier: Modifier = Modifier,
    checkbox: ViewTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    Column {
        CheckboxItem(
            modifier = modifier,
            checkbox.title,
            onTitleChange = {
                eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
            }
        ) {
            eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
        }
        checkbox.children.forEach { child ->
            CheckboxItem(
                Modifier.padding(start = 32.dp, top = 8.dp, end = 16.dp),
                child.title,
                {
                    eventCollector(EditTemplateEvent.ChildItemTitleChanged(checkbox, child, it))
                }
            ) {
                eventCollector(EditTemplateEvent.ChildItemDeleted(checkbox, child))
            }
        }
        AddCheckboxButton(modifier = Modifier.padding(start = 32.dp, top = 4.dp, end = 16.dp), onClick = {
            eventCollector(EditTemplateEvent.ChildItemAdded(checkbox))
        })
    }
}

@Composable
private fun CheckboxItem(
    modifier: Modifier,
    title: String,
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
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
            value = title,
            onValueChange = onTitleChange,
            singleLine = true,
            label = { Text(text = stringResource(R.string.name)) },
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
    val checkbox = ViewTemplateCheckbox.Existing(
        TemplateCheckboxId(0),
        null,
        "Checkbox 1",
        listOf()
    )
    ParentCheckboxItem(checkbox = checkbox, eventCollector = {})
}
