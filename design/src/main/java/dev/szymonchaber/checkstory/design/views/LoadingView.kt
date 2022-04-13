package dev.szymonchaber.checkstory.design.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FullSizeLoadingView() {
    Column(modifier = Modifier.fillMaxSize()) {
        LoadingView()
    }
}

@Composable
fun ColumnScope.LoadingView() {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .align(alignment = Alignment.CenterHorizontally)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(top = 24.dp)
                .align(alignment = Alignment.CenterVertically)
        )
    }
}
