package dev.szymonchaber.checkstory.checklist.catalog

fun List<Task>.withTaskAtIndex(task: Task, index: Int): List<Task> {
    return take(index) + task + drop(index)
}
