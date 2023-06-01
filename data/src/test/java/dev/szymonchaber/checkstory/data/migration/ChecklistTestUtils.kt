package dev.szymonchaber.checkstory.data.migration

import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task
import dev.szymonchaber.checkstory.domain.model.checklist.fill.TaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateId
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTask
import dev.szymonchaber.checkstory.domain.model.checklist.template.TemplateTaskId
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.Reminder
import dev.szymonchaber.checkstory.domain.model.checklist.template.reminder.ReminderId
import java.time.LocalDateTime
import java.util.*

internal object ChecklistTestUtils {

    fun createTemplate(): Template {
        val templateId = TemplateId.new()
        val templateTaskId = TemplateTaskId(UUID.randomUUID())
        val templateTaskId2 = TemplateTaskId(UUID.randomUUID())
        return Template(
            id = templateId,
            title = "Example template",
            description = "Example template description",
            tasks = listOf(
                TemplateTask(
                    id = templateTaskId,
                    parentId = null,
                    title = "Top task 0",
                    children = listOf(
                        TemplateTask(
                            id = TemplateTaskId(UUID.randomUUID()),
                            parentId = templateTaskId,
                            title = "CHILD_TASK_0",
                            children = listOf(),
                            sortPosition = 0,
                            templateId = templateId
                        ),
                        TemplateTask(
                            id = TemplateTaskId(UUID.randomUUID()),
                            parentId = templateTaskId,
                            title = "CHILD_TASK_1",
                            children = listOf(),
                            sortPosition = 1,
                            templateId = templateId
                        )
                    ),
                    sortPosition = 0,
                    templateId = templateId
                ),
                TemplateTask(
                    id = templateTaskId2,
                    parentId = null,
                    title = "Top task 1",
                    children = listOf(
                        TemplateTask(
                            id = TemplateTaskId(UUID.randomUUID()),
                            parentId = templateTaskId2,
                            title = "CHILD_TASK_2",
                            children = listOf(),
                            sortPosition = 0,
                            templateId = templateId
                        ),
                        TemplateTask(
                            id = TemplateTaskId(UUID.randomUUID()),
                            parentId = templateTaskId2,
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

    private fun checkbox(checklistId: ChecklistId, index: Int): Task {
        val parentId = TaskId(UUID.randomUUID())
        return Task(
            id = parentId,
            parentId = null,
            checklistId = checklistId,
            title = "Example item $index",
            isChecked = false,
            children = listOf(
                Task(
                    id = TaskId(UUID.randomUUID()),
                    parentId = parentId,
                    checklistId = checklistId,
                    title = "Example item child item 0",
                    isChecked = false,
                    children = emptyList()
                ),
                Task(
                    id = TaskId(UUID.randomUUID()),
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
