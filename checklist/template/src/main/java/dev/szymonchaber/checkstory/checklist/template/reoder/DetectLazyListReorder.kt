package dev.szymonchaber.checkstory.checklist.template.reoder

import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import dev.szymonchaber.checkstory.checklist.template.fastFirstOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Modifier.detectLazyListReorder(): Modifier {
    return composed {
        val scope = rememberCoroutineScope()
        var overscrollJob by remember { mutableStateOf<Job?>(null) }
        val dragDropState = LocalDragDropState.current

        pointerInput(Unit) {
            forEachGesture {
                val dragStart = dragDropState.interactions.receive()
                val down = awaitPointerEventScope {
                    currentEvent.changes.fastFirstOrNull { it.id == dragStart.id }
                }
                if (down != null) {
                    dragDropState.onDragStart(down.position)
                    dragStart.offset?.apply {
                        dragDropState.onDrag(this)
                    }
                    detectDrag(
                        down.id,
                        onDragEnd = {
                            dragDropState.onDragInterrupted()
                        },
                        onDragCancel = {
                            dragDropState.onDragInterrupted()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragDropState.onDrag(dragAmount)

                            if (overscrollJob?.isActive == true) {
                                return@detectDrag
                            }

                            dragDropState
                                .checkForOverScroll()
                                .takeIf { it != 0f }
                                ?.let {
                                    overscrollJob =
                                        scope.launch { dragDropState.lazyListState.scrollBy(it) }
                                }
                                ?: run { overscrollJob?.cancel() }
                        })
                }
            }
        }
    }
}
