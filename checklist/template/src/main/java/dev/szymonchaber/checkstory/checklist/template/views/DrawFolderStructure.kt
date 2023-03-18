package dev.szymonchaber.checkstory.checklist.template.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.nestedPaddingStart

@Composable
fun Modifier.drawFolderStructure(
    nestingLevel: Int,
    isLastChild: Boolean,
    paddingStart: Dp,
    taskTopPadding: Dp
): Modifier = drawBehind { // TODO check drawWithContent or withCache
    if (nestingLevel > 0) {
        val heightFraction = if (!isLastChild) 1f else 0.5f
        val halfOfGlobalNesting = nestedPaddingStart.toPx() / 2
        drawLine(
            color = Color.Gray,
            start = Offset(x = paddingStart.toPx() - halfOfGlobalNesting, y = 0f),
            end = Offset(
                x = paddingStart.toPx() - halfOfGlobalNesting,
                y = size.height * heightFraction + taskTopPadding.toPx() / 2
            ),
            strokeWidth = 2.dp.toPx()
        )
        val visualCenterY = center.y + taskTopPadding.toPx() / 2
        drawLine(
            color = Color.Gray,
            start = Offset(x = paddingStart.toPx() - halfOfGlobalNesting, y = visualCenterY),
            end = Offset(x = paddingStart.toPx(), y = visualCenterY),
            strokeWidth = 2.dp.toPx()
        )
    }
}
