package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.checklist.template.reoder.DropTarget
import dev.szymonchaber.checkstory.checklist.template.views.CheckboxItem

@Composable
fun CommonCheckbox(
    checkbox: ViewTemplateCheckbox,
    paddingStart: Dp,
    nestingLevel: Int,
    eventCollector: (EditTemplateEvent) -> Unit,
) {
    val taskTopPadding = 8.dp
    val focusRequester = remember { FocusRequester() }
    Box(Modifier.height(IntrinsicSize.Min)) {
        val acceptChildren = nestingLevel < 3
        CheckboxItem(
            modifier = Modifier
//                .drawFolderStructure(nestingLevel, paddingStart, taskTopPadding) TODO decide if this should stay
                .padding(top = taskTopPadding, start = paddingStart),
            title = checkbox.title,
            placeholder = checkbox.placeholderTitle,
            focusRequester = focusRequester,
            onTitleChange = {
                eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
            },
            onAddSubtask = {
                eventCollector(EditTemplateEvent.ChildItemAdded(checkbox.viewKey))
            },
            onDeleteClick = {
                eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
            },
            acceptChildren = acceptChildren
        )
        Receptacles(
            modifier = Modifier.padding(top = taskTopPadding, start = paddingStart),
            acceptChildren = acceptChildren,
            forCheckbox = checkbox.viewKey,
            onSiblingTaskDropped = { siblingTask ->
                eventCollector(EditTemplateEvent.SiblingMovedBelow(checkbox.viewKey, siblingTask))
            },
            onChildTaskDropped = { childTask ->
                eventCollector(EditTemplateEvent.ChildMovedBelow(checkbox.viewKey, childTask))
            }
        )
    }
    val recentlyAddedItem = LocalRecentlyAddedUnconsumedItem.current
    LaunchedEffect(recentlyAddedItem.item) {
        if (checkbox.viewKey == recentlyAddedItem.item) {
            focusRequester.requestFocus()
            recentlyAddedItem.item = null
        }
    }
}

@Composable
private fun Receptacles(
    forCheckbox: ViewTemplateCheckboxKey?,
    onSiblingTaskDropped: (ViewTemplateCheckboxKey) -> Unit,
    onChildTaskDropped: (ViewTemplateCheckboxKey) -> Unit,
    modifier: Modifier = Modifier,
    acceptChildren: Boolean,
) {
    Row(modifier.fillMaxSize()) {
        DropTarget(
            modifier = Modifier
                .fillMaxHeight()
                .then {
                    if (acceptChildren) {
                        width(24.dp)
                    } else {
                        fillMaxWidth()
                    }
                },
            key = forCheckbox,
            onDataDropped = { siblingTask ->
                onSiblingTaskDropped(siblingTask)
            }
        )
        if (acceptChildren) {
            DropTarget(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                key = forCheckbox,
                onDataDropped = { childTask ->
                    onChildTaskDropped(childTask)
                }
            )
        }
    }
}
