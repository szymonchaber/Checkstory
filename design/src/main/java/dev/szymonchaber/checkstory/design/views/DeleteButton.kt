package dev.szymonchaber.checkstory.design.views

import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import dev.szymonchaber.checkstory.design.R

@Composable
fun DeleteButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        modifier = modifier
            .clickable(onClick = onClick),
        text = stringResource(R.string.delete),
        color = Color.Red // TODO decide on my own colors
    )
}
