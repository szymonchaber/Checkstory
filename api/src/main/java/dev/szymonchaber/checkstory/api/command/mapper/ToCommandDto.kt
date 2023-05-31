package dev.szymonchaber.checkstory.api.command.mapper

import dev.szymonchaber.checkstory.api.command.dto.ApiChecklistCommandTask
import dev.szymonchaber.checkstory.api.command.dto.ApiTemplateCommandReminder
import dev.szymonchaber.checkstory.api.command.dto.TemplateApiCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox

internal fun TemplateDomainCommand.toCommandDto(): TemplateApiCommand {
    return when (this) {
        is TemplateDomainCommand.CreateNewTemplate -> {
            TemplateApiCommand.CreateTemplateApiCommand(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.RenameTemplate -> {
            TemplateApiCommand.EditTemplateTitleApiCommand(
                templateId = templateId.id,
                newTitle = newTitle,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.ChangeTemplateDescription -> {
            TemplateApiCommand.EditTemplateDescriptionApiCommand(
                templateId = templateId.id,
                newDescription = newDescription,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.AddTemplateTask -> {
            TemplateApiCommand.AddTemplateTaskApiCommand(
                templateId = templateId.id,
                taskId = taskId.id,
                parentTaskId = parentTaskId?.id?.toString(),
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.RenameTemplateTask -> {
            TemplateApiCommand.RenameTemplateTaskApiCommand(
                templateId = templateId.id,
                taskId = taskId.id,
                newTitle = newTitle,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.DeleteTemplateTask -> {
            TemplateApiCommand.DeleteTemplateTaskApiCommand(
                taskId = taskId.id,
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.UpdateCheckboxPositions -> {
            TemplateApiCommand.UpdateTasksPositionsApiCommand(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
                taskIdToLocalPosition = localPositions.mapKeys { it.key.id }
            )
        }

        is TemplateDomainCommand.MoveTemplateTask -> {
            TemplateApiCommand.MoveTemplateTaskApiCommand(
                templateId = templateId.id,
                taskId = taskId.id,
                newParentTaskId = newParentTaskId?.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.AddOrReplaceTemplateReminder -> {
            TemplateApiCommand.AddOrUpdateTemplateReminderApiCommand(
                templateId = templateId.id,
                reminder = ApiTemplateCommandReminder.fromReminder(reminder),
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.DeleteTemplateReminder -> {
            TemplateApiCommand.DeleteTemplateReminderApiCommand(
                templateId = templateId.id,
                reminderId = reminderId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateDomainCommand.DeleteTemplate -> {
            TemplateApiCommand.DeleteTemplateApiCommand(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }
    }
}

internal fun Checkbox.toTaskDto(): ApiChecklistCommandTask {
    return ApiChecklistCommandTask(
        id = id.id,
        checklistId = checklistId.id,
        title = title,
        sortPosition = 0,
        isChecked = isChecked,
        children = children.map { it.toTaskDto() }
    )
}
