package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
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
    content: @Composable (BoxScope.(isInBound: Boolean, data: T?) -> Unit)
) {
    val dragInfo = LocalDragTargetInfo.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    var isCurrentDropTarget by remember {
        mutableStateOf(false)
    }

    val targetedItemInfo = LocalTargetedItemInfo.current

    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            isCurrentDropTarget = rect.contains(dragPosition + dragOffset)
            if (isCurrentDropTarget) {
                targetedItemInfo.targetedItemSet = targetedItemInfo.targetedItemSet.plus(key)
                targetedItemInfo.targetedItemPosition =
                    it.positionInRoot().plus(Offset.Zero.copy(y = it.size.height.toFloat()))
            } else {
                targetedItemInfo.targetedItemSet = targetedItemInfo.targetedItemSet.minus(key)
            }
        }
    }) {
        val data =
            if (isCurrentDropTarget && !dragInfo.isDragging && dragInfo.dataToDrop != key) {
                dragInfo.dataToDrop as T?
            } else {
                null
            }
        content(isCurrentDropTarget, data)
    }
}