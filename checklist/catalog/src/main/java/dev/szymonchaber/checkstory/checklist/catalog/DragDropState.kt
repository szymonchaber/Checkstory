package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.Job

class DragDropState(
    val tasksWithNestedLevel: List<Pair<Task, Int>>,
    val lazyListState: LazyListState,
) {

    // region mine
    var isDragging by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable (Modifier) -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Int?>(null)

    var currentDropTarget: ((Int) -> Unit)? by mutableStateOf(null)
    var currentDropTargetPosition: Offset? by mutableStateOf(null)

    // endregion

    var draggedDistance by mutableStateOf(0f)

    var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    var overscrollJob by mutableStateOf<Job?>(null)

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.takeUnless { it.key !is Int }
            ?.also { itemInfo ->
                currentIndexOfDraggedItem = itemInfo.index
                initiallyDraggedElement = itemInfo
                dragPosition = Offset(0f, itemInfo.offset.toFloat())
                isDragging = true
                dataToDrop = itemInfo.key as Int
                draggableComposable = { _ ->
                    TaskCardDetails(task = tasksWithNestedLevel.firstOrNull { it.first.id == itemInfo.key }?.first!!)
                }
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
