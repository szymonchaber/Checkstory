package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

val tasks = listOf(
    Task(1, "Pizza", Color.Blue, listOf()),
    Task(2, "French toast", Color.Cyan, listOf()),
    Task(3, "Chocolate cake", Color.Magenta, listOf()),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Experiment() {
    var magicTree by remember {
        mutableStateOf(MagicTree(tasks))
    }
    LongPressDraggable(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            item {
                DropTarget<Task>(modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp), key = Unit, onDataDropped = {
                    magicTree = magicTree.withTaskMovedToTop(it)
                })
            }
            items(items = magicTree.tasks, key = { it.id }) { task ->
                val childTasks = remember {
                    mutableStateListOf<Task>()
                }
                TaskCard(
                    modifier = Modifier.animateItemPlacement(),
                    task = task,
                    childTasks = childTasks,
                    onSiblingTaskDropped = { siblingTask ->
                        magicTree = magicTree.withTaskMovedBelow(siblingTask, task)
                    }
                ) { childTask ->
                    childTasks.add(childTask.copy(name = "Child ${childTask.name}"))
                }
            }
            item {
                DropTarget<Task>(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .height(96.dp),
                    key = true,
                    onDataDropped = {
                        magicTree = magicTree.withTaskMovedToBottom(it)
                    }
                )
            }
        }
        TargetTodoistLine()
    }
}

data class MagicTree(val tasks: List<Task>) {

    fun withTaskMovedToBottom(task: Task): MagicTree {
        return copy(tasks = tasks.toMutableList().apply {
            removeAt(indexOfFirst { it.id == task.id })
            add(task)
        })
    }

    fun withTaskMovedToTop(task: Task): MagicTree {
        return copy(tasks = tasks.toMutableList().apply {
            removeAt(indexOfFirst { it.id == task.id })
            val targetIndex = 0
            if (targetIndex > lastIndex) {
                add(task)
            } else {
                add(targetIndex, task)
            }
        })
    }

    fun withTaskMovedBelow(task: Task, below: Task): MagicTree {
        return copy(tasks = tasks.toMutableList().apply {
            removeAt(indexOfFirst { it.id == task.id })
            val targetIndex = indexOfFirst { it.id == below.id } + 1
            if (targetIndex > lastIndex) {
                add(task)
            } else {
                add(targetIndex, task)
            }
        })
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

data class Task(val id: Int, val name: String, val color: Color, val children: List<Task>)
