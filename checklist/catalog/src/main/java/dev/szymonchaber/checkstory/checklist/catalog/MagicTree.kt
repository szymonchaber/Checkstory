package dev.szymonchaber.checkstory.checklist.catalog

import timber.log.Timber

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
