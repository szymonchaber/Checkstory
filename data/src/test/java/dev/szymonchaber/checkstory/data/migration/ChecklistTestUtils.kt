package dev.szymonchaber.checkstory.data.migration

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckbox
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateCheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import java.time.LocalDateTime
import java.util.*

internal object ChecklistTestUtils {

    fun createTemplate(): Template {
        val templateId = TemplateId.new()
        val templateCheckboxId = TemplateCheckboxId(UUID.randomUUID())
        val templateCheckboxId2 = TemplateCheckboxId(UUID.randomUUID())
        return Template(
            id = templateId,
            title = "Example template",
            description = "Example template description",
            items = listOf(
                TemplateCheckbox(
                    id = templateCheckboxId,
                    parentId = null,
                    title = "Top task 0",
                    children = listOf(
                        TemplateCheckbox(
                            id = TemplateCheckboxId(UUID.randomUUID()),
                            parentId = templateCheckboxId,
                            title = "CHILD_TASK_0",
                            children = listOf(),
                            sortPosition = 0,
                            templateId = templateId
                        ),
                        TemplateCheckbox(
                            id = TemplateCheckboxId(UUID.randomUUID()),
                            parentId = templateCheckboxId,
                            title = "CHILD_TASK_1",
                            children = listOf(),
                            sortPosition = 1,
                            templateId = templateId
                        )
                    ),
                    sortPosition = 0,
                    templateId = templateId
                ),
                TemplateCheckbox(
                    id = templateCheckboxId2,
                    parentId = null,
                    title = "Top task 1",
                    children = listOf(
                        TemplateCheckbox(
                            id = TemplateCheckboxId(UUID.randomUUID()),
                            parentId = templateCheckboxId2,
                            title = "CHILD_TASK_2",
                            children = listOf(),
                            sortPosition = 0,
                            templateId = templateId
                        ),
                        TemplateCheckbox(
                            id = TemplateCheckboxId(UUID.randomUUID()),
                            parentId = templateCheckboxId2,
                            title = "CHILD_TASK_3",
                            children = listOf(),
                            sortPosition = 1,
                            templateId = templateId
                        )
                    ),
                    sortPosition = 1,
                    templateId = templateId
                )
            ),
            createdAt = LocalDateTime.now(),
            checklists = listOf(checklist(templateId), checklist(templateId)),
            reminders = listOf(reminderExact(templateId))
        )
    }

    private fun reminderExact(templateId: TemplateId): Reminder {
        return Reminder.Exact(
            id = ReminderId(UUID.randomUUID()),
            forTemplate = templateId,
            startDateTime = LocalDateTime.now().plusDays(1)
        )
    }

    private fun checklist(templateId: TemplateId): Checklist {
        val checklistId = ChecklistId(UUID.randomUUID())
        return Checklist(
            id = checklistId,
            templateId = templateId,
            title = "Example checklist",
            description = "Example checklist description",
            items = List(5) { checkbox(checklistId, it) },
            notes = "Example notes",
            createdAt = LocalDateTime.now()
        )
    }

    private fun checkbox(checklistId: ChecklistId, index: Int): Checkbox {
        val parentId = CheckboxId(UUID.randomUUID())
        return Checkbox(
            id = parentId,
            parentId = null,
            checklistId = checklistId,
            title = "Example item $index",
            isChecked = false,
            children = listOf(
                Checkbox(
                    id = CheckboxId(UUID.randomUUID()),
                    parentId = parentId,
                    checklistId = checklistId,
                    title = "Example item child item 0",
                    isChecked = false,
                    children = emptyList()
                ),
                Checkbox(
                    id = CheckboxId(UUID.randomUUID()),
                    parentId = parentId,
                    checklistId = checklistId,
                    title = "Example item child item 1",
                    isChecked = false,
                    children = emptyList()
                )
            )
        )
    }
}
