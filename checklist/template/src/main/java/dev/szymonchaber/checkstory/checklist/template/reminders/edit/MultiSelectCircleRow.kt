package dev.szymonchaber.checkstory.checklist.template.reminders.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <T> MultiSelectCircleRow(
    currentSelections: List<T>,
    toggleStates: List<SelectOption<T>>,
    onSelectionChange: (List<T>) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTint = MaterialTheme.colors.primary
    val unselectedTint = Color.Gray

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
    ) {
        toggleStates.forEachIndexed { index, toggleState ->
            val isSelected = currentSelections.any { it == toggleState.tag }
            val backgroundTint = if (isSelected) selectedTint else unselectedTint
            val textColor = if (isSelected) Color.White else Color.Unspecified

            if (index != 0) {
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .then(Modifier.clip(CircleShape))
                    .background(backgroundTint)
                    .aspectRatio(1f)
                    .weight(1f)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .toggleable(
                        value = isSelected,
                        enabled = true,
                        onValueChange = { selected ->
                            if (selected) {
                                onSelectionChange(currentSelections.plus(toggleState.tag))
                            } else {
                                if (currentSelections.contains(toggleState.tag)) { // TODO Delete when empty lists are accepted
                                    return@toggleable
                                }
                                onSelectionChange(currentSelections.minus(toggleState.tag))
                            }
                        })
            ) {
                Text(
                    toggleState.text,
                    textAlign = TextAlign.Center,
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}