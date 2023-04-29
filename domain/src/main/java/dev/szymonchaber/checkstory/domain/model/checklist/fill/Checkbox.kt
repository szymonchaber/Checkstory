package dev.szymonchaber.checkstory.domain.model.checklist.fill

data class Checkbox(
    val id: CheckboxId,
    val parentId: CheckboxId?,
    val checklistId: ChecklistId,
    val title: String,
    val isChecked: Boolean,
    val children: List<Checkbox>
) {

    fun withUpdatedIsCheckedRecursive(id: CheckboxId, isChecked: Boolean): Checkbox {
        return if (this.id == id) {
            copy(isChecked = isChecked)
        } else {
            copy(children = children.map { it.withUpdatedIsCheckedRecursive(id, isChecked) })
        }
    }

    companion object {

        fun List<Checkbox>.checkedCount(): Int {
            return count { it.isChecked }
        }
    }
}
