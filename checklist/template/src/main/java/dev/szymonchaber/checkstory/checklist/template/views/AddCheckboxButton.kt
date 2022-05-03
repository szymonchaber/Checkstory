package dev.szymonchaber.checkstory.checklist.template.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.szymonchaber.checkstory.checklist.template.R

@Composable
fun AddCheckboxButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val text = stringResource(R.string.new_checkbox)
    AddButton(modifier, onClick, text)
}

@Composable
fun AddButton(modifier: Modifier, onClick: () -> Unit, text: String) {
    Row(modifier.clickable(onClick = onClick)) {
        IconButton(onClick) {
            Icon(Icons.Filled.Add, null)
        }
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = text
        )
    }
}
