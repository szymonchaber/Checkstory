package dev.szymonchaber.checkstory.domain.model.checklist.fill

data class Checkbox(val id: CheckboxId, val checklistId: ChecklistId, val title: String, val isChecked: Boolean)
