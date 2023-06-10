package dev.szymonchaber.checkstory.checklist.template.views

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.detectDragHandleReorder
import dev.szymonchaber.checkstory.design.R
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@Composable
fun TaskView(
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String? = null,
    isFunctional: Boolean = true,
    focusRequester: FocusRequester,
    onTitleChange: (String) -> Unit,
    onAddSubtask: () -> Unit,
    onDeleteClick: () -> Unit,
    acceptChildren: Boolean
) {
    var showActualValue by remember {
        mutableStateOf(false)
    }
    var placeholderCharactersDisplayed by remember(placeholder) {
        mutableStateOf(placeholder?.count() ?: 0)
    }
    val animatedCharacterCount by animateIntAsState(
        targetValue = placeholderCharactersDisplayed,
        animationSpec = tween(
            durationMillis = pleasantCharacterRemovalAnimationDurationMillis * (placeholder?.length ?: 1)
        )
    ) {
        if (it == 0) {
            showActualValue = true
        }
    }
    val textValue = if (showActualValue || title.isNotEmpty()) {
        title
    } else {
        placeholder?.take(animatedCharacterCount) ?: ""
    }
    Row(
        Modifier
            .background(MaterialTheme.colors.surface)
            .then(modifier)
    ) {
        DragHandle(
            Modifier
                .detectDragHandleReorder()
                .align(Alignment.CenterVertically)
        )
        OutlinedTextField(
            modifier = Modifier
                .padding(start = 4.dp)
                .focusRequester(focusRequester = focusRequester)
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        placeholderCharactersDisplayed = 0
                    }
                },
            keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
            value = textValue,
            onValueChange = onTitleChange,
            label = { Text(text = stringResource(R.string.task_name)) },
            placeholder = placeholder?.let {
                { Text(text = it) }
            },
            enabled = isFunctional,
            trailingIcon = {
                if (isFunctional) {
                    Row {
                        if (acceptChildren) {
                            IconButton(onClick = { onAddSubtask() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_add_subtask),
                                    null,
                                )
                            }
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Filled.Delete, "")
                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskPreview() {
    rememberReorderableLazyListState({ _, _ -> })
    TaskView(
        modifier = Modifier,
        title = "Checkbox",
        placeholder = null,
        focusRequester = remember {
            FocusRequester()
        },
        onTitleChange = { },
        onAddSubtask = {},
        acceptChildren = false,
        onDeleteClick = {}
    )
}
