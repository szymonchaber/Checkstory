package dev.szymonchaber.checkstory.checklist.template.reoder

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DropTargetIndicatorLine() {
    val state = LocalDragDropState.current

    val offsetY = LocalDensity.current.toPx(48.dp) // TODO more debug lines still?
    val targetValue = state.currentDropTargetInfo?.offset?.minus(Offset(x = 0f, y = offsetY)) ?: Offset.Zero

    val shouldSnap = remember(state.previousDropTargetInfo) {
        state.previousDropTargetInfo == null
    }
    val offset by animateOffsetAsState(
        targetValue = targetValue,
        animationSpec = if (shouldSnap) {
            snap()
        } else {
            spring()
        }
    )
    if (state.isDragging) {
        val colors = MaterialTheme.colors
        Canvas(
            modifier = Modifier
                .padding(end = 16.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = offset.y
                }
        ) {
            drawLine(
                color = colors.primary,
                start = offset.copy(y = 0f),
                end = offset.copy(x = this.size.width, y = 0f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

private fun Density.toPx(dp: Dp): Float {
    return dp.toPx()
}
