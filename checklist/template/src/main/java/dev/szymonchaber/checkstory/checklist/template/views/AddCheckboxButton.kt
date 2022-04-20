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

@Composable
fun AddCheckboxButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clickable(onClick = onClick)) {
        IconButton({}) {
            Icon(Icons.Filled.Add, null)
        }
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = "New checkbox"
        )
    }
}
