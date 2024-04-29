package dev.szymonchaber.checkstory.checklist.template.reoder

import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import dev.szymonchaber.checkstory.checklist.template.DragSource
import dev.szymonchaber.checkstory.checklist.template.fastFirstOrNull

fun Modifier.detectLazyListReorder(): Modifier {
    return composed {
        val dragDropState = LocalDragDropState.current

        pointerInput(Unit) {
            forEachGesture {
                val dragStart = dragDropState.interactions.receive()
                val down = awaitPointerEventScope {
                    currentEvent.changes.fastFirstOrNull { it.id == dragStart.id }
                }
                if (down != null) {
                    dragDropState.onDragStart(
                        down.position,
                        DragSource(dragStart.handlePosition, dragStart.handleSize)
                    )
                    dragDropState.onDrag(dragStart.offset)
                    detectDrag(
                        down.id,
                        onDragEnd = {
                            dragDropState.onDragInterrupt()
                        },
                        onDragCancel = {
                            dragDropState.onDragInterrupt()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragDropState.onDrag(dragAmount)
                        })
                }
            }
        }
    }
}
