package dev.szymonchaber.checkstory.checklist.template.views

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.R
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@Composable
fun CheckboxItem(
    modifier: Modifier,
    state: ReorderableLazyListState,
    isDragging: Boolean,
    title: String,
    isNew: Boolean,
    onTitleChange: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation.value)
            .background(MaterialTheme.colors.surface)
            .then(modifier)
    ) {
        DragHandle(state)
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
                .let {
                    if (isNew) {
                        it
//                            .focusOnEntry(true) // TODO does not work
                    } else {
                        it
                    }
                },
            keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
            value = title,
            onValueChange = onTitleChange,
            label = { Text(text = stringResource(R.string.task_name)) },
            trailingIcon = {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, "")
                }
            }
        )
    }
}

@Composable
private fun RowScope.DragHandle(state: ReorderableLazyListState) {
    Icon(
        modifier = Modifier
            .detectReorder(state)
            .align(Alignment.CenterVertically),
        painter = painterResource(id = R.drawable.drag_indicator),
        tint = Color.Gray,
        contentDescription = null
    )
}

@Preview(showBackground = true)
@Composable
fun CheckboxItemPreview() {
    rememberReorderableLazyListState({ _, _ -> })
    CheckboxItem(
        modifier = Modifier,
        state = rememberReorderableLazyListState({ _, _ -> }),
        isDragging = false,
        title = "Checkbox",
        isNew = true,
        onTitleChange = { }
    ) { }
}
