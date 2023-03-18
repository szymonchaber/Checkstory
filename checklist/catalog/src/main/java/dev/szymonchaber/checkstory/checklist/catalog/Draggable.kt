package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun Draggable(
    dataToDrop: Int,
    modifier: Modifier = Modifier,
    onDragStart: (Int) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    val currentState = LocalDragDropState.current

    Box(
        modifier = modifier
            .onGloballyPositioned {
                currentPosition = it.localToWindow(Offset.Zero)
        }
    ) {
        val dragHandleModifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    currentState.dataToDrop = dataToDrop
                    onDragStart(dataToDrop)
                    currentState.isDragging = true
                    currentState.dragPosition = currentPosition + it
                    currentState.draggableComposable = content
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
