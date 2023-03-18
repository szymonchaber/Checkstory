package dev.szymonchaber.checkstory.checklist.template

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import dev.szymonchaber.checkstory.checklist.template.model.ViewTemplateCheckbox
import dev.szymonchaber.checkstory.checklist.template.views.NewCheckboxItem
import kotlinx.coroutines.Job

class DragDropState(
    val getCurrentTasks: () -> List<Pair<ViewTemplateCheckbox, Int>>,
    val lazyListState: LazyListState,
) {

    // region mine
    var isDragging by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable (Modifier) -> Unit)?>(null)
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
            ?.takeUnless { it.key !is ViewTemplateCheckboxKey }
            ?.also { itemInfo ->
                currentIndexOfDraggedItem = itemInfo.index
                initiallyDraggedElement = itemInfo
                dragPosition = Offset(0f, itemInfo.offset.toFloat())
                isDragging = true
                dataToDrop = itemInfo.key as ViewTemplateCheckboxKey
                draggableComposable = { _ ->
                    val (task, nestingLevel) = getCurrentTasks().firstOrNull { it.first.viewKey == itemInfo.key }!!
                    val focusRequester = remember { FocusRequester() }
                    NewCheckboxItem(
                        title = task.title,
                        placeholder = task.placeholderTitle,
                        isFunctional = false,
                        focusRequester = focusRequester,
                        onTitleChange = {},
                        onAddSubtask = {},
                        onDeleteClick = {},
                        acceptChildren = nestingLevel < 3
                    )
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
