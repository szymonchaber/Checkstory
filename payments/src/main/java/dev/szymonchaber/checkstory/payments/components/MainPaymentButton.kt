package dev.szymonchaber.checkstory.payments.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ColumnScope.MainPaymentButton(
    onClick: () -> Unit,
    content: @Composable (RowScope.() -> Unit)
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 8.dp)
            .align(Alignment.CenterHorizontally),
        onClick = onClick, content = content
    )
}
