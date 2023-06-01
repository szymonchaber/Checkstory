package dev.szymonchaber.checkstory.checklist.template.reoder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.DropTargetInfo
import dev.szymonchaber.checkstory.checklist.template.LocalIsReorderValidLookup
import dev.szymonchaber.checkstory.common.extensions.let
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId

@Composable
fun DropTarget(
    modifier: Modifier,
    onDataDropped: (TemplateTaskId) -> Unit,
    content: @Composable (BoxScope.() -> Unit) = {},
    placeTargetLineOnTop: Boolean = false,
    id: TemplateTaskId? = null,
    dropTargetOffset: Dp = 0.dp
) {
    val dragInfo = LocalDragDropState.current
    val dragPosition = dragInfo.initialDragPosition
    val dragOffset = dragInfo.dragOffset
    val density = LocalDensity.current
    val isReorderValid = LocalIsReorderValidLookup.current

    fun canReceive(subject: TemplateTaskId?): Boolean {
        if (id == null) {
            return true
        }

        return let(id, subject) { target, dragSubject ->
            target != dragSubject && isReorderValid(dragSubject, target)
        } ?: false
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val boundsInWindow = coordinates.boundsInWindow()  // TODO more debug shapes?
                dragPosition?.let {
                    with(density) {
                        val isCurrentDropTarget = boundsInWindow.contains(it + dragOffset + Offset(0f, 96.dp.toPx()))
                        if (isCurrentDropTarget && canReceive(dragInfo.dataToDrop)) {
                            val yOffset = if (placeTargetLineOnTop) {
                                0f
                            } else {
                                coordinates.size.height.toFloat()
                            }
                            val offset =
                                coordinates.positionInRoot().plus(Offset(x = dropTargetOffset.toPx(), y = yOffset))
                            dragInfo.onDropTargetAcquired(DropTargetInfo(offset, onDataDropped))
                        }
                    }
                }
            },
        content = content
    )
}
