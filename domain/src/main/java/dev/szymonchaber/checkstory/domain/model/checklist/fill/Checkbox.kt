package dev.szymonchaber.checkstory.domain.model.checklist.fill

data class Checkbox(
    val id: CheckboxId,
    val parentId: CheckboxId?,
    val checklistId: ChecklistId,
    val title: String,
    val isChecked: Boolean,
    val children: List<Checkbox>
) {

    companion object {

        fun List<Checkbox>.checkedCount(): Int {
            return count { it.isChecked }
        }
    }
}
