package dev.szymonchaber.checkstory.data.api.event.mapper

import com.google.firebase.auth.FirebaseUser
import dev.szymonchaber.checkstory.data.api.event.dto.AddTemplateTaskEventDto
import dev.szymonchaber.checkstory.data.api.event.dto.CreateTemplateEventDto
import dev.szymonchaber.checkstory.data.api.event.dto.DeleteTemplateTaskEventDto
import dev.szymonchaber.checkstory.data.api.event.dto.EditTemplateDescriptionEventDto
import dev.szymonchaber.checkstory.data.api.event.dto.EditTemplateTitleEventDto
import dev.szymonchaber.checkstory.data.api.event.dto.EventDto
import dev.szymonchaber.checkstory.data.api.event.dto.RenameTemplateTaskEventDto
import dev.szymonchaber.checkstory.domain.model.DomainEvent
import dev.szymonchaber.checkstory.domain.model.EditTemplateDomainEvent

fun DomainEvent.toEventDto(currentUser: FirebaseUser): EventDto {
    return when (this) {
        is EditTemplateDomainEvent.CreateNewTemplate -> {
            CreateTemplateEventDto(
                templateId = templateId.id.toString(),
                eventId = eventId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is EditTemplateDomainEvent.RenameTemplate -> {
            EditTemplateTitleEventDto(
                templateId = templateId.id.toString(),
                newTitle = newTitle,
                eventId = eventId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is EditTemplateDomainEvent.ChangeTemplateDescription -> {
            EditTemplateDescriptionEventDto(
                templateId = templateId.id.toString(),
                newDescription = newDescription,
                eventId = eventId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is EditTemplateDomainEvent.AddTemplateTask -> {
            AddTemplateTaskEventDto(
                templateId = templateId.id.toString(),
                taskId = taskId.id.toString(),
                parentTaskId = parentTaskId?.id?.toString(),
                eventId = eventId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is EditTemplateDomainEvent.RenameTemplateTask -> {
            RenameTemplateTaskEventDto(
                templateId = templateId.id.toString(),
                taskId = taskId.id.toString(),
                newTitle = newTitle,
                eventId = eventId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }

        is EditTemplateDomainEvent.DeleteTemplateTask -> {
            DeleteTemplateTaskEventDto(
                taskId = taskId.id.toString(),
                templateId = templateId.id.toString(),
                eventId = eventId.toString(),
                timestamp = timestamp,
                userId = currentUser.uid
            )
        }
    }
}
