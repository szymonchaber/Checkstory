package dev.szymonchaber.checkstory.design.views

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
        color = Color.Gray,
        text = text,
    )
}
