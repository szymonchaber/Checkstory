package dev.szymonchaber.checkstory.data.api.event.mapper

import com.google.firebase.auth.FirebaseUser
import dev.szymonchaber.checkstory.data.api.event.dto.AddOrUpdateTemplateReminderCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.AddTemplateTaskCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.ChangeTaskCheckedCommand
import dev.szymonchaber.checkstory.data.api.event.dto.ChecklistCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.CreateChecklistCommand
import dev.szymonchaber.checkstory.data.api.event.dto.CreateTemplateCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.DeleteChecklistCommand
import dev.szymonchaber.checkstory.data.api.event.dto.DeleteTemplateCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.DeleteTemplateReminderCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.DeleteTemplateTaskCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.EditChecklistNotesCommand
import dev.szymonchaber.checkstory.data.api.event.dto.EditTemplateDescriptionCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.EditTemplateTitleCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.MoveTemplateTaskCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.RenameTemplateTaskCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.TaskDto
import dev.szymonchaber.checkstory.data.api.event.dto.TemplateCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.UpdateTasksPositionsCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.toReminderDto
import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox

fun TemplateDomainCommand.toCommandDto(currentUser: FirebaseUser): TemplateCommandDto {
    return when (this) {
        is TemplateDomainCommand.CreateNewTemplate -> {
            CreateTemplateCommandDto(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.RenameTemplate -> {
            EditTemplateTitleCommandDto(
                templateId = templateId.id,
                newTitle = newTitle,
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.ChangeTemplateDescription -> {
            EditTemplateDescriptionCommandDto(
                templateId = templateId.id,
                newDescription = newDescription,
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.AddTemplateTask -> {
            AddTemplateTaskCommandDto(
                templateId = templateId.id,
                taskId = taskId.id,
                parentTaskId = parentTaskId?.id?.toString(),
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.RenameTemplateTask -> {
            RenameTemplateTaskCommandDto(
                templateId = templateId.id,
                taskId = taskId.id,
                newTitle = newTitle,
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.DeleteTemplateTask -> {
            DeleteTemplateTaskCommandDto(
                taskId = taskId.id,
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.UpdateCheckboxPositions -> {
            UpdateTasksPositionsCommandDto(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid,
                taskIdToLocalPosition = localPositions.mapKeys { it.key.id }
            )
        }

        is TemplateDomainCommand.MoveTemplateTask -> {
            MoveTemplateTaskCommandDto(
                templateId = templateId.id,
                taskId = taskId.id,
                newParentTaskId = newParentTaskId?.id,
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.AddOrReplaceTemplateReminder -> {
            AddOrUpdateTemplateReminderCommandDto(
                templateId = templateId.id,
                reminder = reminder.toReminderDto(),
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.DeleteTemplateReminder -> {
            DeleteTemplateReminderCommandDto(
                templateId = templateId.id,
                reminderId = reminderId.id,
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.DeleteTemplate -> {
            DeleteTemplateCommandDto(
                templateId = templateId.id,
                commandId = commandId,
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }
    }
}

fun ChecklistDomainCommand.toCommandDto(currentUser: FirebaseUser): ChecklistCommandDto {
    return when (this) {
        is ChecklistDomainCommand.CreateChecklistCommand -> CreateChecklistCommand(
            checklistId.id,
            templateId.id,
            title,
            description,
            tasks.map {
                it.toTaskDto()
            },
            commandId,
            timestamp,
            currentUser.uid
        )

        is ChecklistDomainCommand.ChangeTaskCheckedCommand -> ChangeTaskCheckedCommand(
            checklistId = checklistId.id,
            taskId = taskId.id,
            isChecked = isChecked,
            commandId = commandId,
            timestamp = timestamp,
            userId = currentUser.uid
        )

        is ChecklistDomainCommand.DeleteChecklistCommand -> DeleteChecklistCommand(
            checklistId = checklistId.id,
            commandId = commandId,
            timestamp = timestamp,
            userId = currentUser.uid
        )

        is ChecklistDomainCommand.EditChecklistNotesCommand -> EditChecklistNotesCommand(
            checklistId = checklistId.id,
            newNotes = newNotes,
            commandId = commandId,
            timestamp = timestamp,
            userId = currentUser.uid
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
