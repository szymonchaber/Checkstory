package dev.szymonchaber.checkstory.checklist.template.reoder

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.szymonchaber.checkstory.checklist.template.DragDropState
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState = rememberLazyListState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): DragDropState {
    return remember {
        DragDropState(lazyListState = lazyListState, scope = coroutineScope)
    }
}
