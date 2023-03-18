package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

const val NEW_TASK_ID = -50

val indexGenerator = AtomicInteger(0)

val tasks = List(10) {
    val index = indexGenerator.getAndIncrement()

    Task(index, "Task ${index + 1}", List(2) {
        val localIndex = indexGenerator.getAndIncrement()
        Task(localIndex, "Task ${localIndex + 1}", listOf())
    })
}

class DragDropListStateMine(
    val unwrappedTasks: List<Pair<Task, Int>>,
    val lazyListState: LazyListState,
    private val draggableItemState: DragDropState
) {
    var draggedDistance by mutableStateOf(0f)

    var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    val elementDisplacement: Float?
        get() = currentIndexOfDraggedItem
            ?.let { lazyListState.getVisibleItemInfoFor(absoluteIndex = it) }
            ?.let { item ->
                (initiallyDraggedElement?.offset ?: 0f).toFloat() + draggedDistance - item.offset
            }

    var overscrollJob by mutableStateOf<Job?>(null)

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.takeUnless { it.key !is Int }
            ?.also { itemInfo ->
                currentIndexOfDraggedItem = itemInfo.index
                initiallyDraggedElement = itemInfo
                draggableItemState.dragPosition = Offset(0f, itemInfo.offset.toFloat())
                draggableItemState.isDragging = true
                draggableItemState.dataToDrop = itemInfo.key as Int
                draggableItemState.draggableComposable = { _ ->
                    Text(
                        modifier = Modifier.background(Color.White),
                        text = "Task with name ${unwrappedTasks.firstOrNull { it.first.id == itemInfo.key }?.first?.name}"
                    )
                }
            }
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        overscrollJob?.cancel()
        draggableItemState.isDragging = false
        draggableItemState.dragOffset = Offset.Zero
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y
        draggableItemState.dragOffset += offset
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance

            return@let when {
                draggedDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}

@Composable
fun rememberDragDropListStateMine(
    lazyListState: LazyListState = rememberLazyListState(),
    unwrappedTasks: List<Pair<Task, Int>>,
    draggableItemState: DragDropState,
): DragDropListStateMine {
    return remember {
        DragDropListStateMine(
            unwrappedTasks = unwrappedTasks,
            lazyListState = lazyListState,
            draggableItemState = draggableItemState
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Experiment() {
    var magicTree by remember {
        mutableStateOf(MagicTree(tasks))
    }
    val items: List<Pair<Task, Int>> by remember {
        derivedStateOf {
            magicTree.flattenWithNestedLevel()
        }
    }

    val dragDropState = remember { DragDropState() }

    LaunchedEffect(key1 = dragDropState.isDragging) {
        val data = if (!dragDropState.isDragging) {
            dragDropState.dataToDrop
        } else {
            null
        }
        data?.let {
            dragDropState.dataToDrop = null
            dragDropState.currentDropTarget?.invoke(it)
        }
    }

    CompositionLocalProvider(
        LocalDragDropState provides dragDropState,
    ) {
        val scope = rememberCoroutineScope()
        var overscrollJob by remember { mutableStateOf<Job?>(null) }
        val dragDropListStateMine =
            rememberDragDropListStateMine(
                unwrappedTasks = items,
                draggableItemState = dragDropState
            )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDrag = { change, offset ->
                                change.consume()
                                dragDropListStateMine.onDrag(offset)

                                if (overscrollJob?.isActive == true)
                                    return@detectDragGesturesAfterLongPress

                                dragDropListStateMine
                                    .checkForOverScroll()
                                    .takeIf { it != 0f }
                                    ?.let {
                                        overscrollJob =
                                            scope.launch { dragDropListStateMine.lazyListState.scrollBy(it) }
                                    }
                                    ?: run { overscrollJob?.cancel() }
                            },
                            onDragStart = { offset -> dragDropListStateMine.onDragStart(offset) },
                            onDragEnd = { dragDropListStateMine.onDragInterrupted() },
                            onDragCancel = { dragDropListStateMine.onDragInterrupted() }
                        )
                    },
                contentPadding = PaddingValues(horizontal = 10.dp),
                state = dragDropListStateMine.lazyListState
            ) {
                item {
                    DropTarget(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        key = Unit,
                        onDataDropped = {
                            magicTree = magicTree.withTaskMovedToTop(it)
                        }
                    )
                }
                items(items = items, key = { it.first.id }) { (task, nestedLevel) ->
                    val startPadding by animateDpAsState(
                        24.dp * nestedLevel
                    )
                    TaskCard(
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(start = startPadding),
                        task = task,
                        onSiblingTaskDroppedOnto = { newSiblingId ->
                            magicTree = magicTree.withTaskMovedBelow(newSiblingId, below = task)
                        },
                        onChildTaskDroppedUnder = { childTaskId ->
                            magicTree = magicTree.withChildMovedUnderTask(childTaskId, targetTask = task)
                        }
                    )
                }
                item {
                    DropTarget(
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
            DropTargetIndicatorLine()
            Draggable(
                dataToDrop = NEW_TASK_ID,
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Cyan)
                        .height(50.dp)
                        .padding(8.dp)
                        .then(it)
                ) {
                    Text(modifier = Modifier.align(Alignment.Center), text = "New task")
                }
            }
            if (dragDropState.isDragging) {
                var targetSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            dragDropListStateMine.elementDisplacement
                            val offset = (dragDropState.dragPosition + dragDropState.dragOffset)
                            alpha = if (targetSize == IntSize.Zero) 0f else .9f
                            translationX = offset.x//.minus(12.dp.toPx())
//                            translationY = offset.y//.minus(targetSize.height * 2 + 0.dp.toPx())
                            translationY = offset.y
//                                dragDropState.dragPosition.y + (dragDropListStateMine.elementDisplacement ?: 0f)
                        }
                        .onGloballyPositioned {
                            targetSize = it.size
                        }
                ) {
                    dragDropState.draggableComposable?.invoke(Modifier)
                }
            }
        }
    }
}

data class MagicTree(val tasks: List<Task>) {

    fun withTaskMovedToBottom(taskId: Int): MagicTree {
        val (filteredTasks, removedTask) = withExtractedTask(taskId)
        return copy(tasks = filteredTasks.withTaskAtIndex(removedTask, filteredTasks.size))
    }

    fun withTaskMovedToTop(taskId: Int): MagicTree {
        val (filteredTasks, removedTask) = withExtractedTask(taskId)
        return copy(tasks = filteredTasks.withTaskAtIndex(removedTask, 0))
    }

    fun withTaskMovedBelow(taskId: Int, below: Task): MagicTree {
        val (filteredTasks, movedItem) = withExtractedTask(taskId)
        val isSiblingTopLevel = filteredTasks.any { it.id == below.id }
        val new = if (isSiblingTopLevel) {
            val newTaskIndex = filteredTasks.indexOfFirst { it.id == below.id } + 1
            filteredTasks.withTaskAtIndex(movedItem, newTaskIndex)
        } else {
            filteredTasks.map {
                it.withMovedSiblingRecursive(below.id, movedItem)
            }
        }
        return copy(tasks = new)
    }

    fun withChildMovedUnderTask(childTaskId: Int, targetTask: Task): MagicTree {
        val (filteredTasks, movedItem) = withExtractedTask(childTaskId)
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
            return tasks to Task(id = index, "", listOf())
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

    fun logMagicTree(indent: String = "") {
        tasks.forEachIndexed { index, task ->
            val isLast = index == tasks.lastIndex
            val prefix = if (isLast) "└─" else "├─"
            val nextIndent = indent + if (isLast) "  " else "│ "
            Timber.d("$indent$prefix${task.id} ${task.name}")
            logTaskChildren(task, nextIndent)
        }
    }

    private fun logTaskChildren(task: Task, indent: String) {
        task.children.forEachIndexed { index, childTask ->
            val isLast = index == task.children.lastIndex
            val prefix = if (isLast) "└─" else "├─"
            val nextIndent = indent + if (isLast) "  " else "│ "
            Timber.d("$indent$prefix${childTask.id} ${childTask.name}")
            logTaskChildren(childTask, nextIndent)
        }
    }

    fun flattenWithNestedLevel(): List<Pair<Task, Int>> {
        val result = mutableListOf<Pair<Task, Int>>()

        fun visit(task: Task, level: Int) {
            result.add(Pair(task, level))
            task.children.forEach { child -> visit(child, level + 1) }
        }

        tasks.forEach { task -> visit(task, 0) }
        return result
    }
}

data class Task(val id: Int, val name: String, val children: List<Task>) {

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
                        tasks.withTaskAtIndex(childTask, 0)
                    } else {
                        tasks
                    }
                }
        )
    }

    fun withMovedSiblingRecursive(siblingId: Int, movedItem: Task): Task {
        val children = children.map {
            it.withMovedSiblingRecursive(siblingId, movedItem)
        }
            .let { tasks ->
                if (tasks.any { it.id == siblingId }) {
                    val targetLocalIndex = tasks.indexOfFirst { it.id == siblingId } + 1
                    Timber.d("Dropping below ${tasks[targetLocalIndex - 1].name}!")
                    tasks.withTaskAtIndex(movedItem, targetLocalIndex)
                } else {
                    tasks
                }
            }
        return copy(children = children)
    }
}

fun List<Task>.withTaskAtIndex(task: Task, index: Int): List<Task> {
    return take(index) + task + drop(index)
}
