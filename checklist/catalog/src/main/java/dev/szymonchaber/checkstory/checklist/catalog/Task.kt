package dev.szymonchaber.checkstory.checklist.catalog

import timber.log.Timber

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
