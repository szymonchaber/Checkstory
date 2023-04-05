package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.reoder.LocalDragDropState
import dev.szymonchaber.checkstory.checklist.template.views.DragHandle

@Composable
fun NewTask(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp),
    ) {
        Row(Modifier.height(36.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                textAlign = TextAlign.Center,
                text = "New task",
                style = MaterialTheme.typography.button,
            )
            DragHandle(Modifier.align(Alignment.CenterVertically))
        }
    }
}

@Composable
fun Draggable(
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    val currentState = LocalDragDropState.current
    var currentSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    Box(
        modifier = modifier
            .onGloballyPositioned {
                currentSize = it.size
                currentPosition = it.localToWindow(Offset.Zero)
            }
    ) {
        val dragHandleModifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    currentState.onDragStart(
                        it,
                        DragSource.NewTaskDraggable(
                            currentPosition.copy(y = currentPosition.y - currentSize.height.toFloat() * 2),
                            currentSize
                        )
                    )
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    currentState.onDrag(dragAmount)
                },
                onDragEnd = {
                    currentState.onDragInterrupt()
                },
                onDragCancel = {
                    currentState.onDragInterrupt()
                }
            )
        }
        content(dragHandleModifier)
    }
}
