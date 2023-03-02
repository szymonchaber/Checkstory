package dev.szymonchaber.checkstory.checklist.template.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import dev.szymonchaber.checkstory.checklist.template.R
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

@Composable
fun CheckboxItem(
    modifier: Modifier,
    title: String,
    placeholder: String? = null,
    nestingLevel: Int,
    focusRequester: FocusRequester,
    onTitleChange: (String) -> Unit,
    onAddSubtask: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .then(modifier)
    ) {
        var hasFocus by remember {
            mutableStateOf(false)
        }
        val textValue: String = if (hasFocus || title.isNotEmpty()) {
            title
        } else {
            placeholder ?: ""
        }
        OutlinedTextField(
            modifier = Modifier
                .focusRequester(focusRequester = focusRequester)
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .onFocusChanged { focusState ->
                    hasFocus = focusState.isFocused
                },
            keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
            value = textValue,
            onValueChange = onTitleChange,
            label = { Text(text = stringResource(R.string.task_name)) },
            placeholder = placeholder?.let {
                { Text(text = it) }
            },
            trailingIcon = {
                Row {
                    if (nestingLevel < 4) {
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
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CheckboxItemPreview() {
    rememberReorderableLazyListState({ _, _ -> })
    CheckboxItem(
        modifier = Modifier,
        title = "Checkbox",
        placeholder = null,
        nestingLevel = 4,
        focusRequester = remember {
            FocusRequester()
        },
        onTitleChange = { },
        onAddSubtask = {}
    ) {

    }
}
