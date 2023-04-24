package dev.szymonchaber.checkstory.data.api

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.data.api.dto.ChecklistTemplateDto
import dev.szymonchaber.checkstory.domain.model.EditTemplateDomainEvent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import javax.inject.Inject

internal class ChecklistTemplateApi @Inject constructor(private val httpClient: HttpClient) {

    val token =
        "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsImV4cCI6MTY3MTY5ODIyNCwiaWF0IjoxNjcxNjYyMjI0fQ.-eLoW8jrKQGoW3Q6W7sUaAABODl5tCAfZBwqcSo_dt8"

    suspend fun getAllChecklistTemplates(): List<ChecklistTemplateDto> {
        return httpClient.get("http://10.0.2.2:8080/templates") {
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun pushChecklistTemplates(checklistTemplates: List<ChecklistTemplateDto>): List<ChecklistTemplateDto> {
        return httpClient.post("http://10.0.2.2:8080/templates/batch") {
            header("Authorization", "Bearer $token")
            setBody(checklistTemplates)
        }.body()
    }

    suspend fun helloWorld(token: String): String {
        return httpClient.get("http://10.0.2.2:8080/hello") {
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun pushEvents(editTemplateDomainEvents: List<EditTemplateDomainEvent>) {
        val token = Firebase.auth.currentUser!!.getIdToken(false).result!!.token
        val eventDtos: List<DomainEvent> = editTemplateDomainEvents.map {
            when (it) {
                is EditTemplateDomainEvent.CreateNewTemplate -> {
                    CreateTemplateEvent(
                        templateId = it.templateId.id.toString(),
                        eventId = it.eventId.toString(),
                        timestamp = it.timestamp
                    )
                }

                is EditTemplateDomainEvent.RenameTemplate -> {
                    EditTemplateTitleEvent(
                        templateId = it.templateId.id.toString(),
                        newTitle = it.newTitle,
                        eventId = it.eventId.toString(),
                        timestamp = it.timestamp
                    )
                }

                is EditTemplateDomainEvent.ChangeTemplateDescription -> {
                    EditTemplateDescriptionEvent(
                        templateId = it.templateId.id.toString(),
                        newDescription = it.newDescription,
                        eventId = it.eventId.toString(),
                        timestamp = it.timestamp
                    )
                }

                is EditTemplateDomainEvent.AddTemplateTask -> {
                    AddTemplateTaskEvent(
                        templateId = it.templateId.id.toString(),
                        taskId = it.taskId.id.toString(),
                        parentTaskId = it.parentTaskId?.id?.toString(),
                        eventId = it.eventId.toString(),
                        timestamp = it.timestamp
                    )
                }

                is EditTemplateDomainEvent.RenameTemplateTask -> {
                    RenameTemplateTaskEvent(
                        templateId = it.templateId.id.toString(),
                        taskId = it.taskId.id.toString(),
                        newTitle = it.newTitle,
                        eventId = it.eventId.toString(),
                        timestamp = it.timestamp
                    )
                }

                is EditTemplateDomainEvent.DeleteTemplateTask -> {
                    DeleteTemplateTaskEvent(
                        taskId = it.taskId.id.toString(),
                        templateId = it.templateId.id.toString(),
                        eventId = it.eventId.toString(),
                        timestamp = it.timestamp
                    )
                }
            }
        }
            .shuffled() // TODO delete when it's confirmed to be working
        return httpClient.post("http://10.0.2.2:8080/events") {
            header("Authorization", "Bearer $token")
            setBody(eventDtos)
        }.body()
    }
}

@Serializable
sealed interface DomainEvent {

    @Transient
    val eventType: String
    val eventId: String
    val timestamp: Long
}

@Serializable
sealed interface TemplateEvent : DomainEvent {

    val templateId: String
}

@Serializable
@SerialName("createTemplate")
data class CreateTemplateEvent(
    override val templateId: String,
    override val eventId: String,
    override val timestamp: Long
) : TemplateEvent {

    override val eventType: String = "createTemplate"
}

@Serializable
@SerialName("editTemplateTitle")
data class EditTemplateTitleEvent(
    override val templateId: String,
    val newTitle: String,
    override val eventId: String,
    override val timestamp: Long
) : TemplateEvent {

    override val eventType: String = "editTemplateTitle"
}

@Serializable
@SerialName("editTemplateDescription")
data class EditTemplateDescriptionEvent(
    override val templateId: String,
    val newDescription: String,
    override val eventId: String,
    override val timestamp: Long
) : TemplateEvent {

    override val eventType: String = "editTemplateDescription"
}

@Serializable
@SerialName("addTemplateTask")
data class AddTemplateTaskEvent(
    override val templateId: String,
    val taskId: String,
    val parentTaskId: String?,
    override val eventId: String,
    override val timestamp: Long
) : TemplateEvent {

    override val eventType: String = "addTemplateTask"
}

@Serializable
@SerialName("renameTemplateTask")
data class RenameTemplateTaskEvent(
    override val templateId: String,
    val taskId: String,
    val newTitle: String,
    override val eventId: String,
    override val timestamp: Long
) : TemplateEvent {

    override val eventType: String = "renameTemplateTask"
}

@Serializable
@SerialName("deleteTemplateTask")
data class DeleteTemplateTaskEvent(
    override val templateId: String,
    val taskId: String,
    override val eventId: String,
    override val timestamp: Long
) : TemplateEvent {

    override val eventType: String = "deleteTemplateTask"
}
