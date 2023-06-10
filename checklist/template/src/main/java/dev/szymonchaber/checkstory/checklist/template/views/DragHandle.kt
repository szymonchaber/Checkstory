package dev.szymonchaber.checkstory.checklist.template.views

import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import dev.szymonchaber.checkstory.design.R

@Composable
fun DragHandle(modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = R.drawable.drag_indicator),
        tint = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
        contentDescription = null
    )
}
