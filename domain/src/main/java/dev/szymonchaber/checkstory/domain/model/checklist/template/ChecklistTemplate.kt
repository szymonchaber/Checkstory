package dev.szymonchaber.checkstory.domain.model.checklist.template

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import java.time.LocalDateTime

data class ChecklistTemplate(
    val id: ChecklistTemplateId,
    val title: String,
    val description: String,
    val items: List<TemplateCheckbox>,
    val createdAt: LocalDateTime,
    val checklists: List<Checklist>,
    val reminders: List<Reminder> = listOf(Reminder.Exact(ReminderId(0), id, LocalDateTime.now()))
) {

    val isStored: Boolean
        get() = id.id != 0L
}

data class TemplateCheckbox(
    val id: TemplateCheckboxId,
    val parentId: TemplateCheckboxId?,
    val title: String,
    val children: List<TemplateCheckbox>
)
