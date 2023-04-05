package dev.szymonchaber.checkstory.checklist.template.reoder

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.szymonchaber.checkstory.checklist.template.DragDropState
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState = rememberLazyListState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): DragDropState {
    val density = LocalDensity.current
    return remember {
        with(density) {
            DragDropState(lazyListState = lazyListState, scope = coroutineScope, maxScroll = 20.dp.toPx())
        }
    }
}
