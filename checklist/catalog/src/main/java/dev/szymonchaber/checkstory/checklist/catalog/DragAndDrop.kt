package dev.szymonchaber.checkstory.checklist.catalog

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Experiment() {
    var magicTree by remember {
        mutableStateOf(MagicTree(tasks))
    }
    val items by remember {
        derivedStateOf {
            magicTree.flattenWithNestedLevel()
        }
    }

    LongPressDraggableOverLazyList(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 10.dp),
            state = it
        ) {
            item {
                DropTarget<Task>(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    key = Unit,
                    onDataDropped = {
                        magicTree = magicTree.withTaskMovedToTop(it)
                    },
                    debugTag = "Top task receptacle"
                )
            }
            items(items = items, key = { it.first.id }) { (task, nestedLevel) ->
                val startPadding: Dp by animateDpAsState(
                    24.dp * nestedLevel
                )
                TaskCard(
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(start = startPadding),
                    task = task,
                    onSiblingTaskDroppedOnto = { newSibling ->
                        Timber.d("onSiblingTaskDroppedOnto: taskToMove: ${newSibling.name} existingSibling: ${task.name}")
                        Timber.d("Content before:\n")
                        magicTree.logMagicTree()
                        magicTree = magicTree.withTaskMovedBelow(newSibling, below = task)
                        Timber.d("Content after:\n")
                        magicTree.logMagicTree()
                    },
                    onChildTaskDroppedUnder = { childTask ->
                        Timber.d("onChildTaskDroppedUnder: childTask: ${childTask.name} targetTask: ${task.name}")
                        Timber.d("Content before:\n")
                        magicTree.logMagicTree()
                        magicTree = magicTree.withChildMovedUnderTask(childTask, targetTask = task)
                        Timber.d("Content after:\n")
                        magicTree.logMagicTree()
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
                    },
                    debugTag = "Bottom task receptacle"
                )
            }
        }
        DropTargetIndicatorLine()
        Draggable(
            modifier = Modifier.align(Alignment.BottomStart),
            dataToDrop = Task(id = NEW_TASK_ID, "", listOf()),
            onDragStart = {}
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
                        tasks.plus(childTask)
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
                    // correct level
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
