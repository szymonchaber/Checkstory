package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.util.concurrent.atomic.AtomicInteger

val colors = listOf(Color.Blue, Color.Red, Color.Magenta, Color.Gray, Color.DarkGray, Color.Cyan, Color.Green)

val food = listOf(
    Task(1, "Pizza", Color.Blue, listOf()),
    Task(2, "French toast", Color.Cyan, listOf()),
    Task(3, "Chocolate cake", Color.Magenta, listOf()),
)

const val NEW_TASK_ID = -50

val indexGenerator = AtomicInteger(0)

val tasks = List(50) {
    val index = indexGenerator.getAndIncrement()
    Task(index, "Item ${it + 1}", colors[index % colors.size], listOf())
}

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
                DropTarget<Task>(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    key = Unit,
                    onDataDropped = {
                        magicTree = magicTree.withTaskMovedToTop(it)
                    }
                )
            }
            items(items = magicTree.tasks, key = { it.id }) { task ->
                NestedTaskCard(
                    modifier = Modifier.animateItemPlacement(),
                    task = task,
                    onSiblingTaskDroppedOnto = { siblingTask, targetTask ->
                        magicTree = magicTree.withTaskMovedBelow(siblingTask, targetTask)
                    },
                    onChildTaskDroppedUnder = { childTask, targetTask ->
                        magicTree = magicTree.withChildMovedUnderTask(childTask, targetTask)
                    }
                )
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
        Draggable(
            modifier = Modifier.align(Alignment.BottomStart),
            dataToDrop = Task(id = NEW_TASK_ID, "", Color.LightGray, listOf())
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Cyan)
                    .height(50.dp)
                    .padding(8.dp)
            ) {
                Text(modifier = Modifier.align(Alignment.Center), text = "New task")
            }
        }
    }
}

@Composable
fun NestedTaskCard(
    modifier: Modifier,
    task: Task,
    onSiblingTaskDroppedOnto: (Task, Task) -> Unit,
    onChildTaskDroppedUnder: (Task, Task) -> Unit
) {
    TaskCard(
        modifier = modifier,
        task = task,
        onSiblingTaskDropped = { siblingTask ->
            onSiblingTaskDroppedOnto(siblingTask, task)
        }
    ) { childTask ->
        onChildTaskDroppedUnder(childTask, task)
    }
    task.children.forEach {
        NestedTaskCard(
            modifier = modifier.padding(start = 24.dp),
            task = it,
            onSiblingTaskDroppedOnto = onSiblingTaskDroppedOnto,
            onChildTaskDroppedUnder = onChildTaskDroppedUnder
        )
    }
}

data class MagicTree(val tasks: List<Task>) {

    fun withTaskMovedToBottom(task: Task): MagicTree {
        val (filteredTasks, removedTask) = withExtractedTask(task.id)
        return copy(tasks = filteredTasks.withTaskAtIndex(removedTask, filteredTasks.size))
    }

    fun withTaskMovedToTop(task: Task): MagicTree {
        val (filteredTasks, removedTask) = withExtractedTask(task.id)
        return copy(tasks = filteredTasks.withTaskAtIndex(removedTask, 0))
    }

    fun withTaskMovedBelow(task: Task, below: Task): MagicTree {
        val (filteredTasks, movedItem) = withExtractedTask(task.id)
        val newTaskIndex = filteredTasks.indexOfFirst { it.id == below.id } + 1
        val new = filteredTasks.withTaskAtIndex(movedItem, newTaskIndex)
        return copy(tasks = new)
    }

    fun withChildMovedUnderTask(childTask: Task, targetTask: Task): MagicTree {
        val (filteredTasks, movedItem) = withExtractedTask(childTask.id)
        return copy(
            tasks = filteredTasks
                .map {
                    it.withMovedChildRecursive(targetTask, movedItem)
                },
        )
    }

    private fun withExtractedTask(taskId: Int): Pair<List<Task>, Task> {
        val index = indexGenerator.getAndIncrement()
        if (taskId == NEW_TASK_ID) {
            return tasks to Task(id = index, "", colors[index % colors.size], listOf())
        }
        var movedItem: Task? = null
        val onItemFoundAndRemoved: (Task) -> Unit = {
            movedItem = it
        }
        val withExtractedElement = tasks
            .filter {
                if (it.id == taskId) {
                    movedItem = it
                    false
                } else {
                    true
                }
            }
            .map {
                it.withoutChild(taskId, onItemFoundAndRemoved)
            }
        return withExtractedElement to movedItem!!
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

data class Task(val id: Int, val name: String, val color: Color, val children: List<Task>) {

    fun withoutChild(childTaskId: Int, onItemFoundAndRemoved: (Task) -> Unit): Task {
        return copy(
            children = children.map { it.withoutChild(childTaskId, onItemFoundAndRemoved) }
                .let { tasks ->
                    tasks
                        .firstOrNull {
                            it.id == childTaskId
                        }?.let { foundChildTask ->
                            tasks.minus(foundChildTask).also {
                                onItemFoundAndRemoved(foundChildTask)
                            }
                        } ?: tasks
                }
        )
    }

    fun withMovedChildRecursive(parentTask: Task, childTask: Task): Task {
        return copy(
            children = children.map { it.withMovedChildRecursive(parentTask, childTask) }
                .let { tasks ->
                    if (id == parentTask.id) {
                        tasks.plus(childTask)
                    } else {
                        tasks
                    }
                }
        )
    }
}

fun List<Task>.withTaskAtIndex(task: Task, index: Int): List<Task> {
    return take(index) + task + drop(index)
}
