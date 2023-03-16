package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

@Composable
fun <T> DropTarget(
    modifier: Modifier,
    key: Any,
    onDataDropped: (T) -> Unit,
    debugTag: String,
    content: @Composable (BoxScope.() -> Unit) = {}
) {
    val dragInfo = LocalDraggableItemInfo.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    var isCurrentDropTarget by remember {
        mutableStateOf(false)
    }

    val targetedItemInfo = LocalDropTargetInfo.current

    val data = if (isCurrentDropTarget && !dragInfo.isDragging && dragInfo.dataToDrop != key) {
        dragInfo.dataToDrop as T?
    } else {
        null
    }
    LaunchedEffect(key1 = data) {
        data?.let {
            dragInfo.dataToDrop = null
            onDataDropped(it)
        }
    }

    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            isCurrentDropTarget = rect.contains(dragPosition + dragOffset)
//            Timber.d("Is $debugTag current drop target: $isCurrentDropTarget")
            if (isCurrentDropTarget) {
                targetedItemInfo.currentDropTargetSet = targetedItemInfo.currentDropTargetSet.plus(key)
                targetedItemInfo.currentDropTargetPosition =
                    it.positionInRoot().plus(Offset.Zero.copy(y = it.size.height.toFloat()))
            } else {
                targetedItemInfo.currentDropTargetSet = targetedItemInfo.currentDropTargetSet.minus(key)
            }
        }
    }, content = content)
}
