package dev.szymonchaber.checkstory.api.command.mapper

import dev.szymonchaber.checkstory.api.command.dto.ApiChecklistCommandTask
import dev.szymonchaber.checkstory.api.command.dto.ApiExistingTemplateData
import dev.szymonchaber.checkstory.api.command.dto.ApiTemplateCommandReminder
import dev.szymonchaber.checkstory.api.command.dto.TemplateApiCommand
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Task

internal fun TemplateCommand.toCommandDto(): TemplateApiCommand {
    return when (this) {
        is TemplateCommand.CreateNewTemplate -> {
            TemplateApiCommand.CreateTemplateApiCommand(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
                existingTemplateData = existingData?.let(ApiExistingTemplateData::fromTemplate)
            )
        }

        is TemplateCommand.RenameTemplate -> {
            TemplateApiCommand.EditTemplateTitleApiCommand(
                templateId = templateId.id,
                newTitle = newTitle,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateCommand.ChangeTemplateDescription -> {
            TemplateApiCommand.EditTemplateDescriptionApiCommand(
                templateId = templateId.id,
                newDescription = newDescription,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateCommand.AddTemplateTask -> {
            TemplateApiCommand.AddTemplateTaskApiCommand(
                templateId = templateId.id,
                taskId = taskId.id,
                parentTaskId = parentTaskId?.id?.toString(),
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateCommand.RenameTemplateTask -> {
            TemplateApiCommand.RenameTemplateTaskApiCommand(
                templateId = templateId.id,
                taskId = taskId.id,
                newTitle = newTitle,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateCommand.DeleteTemplateTask -> {
            TemplateApiCommand.DeleteTemplateTaskApiCommand(
                taskId = taskId.id,
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateCommand.UpdateTaskPositions -> {
            TemplateApiCommand.UpdateTasksPositionsApiCommand(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
                taskIdToLocalPosition = localPositions.mapKeys { it.key.id }
            )
        }

        is TemplateCommand.MoveTemplateTask -> {
            TemplateApiCommand.MoveTemplateTaskApiCommand(
                templateId = templateId.id,
                taskId = taskId.id,
                newParentTaskId = newParentTaskId?.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateCommand.AddOrReplaceTemplateReminder -> {
            TemplateApiCommand.AddOrUpdateTemplateReminderApiCommand(
                templateId = templateId.id,
                reminder = ApiTemplateCommandReminder.fromReminder(reminder),
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateCommand.DeleteTemplateReminder -> {
            TemplateApiCommand.DeleteTemplateReminderApiCommand(
                templateId = templateId.id,
                reminderId = reminderId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }

        is TemplateCommand.DeleteTemplate -> {
            TemplateApiCommand.DeleteTemplateApiCommand(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
            )
        }
    }
}

internal fun Task.toTaskDto(): ApiChecklistCommandTask {
    return ApiChecklistCommandTask(
        id = id.id,
        checklistId = checklistId.id,
        title = title,
        sortPosition = 0,
        isChecked = isChecked,
        children = children.map { it.toTaskDto() }
    )
}
