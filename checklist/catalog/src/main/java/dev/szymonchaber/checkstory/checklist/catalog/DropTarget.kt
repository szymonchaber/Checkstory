package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun DropTarget(
    modifier: Modifier,
    onDataDropped: (Int) -> Unit,
    content: @Composable (BoxScope.() -> Unit) = {},
    placeTargetLineOnTop: Boolean = false
) {
    val dragInfo = LocalDragDropState.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    val density = LocalDensity.current

    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            val isCurrentDropTarget =
                rect.contains(dragPosition + dragOffset + Offset(0f, density.run { 64.dp.toPx() }))
            if (isCurrentDropTarget) {
                dragInfo.currentDropTarget = onDataDropped
                val yOffset = if (placeTargetLineOnTop) 0f else it.size.height.toFloat()
                dragInfo.currentDropTargetPosition = it.positionInRoot().plus(Offset(x = 0f, y = yOffset))
            }
        }
    }, content = content)
}
