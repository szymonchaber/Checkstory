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
import timber.log.Timber

@Composable
fun DropTarget(
    modifier: Modifier,
    onDataDropped: (Int) -> Unit,
    content: @Composable (BoxScope.() -> Unit) = {}
) {
    val dragInfo = LocalDragDropState.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
//    var isCurrentDropTarget by remember {
//        mutableStateOf(false)
//    }

//    val data = if (!dragInfo.isDragging && dragInfo.dataToDrop != key) {
//        dragInfo.dataToDrop
//    } else {
//        null
//    }
//    LaunchedEffect(key1 = data) {
//        data?.let {
//            dragInfo.dataToDrop = null
////            onDataDropped(it)
//            dragInfo.currentDropTargetSet.firstOrNull()?.invoke(it)
//        }
//    }
    val density = LocalDensity.current

    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            val isCurrentDropTarget =
                rect.contains(dragPosition + dragOffset + Offset(0f, density.run { 64.dp.toPx() }))
//            Timber.d("Is $debugTag current drop target: $isCurrentDropTarget")
            if (isCurrentDropTarget) {
                dragInfo.currentDropTarget = onDataDropped
                Timber.d("Setting drop target")
                dragInfo.currentDropTargetPosition =
                    it.positionInRoot().plus(Offset.Zero.copy(y = it.size.height.toFloat()))
            } else {
//                dragInfo.currentDropTargetSet = dragInfo.currentDropTargetSet.minus(onDataDropped)
            }
        }
    }, content = content)
}
