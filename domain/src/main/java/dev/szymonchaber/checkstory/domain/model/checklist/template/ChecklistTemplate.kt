package dev.szymonchaber.checkstory.domain.model.checklist.template

import java.time.LocalDateTime

data class ChecklistTemplate(
    val id: ChecklistTemplateId,
    val title: String,
    val description: String,
    val items: List<TemplateCheckbox>,
    val createdAt: LocalDateTime
)

data class TemplateCheckbox(
    val id: TemplateCheckboxId,
    val parentId: TemplateCheckboxId?,
    val title: String,
    val children: List<TemplateCheckbox>
)
