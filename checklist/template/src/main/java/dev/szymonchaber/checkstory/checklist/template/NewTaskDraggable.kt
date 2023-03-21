package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.reoder.LocalDragDropState

@Composable
fun BoxScope.NewTaskDraggable() {
    Draggable(Modifier.align(Alignment.BottomStart)) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Cyan)
                .height(50.dp)
                .padding(8.dp)
                .then(it)
        ) {
            Text(modifier = Modifier.align(Alignment.Center), text = "New task")
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
                currentPosition = it.localToWindow(Offset.Zero)
                currentSize = it.size
            }
    ) {
        val dragHandleModifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    currentState.dataToDrop = ViewTemplateCheckboxKey(-50, null, true)
                    currentState.checkboxViewId = ViewTemplateCheckboxId(-50, true)
                    currentState.isDragging = true
                    currentState.initialDragPosition = currentPosition + it
                    currentState.initialDragSize = currentSize
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    currentState.dragOffset += Offset(dragAmount.x, dragAmount.y)
                },
                onDragEnd = {
                    currentState.isDragging = false
                    currentState.dragOffset = Offset.Zero
                },
                onDragCancel = {
                    currentState.dragOffset = Offset.Zero
                    currentState.isDragging = false
                })
        }
        content(dragHandleModifier)
    }
}
