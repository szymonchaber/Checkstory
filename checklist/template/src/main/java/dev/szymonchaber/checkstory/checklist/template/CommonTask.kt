package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.model.EditTemplateEvent
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateTask
import dev.szymonchaber.checkstory.checklist.template.reoder.DropTarget
import dev.szymonchaber.checkstory.checklist.template.views.TaskView
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId

@Composable
fun CommonTask(
    task: ViewTemplateTask,
    paddingStart: Dp,
    nestingLevel: Int,
    eventCollector: (EditTemplateEvent) -> Unit,
) {
    val taskTopPadding = 12.dp
    val focusRequester = remember { FocusRequester() }
    Box {
        val acceptChildren = remember(nestingLevel) {
            nestingLevel < 3
        }
        val localDensity = LocalDensity.current
        var columnHeightDp by remember {
            mutableStateOf(0.dp)
        }
        TaskView(
            modifier = Modifier
                .onGloballyPositioned {
                    columnHeightDp = with(localDensity) { it.size.height.toDp() }
                }
                .padding(top = taskTopPadding, start = paddingStart + 16.dp, end = 16.dp),
            title = task.title,
            placeholder = task.placeholderTitle,
            focusRequester = focusRequester,
            onTitleChange = {
                eventCollector(EditTemplateEvent.TaskTitleChanged(task, it))
            },
            onAddSubtask = {
                eventCollector(EditTemplateEvent.ChildTaskAdded(task.id))
            },
            onDeleteClick = {
                eventCollector(EditTemplateEvent.TaskRemoved(task))
            },
            acceptChildren = acceptChildren
        )
        Receptacles(
            forTask = task.id,
            modifier = Modifier
                .height(columnHeightDp),
            acceptChildren = acceptChildren,
            dropTargetOffset = paddingStart + 16.dp,
            eventCollector = eventCollector
        )
    }
    val recentlyAddedItem = LocalRecentlyAddedUnconsumedItem.current
    LaunchedEffect(recentlyAddedItem.item) {
        if (task.id == recentlyAddedItem.item) {
            focusRequester.requestFocus()
            recentlyAddedItem.item = null
        }
    }
}

@Composable
private fun Receptacles(
    forTask: TemplateTaskId,
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
            id = forTask,
            dropTargetOffset = dropTargetOffset,
            onDataDropped = { siblingTask ->
                if (siblingTask.id == NEW_TASK_ID) {
                    eventCollector(EditTemplateEvent.NewSiblingDraggedBelow(forTask))
                } else {
                    eventCollector(EditTemplateEvent.SiblingMovedBelow(forTask, siblingTask))
                }
            }
        )
        if (acceptChildren) {
            DropTarget(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
//                    .background(Color.Yellow.copy(alpha = 0.2f)),
                id = forTask,
                onDataDropped = { childTask ->
                    if (childTask.id == NEW_TASK_ID) {
                        eventCollector(EditTemplateEvent.NewChildDraggedBelow(forTask))
                    } else {
                        eventCollector(EditTemplateEvent.ChildMovedBelow(forTask, childTask))
                    }
                }
            )
        }
    }
}
