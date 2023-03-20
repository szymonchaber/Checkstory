package dev.szymonchaber.checkstory.checklist.template.reoder

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.szymonchaber.checkstory.checklist.template.DragDropState

@Composable
fun rememberDragDropState(lazyListState: LazyListState = rememberLazyListState()): DragDropState {
    return remember {
        DragDropState(lazyListState = lazyListState)
    }
}
