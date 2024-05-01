package dev.szymonchaber.checkstory.design.views

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Suppress("UnusedReceiverParameter")
@Composable
fun RowScope.Space(size: Dp) {
    Spacer(modifier = Modifier.width(size))
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.Space(size: Dp) {
    Spacer(modifier = Modifier.height(size))
}
