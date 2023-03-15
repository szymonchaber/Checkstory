package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import timber.log.Timber

@Composable
fun Experiment() {
    val tasks = remember {
        mutableStateListOf(
            Task(1, "Pizza", Color.Blue),
            Task(2, "French toast", Color.Cyan),
            Task(3, "Chocolate cake", Color.Magenta),
        )
    }
    LongPressDraggable(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            items(items = tasks, key = { it.id }) { task ->
                val childTasks = remember {
                    mutableStateListOf<Task>()
                }
                val siblingTasks = remember {
                    mutableStateListOf<Task>()
                }
                TaskCard(
                    task = task,
                    childTasks = childTasks,
                    siblingTasks = siblingTasks,
                    onSiblingTaskDropped = { siblingTask ->
                        moveTaskBelow(tasks, siblingTask, task)
//                        siblingTasks.add(siblingTask)
                    },
                    onChildTaskDropped = { childTask ->
                        childTasks.add(childTask.copy(name = "Child ${childTask.name}"))
                    }
                )
            }
        }
        TargetTodoistLine()
    }
}

fun moveTaskBelow(tasks: SnapshotStateList<Task>, task: Task, below: Task) {
    Timber.d("Moving $task below $below")
    tasks.removeAt(tasks.indexOfFirst { it.id == task.id })
    val targetIndex = tasks.indexOfFirst { it.id == below.id } + 1
    if (targetIndex > tasks.lastIndex) {
        tasks.add(task)
    } else {
        tasks.add(targetIndex, task)
    }
}

@Composable
private fun TargetTodoistLine() {
    val state = LocalDragTargetInfo.current
    val targetState = LocalTargetedItemInfo.current

    val targetValue = LocalDensity.current.run {
        (targetState.targetedItemPosition ?: Offset.Zero) - Offset.Zero.copy(y = 48.dp.toPx())
    }
    val offset by animateOffsetAsState(targetValue = targetValue)
    if (state.isDragging) {
        Canvas(
            modifier = Modifier
                .padding(end = 20.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = offset.y
                }
        ) {
            drawLine(
                color = Color.Red,
                start = offset.copy(y = 0f),
                end = offset.copy(x = this.size.width, y = 0f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

internal class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
}

internal class TargetedItemInfo {
    var targetedItemSet: Set<Any> by mutableStateOf(setOf())
    var targetedItemPosition: Offset? by mutableStateOf(null)
}

internal val LocalDragTargetInfo = compositionLocalOf { DragTargetInfo() }
internal val LocalTargetedItemInfo = compositionLocalOf { TargetedItemInfo() }

@Composable
fun LongPressDraggable(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val targetState = remember { TargetedItemInfo() }
    val state = remember { DragTargetInfo() }
    CompositionLocalProvider(
        LocalDragTargetInfo provides state,
        LocalTargetedItemInfo provides targetState
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            content()
            if (state.isDragging) {
                var targetSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                Box(modifier = Modifier
                    .graphicsLayer {
                        val offset = (state.dragPosition + state.dragOffset)
                        alpha = if (targetSize == IntSize.Zero) 0f else .9f
                        translationX = offset.x.minus(24.dp.toPx())
                        translationY = offset.y.minus(targetSize.height * 2 + 8.dp.toPx())
                    }
                    .onGloballyPositioned {
                        targetSize = it.size
                    }
                ) {
                    state.draggableComposable?.invoke()
                }
            }
        }
    }
}

data class Task(val id: Int, val name: String, val color: Color)
