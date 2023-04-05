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
import dev.szymonchaber.checkstory.checklist.template.ViewTemplateCheckboxKey
import dev.szymonchaber.checkstory.common.extensions.let
import timber.log.Timber

@Composable
fun DropTarget(
    modifier: Modifier,
    onDataDropped: (ViewTemplateCheckboxKey) -> Unit,
    content: @Composable (BoxScope.() -> Unit) = {},
    placeTargetLineOnTop: Boolean = false,
    key: ViewTemplateCheckboxKey? = null,
    dropTargetOffset: Dp = 0.dp
) {
    val dragInfo = LocalDragDropState.current
    val dragPosition = dragInfo.initialDragPosition
    val dragOffset = dragInfo.dragOffset
    val density = LocalDensity.current

    fun canReceive(viewTemplateCheckboxKey: ViewTemplateCheckboxKey?): Boolean {
        if (key == null) {
            return true
        }

        return let(key, viewTemplateCheckboxKey) { current, other ->
            Timber.d(
                """
                Can receive based on comparison: ${current != other}
                Can receive based on parent: ${!other.hasKeyInAncestors(current)}
                This key: $key
                checkedKey: $viewTemplateCheckboxKey
            """.trimIndent()
            )
            current != other && !current.hasKeyInAncestors(other)
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
