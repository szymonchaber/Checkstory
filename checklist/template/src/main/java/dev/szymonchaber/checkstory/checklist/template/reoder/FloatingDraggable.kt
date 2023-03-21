package dev.szymonchaber.checkstory.checklist.template.reoder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.model.TemplateLoadingState
import dev.szymonchaber.checkstory.checklist.template.viewId
import dev.szymonchaber.checkstory.checklist.template.viewKey
import dev.szymonchaber.checkstory.checklist.template.views.CheckboxItem

@Composable
fun FloatingDraggable(success: TemplateLoadingState.Success) {
    val dragDropState = LocalDragDropState.current
    if (dragDropState.isDragging) {
        var targetSize by remember {
            mutableStateOf(IntSize.Zero)
        }
        Box(
            modifier = Modifier
                .graphicsLayer {
                    val offset = (dragDropState.initialDragPosition?.plus(dragDropState.dragOffset))
                    alpha = if (targetSize == IntSize.Zero) 0f else .9f
                    offset?.let {
                        translationX = it.x//.minus(12.dp.toPx())
                        translationY = it.y
                    }
//                            translationY = offset.y//.minus(targetSize.height * 2 + 0.dp.toPx())
//                                dragDropState.dragPosition.y + (dragDropListStateMine.elementDisplacement ?: 0f)
                }
                .onGloballyPositioned {
                    targetSize = it.size
                }
        ) {
            val task by remember(success.unwrappedCheckboxes, dragDropState.checkboxViewId) {
                derivedStateOf {
                    success.unwrappedCheckboxes
                        .find {
                            it.first.viewId == dragDropState.checkboxViewId
                        }
                }
            }
            task?.let { (foundTask, _) ->
                LaunchedEffect(key1 = foundTask) {
                    dragDropState.dataToDrop = foundTask.viewKey
                }
                CheckboxItem(
                    title = foundTask.title,
                    placeholder = foundTask.placeholderTitle,
                    isFunctional = false,
                    focusRequester = remember { FocusRequester() },
                    onTitleChange = {},
                    onAddSubtask = {},
                    onDeleteClick = {},
                    acceptChildren = false
                )
            } ?: run {
                Text(modifier = Modifier.size(width = 150.dp, height = 30.dp), text = "I'm a new task!")
            }
        }
    }
}