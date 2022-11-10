package dev.szymonchaber.checkstory.checklist.template.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import dev.szymonchaber.checkstory.checklist.template.R

@Composable
fun CheckboxItem(
    modifier: Modifier,
    title: String,
    isNew: Boolean,
    onTitleChange: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
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
            label = { Text(text = stringResource(R.string.name)) },
            trailingIcon = {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, "")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CheckboxItemPreview() {
    CheckboxItem(
        modifier = Modifier,
        title = "Checkbox",
        isNew = true,
        onTitleChange = { },
        onDeleteClick = { }
    )
}
