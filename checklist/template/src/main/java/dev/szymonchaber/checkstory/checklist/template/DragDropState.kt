package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class DragDropState(val lazyListState: LazyListState) {

    val interactions = Channel<StartDrag>()

    // region mine
    var isDragging by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var checkboxViewId by mutableStateOf<ViewTemplateCheckboxId?>(null)
    var dataToDrop by mutableStateOf<ViewTemplateCheckboxKey?>(null)

    var currentDropTarget: ((ViewTemplateCheckboxKey) -> Unit)? by mutableStateOf(null)
    var currentDropTargetPosition: Offset? by mutableStateOf(null)

    // endregion

    var draggedDistance by mutableStateOf(0f)

    var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    var overscrollJob by mutableStateOf<Job?>(null)

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.takeUnless { it.key !is ViewTemplateCheckboxId }
            ?.also { itemInfo ->
                currentIndexOfDraggedItem = itemInfo.index
                initiallyDraggedElement = itemInfo
                dragPosition = Offset(0f, itemInfo.offset.toFloat())
                isDragging = true
                checkboxViewId = itemInfo.key as? ViewTemplateCheckboxId
            }
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        overscrollJob?.cancel()
        isDragging = false
        dragOffset = Offset.Zero
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y
        dragOffset += offset
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance

            when {
                draggedDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}

fun Modifier.detectReorder(): Modifier {
    return this.composed {
        val state = LocalDragDropState.current
        Modifier.pointerInput(Unit) {
            forEachGesture {
                awaitPointerEventScope {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var drag: PointerInputChange?
                    var overSlop = Offset.Zero
                    do {
                        drag = awaitPointerSlopOrCancellation(down.id, down.type) { change, over ->
                            change.consume()
                            overSlop = over
                        }
                    } while (drag != null && !drag.isConsumed)
                    if (drag != null) {
                        state.interactions.trySend(StartDrag(down.id, overSlop))
                    }
                }
            }
        }
    }
}

internal suspend fun AwaitPointerEventScope.awaitPointerSlopOrCancellation(
    pointerId: PointerId,
    pointerType: PointerType,
    onPointerSlopReached: (change: PointerInputChange, overSlop: Offset) -> Unit
): PointerInputChange? {
    if (currentEvent.isPointerUp(pointerId)) {
        return null // The pointer has already been lifted, so the gesture is canceled
    }
    var offset = Offset.Zero
    val touchSlop = viewConfiguration.pointerSlop(pointerType)

    var pointer = pointerId

    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        if (dragEvent.isConsumed) {
            return null
        } else if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) {
                // This is the last "up"
                return null
            } else {
                pointer = otherDown.id
            }
        } else {
            offset += dragEvent.positionChange()
            val distance = offset.getDistance()
            var acceptedDrag = false
            if (distance >= touchSlop) {
                val touchSlopOffset = offset / distance * touchSlop
                onPointerSlopReached(dragEvent, offset - touchSlopOffset)
                if (dragEvent.isConsumed) {
                    acceptedDrag = true
                } else {
                    offset = Offset.Zero
                }
            }

            if (acceptedDrag) {
                return dragEvent
            } else {
                awaitPointerEvent(PointerEventPass.Final)
                if (dragEvent.isConsumed) {
                    return null
                }
            }
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(item)
    }
}

data class StartDrag(val id: PointerId, val offset: Offset? = null)

private fun PointerEvent.isPointerUp(pointerId: PointerId): Boolean =
    changes.fastFirstOrNull { it.id == pointerId }?.pressed != true

@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastFirstOrNull(predicate: (T) -> Boolean): T? {
    contract { callsInPlace(predicate) }
    fastForEach { if (predicate(it)) return it }
    return null
}

private fun ViewConfiguration.pointerSlop(pointerType: PointerType): Float {
    return when (pointerType) {
        PointerType.Mouse -> touchSlop * mouseToTouchSlopRatio
        else -> touchSlop
    }
}

// This value was determined using experiments and common sense.
// We can't use zero slop, because some hypothetical desktop/mobile devices can send
// pointer events with a very high precision (but I haven't encountered any that send
// events with less than 1px precision)
private val mouseSlop = 0.125.dp
private val defaultTouchSlop = 18.dp // The default touch slop on Android devices
private val mouseToTouchSlopRatio = mouseSlop / defaultTouchSlop
