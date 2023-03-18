package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset


class DragDropState {
    var isDragging by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable (Modifier) -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Int?>(null)

    var currentDropTarget: ((Int) -> Unit)? by mutableStateOf(null)
    var currentDropTargetPosition: Offset? by mutableStateOf(null)
}

val LocalDragDropState = compositionLocalOf { DragDropState() }
