package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.checklist.template.views.AddButton
import dev.szymonchaber.checkstory.checklist.template.views.CheckboxItem
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
            title = checkbox.title,
            checkbox is ViewTemplateCheckbox.New,
            onTitleChange = {
                eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
            }
        ) {
            eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
        }
        checkbox.children.forEach { child ->
            CheckboxItem(
                modifier = Modifier.padding(start = 32.dp, top = 8.dp, end = 16.dp),
                title = child.title,
                child is ViewTemplateCheckbox.New,
                onTitleChange = {
                    eventCollector(EditTemplateEvent.ChildItemTitleChanged(checkbox, child, it))
                }
            ) {
                eventCollector(EditTemplateEvent.ChildItemDeleted(checkbox, child))
            }
        }
        val text = stringResource(R.string.new_child_checkbox)
        AddButton(
            modifier = Modifier.padding(start = 20.dp, end = 16.dp),
            onClick = {
                eventCollector(EditTemplateEvent.ChildItemAdded(checkbox))
            },
            text = text
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ParentCheckboxItemPreview() {
    val checkbox = ViewTemplateCheckbox.Existing(
        TemplateCheckboxId(0),
        null,
        "Checkbox 1",
        listOf(
            ViewTemplateCheckbox.Existing(
                TemplateCheckboxId(0),
                null,
                "Checkbox 1",
                listOf(),
                0
            )
        ),
        0
    )
    ParentCheckboxItem(checkbox = checkbox, eventCollector = {})
}
