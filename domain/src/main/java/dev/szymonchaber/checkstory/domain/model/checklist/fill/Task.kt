package dev.szymonchaber.checkstory.domain.model.checklist.fill

data class Task(
    val id: TaskId,
    val parentId: TaskId?,
    val checklistId: ChecklistId,
    val title: String,
    val isChecked: Boolean,
    val children: List<Task>
) {

    fun withUpdatedIsCheckedRecursive(id: TaskId, isChecked: Boolean): Task {
        return if (this.id == id) {
            copy(isChecked = isChecked)
        } else {
            copy(children = children.map { it.withUpdatedIsCheckedRecursive(id, isChecked) })
        }
    }

    companion object {

        fun List<Task>.checkedCount(): Int {
            return count { it.isChecked }
        }
    }
}
