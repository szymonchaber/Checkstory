package dev.szymonchaber.checkstory.checklist.template.reminders.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <T> MultiToggleButton(
    currentSelection: T,
    toggleStates: List<ToggleOption<T>>,
    onToggleChange: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(4.dp, 0.dp, 0.dp, 4.dp)
    val selectedTint = MaterialTheme.colors.primary
    val unselectedTint = Color.LightGray

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
    ) {
        toggleStates.forEachIndexed { index, toggleState ->
            val isSelected = currentSelection == toggleState.tag
            val backgroundTint = if (isSelected) selectedTint else unselectedTint
            val textColor = if (isSelected) Color.White else Color.Unspecified

            if (index != 0) {
                Divider(
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
            }

            val clipModifier = when (index) {
                0 -> Modifier.clip(shape)
                toggleStates.lastIndex -> Modifier.clip(shape.flipHorizontal())
                else -> Modifier
            }
            Row(
                modifier = Modifier
                    .then(clipModifier)
                    .background(backgroundTint)
                    .weight(1f)
                    .padding(vertical = 8.dp, horizontal = 8.dp)
                    .toggleable(
                        value = isSelected,
                        enabled = true,
                        onValueChange = { selected ->
                            if (selected) {
                                onToggleChange(toggleState.tag)
                            }
                        })
            ) {
                Text(
                    toggleState.text,
                    textAlign = TextAlign.Center,
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }
    }
}

data class ToggleOption<T>(val tag: T, val text: String)

fun RoundedCornerShape.flipHorizontal(): RoundedCornerShape {
    return copy(
        topStart = topEnd,
        topEnd = topStart,
        bottomEnd = bottomStart,
        bottomStart = bottomEnd
    )
}
