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
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.ViewTemplateCheckboxKey
import dev.szymonchaber.checkstory.common.extensions.let
import timber.log.Timber

@Composable
fun DropTarget(
    modifier: Modifier,
    onDataDropped: (ViewTemplateCheckboxKey) -> Unit,
    content: @Composable (BoxScope.() -> Unit) = {},
    placeTargetLineOnTop: Boolean = false,
    key: ViewTemplateCheckboxKey? = null
) {
    val dragInfo = LocalDragDropState.current
    val dragPosition = dragInfo.dragPosition
    val dragOffset = dragInfo.dragOffset
    val density = LocalDensity.current

    fun canReceive(viewTemplateCheckboxKey: ViewTemplateCheckboxKey?): Boolean {

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
        } ?: true
    }

    Box(modifier = modifier.onGloballyPositioned {
        it.boundsInWindow().let { rect ->
            val isCurrentDropTarget =
                rect.contains(dragPosition + dragOffset + Offset(0f, density.run { 96.dp.toPx() }))
            if (isCurrentDropTarget && canReceive(dragInfo.dataToDrop)) {
                dragInfo.currentDropTarget = onDataDropped
                val yOffset = if (placeTargetLineOnTop) 0f else it.size.height.toFloat()
                dragInfo.currentDropTargetPosition = it.positionInRoot().plus(Offset(x = 0f, y = yOffset))
            }
        }
    }, content = content)
}
