package dev.szymonchaber.checkstory.checklist.template.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddButton(modifier: Modifier, onClick: () -> Unit, text: String) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Icon(
            Icons.Filled.Add,
            null,
            Modifier
                .padding(vertical = 12.dp)
                .padding(start = 12.dp, end = 8.dp)
                .align(Alignment.CenterVertically)
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = text
        )
    }
}
