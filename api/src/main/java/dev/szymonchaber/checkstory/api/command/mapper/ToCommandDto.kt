package dev.szymonchaber.checkstory.api.command.mapper

import dev.szymonchaber.checkstory.api.command.dto.AddOrUpdateTemplateReminderApiCommand
import dev.szymonchaber.checkstory.api.command.dto.AddTemplateTaskApiCommand
import dev.szymonchaber.checkstory.api.command.dto.ChangeTaskCheckedApiCommand
import dev.szymonchaber.checkstory.api.command.dto.ChecklistApiCommand
import dev.szymonchaber.checkstory.api.command.dto.CreateChecklistApiCommand
import dev.szymonchaber.checkstory.api.command.dto.CreateTemplateApiCommand
import dev.szymonchaber.checkstory.api.command.dto.DeleteChecklistApiCommand
import dev.szymonchaber.checkstory.api.command.dto.DeleteTemplateApiCommand
import dev.szymonchaber.checkstory.api.command.dto.DeleteTemplateReminderApiCommand
import dev.szymonchaber.checkstory.api.command.dto.DeleteTemplateTaskApiCommand
import dev.szymonchaber.checkstory.api.command.dto.EditChecklistNotesApiCommand
import dev.szymonchaber.checkstory.api.command.dto.EditTemplateDescriptionApiCommand
import dev.szymonchaber.checkstory.api.command.dto.EditTemplateTitleApiCommand
import dev.szymonchaber.checkstory.api.command.dto.MoveTemplateTaskApiCommand
import dev.szymonchaber.checkstory.api.command.dto.RenameTemplateTaskApiCommand
import dev.szymonchaber.checkstory.api.command.dto.TaskDto
import dev.szymonchaber.checkstory.api.command.dto.TemplateApiCommand
import dev.szymonchaber.checkstory.api.command.dto.UpdateTasksPositionsApiCommand
import dev.szymonchaber.checkstory.api.command.dto.toReminderDto
import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox

internal fun TemplateDomainCommand.toCommandDto(): TemplateApiCommand {
    return when (this) {
        is TemplateDomainCommand.CreateNewTemplate -> {
            CreateTemplateApiCommand(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.RenameTemplate -> {
            EditTemplateTitleApiCommand(
                templateId = templateId.id,
                newTitle = newTitle,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.ChangeTemplateDescription -> {
            EditTemplateDescriptionApiCommand(
                templateId = templateId.id,
                newDescription = newDescription,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.AddTemplateTask -> {
            AddTemplateTaskApiCommand(
                templateId = templateId.id,
                taskId = taskId.id,
                parentTaskId = parentTaskId?.id?.toString(),
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.RenameTemplateTask -> {
            RenameTemplateTaskApiCommand(
                templateId = templateId.id,
                taskId = taskId.id,
                newTitle = newTitle,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.DeleteTemplateTask -> {
            DeleteTemplateTaskApiCommand(
                taskId = taskId.id,
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.UpdateCheckboxPositions -> {
            UpdateTasksPositionsApiCommand(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
                taskIdToLocalPosition = localPositions.mapKeys { it.key.id }
            )
        }

        is TemplateDomainCommand.MoveTemplateTask -> {
            MoveTemplateTaskApiCommand(
                templateId = templateId.id,
                taskId = taskId.id,
                newParentTaskId = newParentTaskId?.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.AddOrReplaceTemplateReminder -> {
            AddOrUpdateTemplateReminderApiCommand(
                templateId = templateId.id,
                reminder = reminder.toReminderDto(),
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.DeleteTemplateReminder -> {
            DeleteTemplateReminderApiCommand(
                templateId = templateId.id,
                reminderId = reminderId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.DeleteTemplate -> {
            DeleteTemplateApiCommand(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }
    }
}

internal fun ChecklistDomainCommand.toCommandDto(): ChecklistApiCommand {
    return when (this) {
        is ChecklistDomainCommand.CreateChecklistCommand -> CreateChecklistApiCommand(
            checklistId.id,
            templateId.id,
            title,
            description,
            tasks.map {
                it.toTaskDto()
            },
            commandId,
            timestamp,
        )

        is ChecklistDomainCommand.ChangeTaskCheckedCommand -> ChangeTaskCheckedApiCommand(
            checklistId = checklistId.id,
            taskId = taskId.id,
            isChecked = isChecked,
            commandId = commandId,
            timestamp = timestamp,
        )

        is ChecklistDomainCommand.DeleteChecklistCommand -> DeleteChecklistApiCommand(
            checklistId = checklistId.id,
            commandId = commandId,
            timestamp = timestamp,
        )

        is ChecklistDomainCommand.EditChecklistNotesCommand -> EditChecklistNotesApiCommand(
            checklistId = checklistId.id,
            newNotes = newNotes,
            commandId = commandId,
            timestamp = timestamp,
        )
    }
}

private fun Checkbox.toTaskDto(): TaskDto {
    return TaskDto(
        id = id.id,
        checklistId = checklistId.id,
        title = title,
        sortPosition = 0,
        isChecked = isChecked,
        children = children.map { it.toTaskDto() }
    )
}
