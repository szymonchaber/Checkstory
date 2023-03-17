package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp


internal class DraggableItemInfo {
    var isDragging by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable (Modifier) -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
}

internal class DropTargetInfo {
    var currentDropTargetSet: Set<Any> by mutableStateOf(setOf())
    var currentDropTargetPosition: Offset? by mutableStateOf(null)
}

internal val LocalDraggableItemInfo = compositionLocalOf { DraggableItemInfo() }

internal val LocalDropTargetInfo = compositionLocalOf { DropTargetInfo() }

@Composable
fun LongPressDraggableOverLazyList(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(LazyListState) -> Unit
) {
    val targetState = remember { DropTargetInfo() }
    val draggableItemState = remember { DraggableItemInfo() }
    val lazyListState = rememberLazyListState()

    CompositionLocalProvider(
        LocalDraggableItemInfo provides draggableItemState,
        LocalDropTargetInfo provides targetState
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            content(lazyListState)
            if (draggableItemState.isDragging) {
                var targetSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val offset = (draggableItemState.dragPosition + draggableItemState.dragOffset)
                            alpha = if (targetSize == IntSize.Zero) 0f else .9f
                            translationX = offset.x.minus(12.dp.toPx())
                            translationY = offset.y.minus(targetSize.height * 2 + 0.dp.toPx())
                        }
                        .onGloballyPositioned {
                            targetSize = it.size
                        }
                ) {
                    draggableItemState.draggableComposable?.invoke(Modifier)
                }
            }
        }
    }
}
