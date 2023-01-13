package dev.szymonchaber.checkstory.checklist.template.views

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.R
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@Composable
fun ParentCheckboxItem(
    modifier: Modifier = Modifier,
    checkbox: ViewTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit,
    state: ReorderableLazyListState,
    isDragging: Boolean,
    collapseChildren: Boolean
) {
    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
    Column(
        modifier = modifier
            .animateContentSize()
            .shadow(elevation.value)
            .background(MaterialTheme.colors.surface)
    ) {
        Row {
            Icon(
                modifier = Modifier
                    .detectReorder(state)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.drag_indicator),
                contentDescription = null
            )
            CheckboxItem(
                modifier = Modifier.padding(start = 8.dp),
                title = checkbox.title,
                checkbox is ViewTemplateCheckbox.New,
                onTitleChange = {
                    eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
                }
            ) {
                eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
            }
        }
        if (!collapseChildren) {
//            ChildrenCheckboxes(checkbox, eventCollector)
        }
    }
}

@Composable
private fun ChildrenCheckboxes(
    checkbox: ViewTemplateCheckbox,
    eventCollector: (EditTemplateEvent) -> Unit
) {
    checkbox.children.forEach { child ->
        CheckboxItem(
            modifier = Modifier.padding(start = 48.dp, top = 8.dp, end = 0.dp),
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
        modifier = Modifier.padding(start = 36.dp, end = 16.dp),
        onClick = {
            eventCollector(EditTemplateEvent.ChildItemAdded(checkbox))
        },
        text = text
    )
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
                listOf()
            )
        )
    )
    ParentCheckboxItem(
        checkbox = checkbox,
        eventCollector = {},
        state = rememberReorderableLazyListState(onMove = { _, _ -> }),
        isDragging = false,
        collapseChildren = false
    )
}
