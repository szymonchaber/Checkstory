package dev.szymonchaber.checkstory.checklist.template.reoder

import androidx.compose.runtime.compositionLocalOf
import dev.szymonchaber.checkstory.checklist.template.DragDropState

val LocalDragDropState = compositionLocalOf<DragDropState> {
    error("You must provide LocalDragDropState")
}
