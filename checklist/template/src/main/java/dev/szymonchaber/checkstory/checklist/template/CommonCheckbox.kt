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
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId

@Composable
fun CommonCheckbox(
    checkbox: ViewTemplateCheckbox,
    paddingStart: Dp,
    nestingLevel: Int,
    eventCollector: (EditTemplateEvent) -> Unit,
) {
    val taskTopPadding = 12.dp
    val focusRequester = remember { FocusRequester() }
    Box(Modifier.height(IntrinsicSize.Min)) {
        val acceptChildren = nestingLevel < 3
        CheckboxItem(
            modifier = Modifier
//                .drawFolderStructure(nestingLevel, paddingStart, taskTopPadding) TODO decide if this should stay
                .padding(top = taskTopPadding, start = paddingStart + 16.dp, end = 16.dp),
            title = checkbox.title,
            placeholder = checkbox.placeholderTitle,
            focusRequester = focusRequester,
            onTitleChange = {
                eventCollector(EditTemplateEvent.ItemTitleChanged(checkbox, it))
            },
            onAddSubtask = {
                eventCollector(EditTemplateEvent.ChildItemAdded(checkbox.id))
            },
            onDeleteClick = {
                eventCollector(EditTemplateEvent.ItemRemoved(checkbox))
            },
            acceptChildren = acceptChildren
        )
        Receptacles(
            forCheckbox = checkbox.id,
            modifier = Modifier,
            acceptChildren = acceptChildren,
            dropTargetOffset = paddingStart + 16.dp,
            eventCollector = eventCollector
        )
    }
    val recentlyAddedItem = LocalRecentlyAddedUnconsumedItem.current
    LaunchedEffect(recentlyAddedItem.item) {
        if (checkbox.id == recentlyAddedItem.item) {
            focusRequester.requestFocus()
            recentlyAddedItem.item = null
        }
    }
}

@Composable
private fun Receptacles(
    forCheckbox: TemplateCheckboxId,
    modifier: Modifier = Modifier,
    acceptChildren: Boolean,
    dropTargetOffset: Dp,
    eventCollector: (EditTemplateEvent) -> Unit,
) {
    Row(modifier.fillMaxSize()) {
        DropTarget(
            modifier = Modifier
                .fillMaxHeight()
                .then {
                    if (acceptChildren) {
                        val siblingLevelTargetSizeMinimum = 24.dp
                        width(dropTargetOffset + siblingLevelTargetSizeMinimum)
                    } else {
                        fillMaxWidth()
                    }
                },
//                .background(Color.Red.copy(alpha = 0.2f)),
            id = forCheckbox,
            dropTargetOffset = dropTargetOffset,
            onDataDropped = { siblingTask ->
                if (siblingTask.id == NEW_TASK_ID) {
                    eventCollector(EditTemplateEvent.NewSiblingDraggedBelow(forCheckbox))
                } else {
                    eventCollector(EditTemplateEvent.SiblingMovedBelow(forCheckbox, siblingTask))
                }
            }
        )
        if (acceptChildren) {
            DropTarget(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
//                    .background(Color.Yellow.copy(alpha = 0.2f)),
                id = forCheckbox,
                onDataDropped = { childTask ->
                    if (childTask.id == NEW_TASK_ID) {
                        eventCollector(EditTemplateEvent.NewChildDraggedBelow(forCheckbox))
                    } else {
                        eventCollector(EditTemplateEvent.ChildMovedBelow(forCheckbox, childTask))
                    }
                }
            )
        }
    }
}
