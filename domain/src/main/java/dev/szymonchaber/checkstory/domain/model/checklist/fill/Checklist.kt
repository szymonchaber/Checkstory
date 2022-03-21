package dev.szymonchaber.checkstory.domain.model.checklist.fill

import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId

data class Checklist(
    val id: ChecklistId,
    val checklistTemplateId: ChecklistTemplateId,
    val title: String,
    val description: String,
    val items: List<Checkbox>,
    val notes: String
)
