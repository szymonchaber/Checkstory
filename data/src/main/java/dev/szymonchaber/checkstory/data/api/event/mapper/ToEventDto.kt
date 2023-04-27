package dev.szymonchaber.checkstory.data.api.event.mapper

import com.google.firebase.auth.FirebaseUser
import dev.szymonchaber.checkstory.data.api.event.dto.AddTemplateTaskCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.CommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.CreateTemplateCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.DeleteTemplateTaskCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.EditTemplateDescriptionCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.EditTemplateTitleCommandDto
import dev.szymonchaber.checkstory.data.api.event.dto.RenameTemplateTaskCommandDto
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand

fun DomainCommand.toCommandDto(currentUser: FirebaseUser): CommandDto {
    return when (this) {
        is TemplateDomainCommand.CreateNewTemplate -> {
            CreateTemplateCommandDto(
                templateId = templateId.id.toString(),
                eventId = commandId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.RenameTemplate -> {
            EditTemplateTitleCommandDto(
                templateId = templateId.id.toString(),
                newTitle = newTitle,
                eventId = commandId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.ChangeTemplateDescription -> {
            EditTemplateDescriptionCommandDto(
                templateId = templateId.id.toString(),
                newDescription = newDescription,
                eventId = commandId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.AddTemplateTask -> {
            AddTemplateTaskCommandDto(
                templateId = templateId.id.toString(),
                taskId = taskId.id.toString(),
                parentTaskId = parentTaskId?.id?.toString(),
                eventId = commandId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.RenameTemplateTask -> {
            RenameTemplateTaskCommandDto(
                templateId = templateId.id.toString(),
                taskId = taskId.id.toString(),
                newTitle = newTitle,
                eventId = commandId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is TemplateDomainCommand.DeleteTemplateTask -> {
            DeleteTemplateTaskCommandDto(
                taskId = taskId.id.toString(),
                templateId = templateId.id.toString(),
                eventId = commandId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }
    }
}
