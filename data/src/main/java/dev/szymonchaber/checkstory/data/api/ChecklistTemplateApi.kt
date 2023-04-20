package dev.szymonchaber.checkstory.data.api

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.data.api.dto.ChecklistTemplateDto
import dev.szymonchaber.checkstory.domain.model.EditTemplateDomainEvent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

internal class ChecklistTemplateApi @Inject constructor(private val httpClient: HttpClient) {

    val token =
        "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsImV4cCI6MTY3MTY5ODIyNCwiaWF0IjoxNjcxNjYyMjI0fQ.-eLoW8jrKQGoW3Q6W7sUaAABODl5tCAfZBwqcSo_dt8"

    suspend fun getAllChecklistTemplates(): List<ChecklistTemplateDto> {
        return httpClient.get("http://10.0.2.2:8080/templates") {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun pushChecklistTemplates(checklistTemplates: List<ChecklistTemplateDto>): List<ChecklistTemplateDto> {
        return httpClient.post("http://10.0.2.2:8080/templates/batch") {
            header("Authorization", "Bearer $token")
            body = checklistTemplates
        }
    }

    suspend fun helloWorld(token: String): String {
        return httpClient.get("http://10.0.2.2:8080/hello") {
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun pushEvents(editTemplateDomainEvents: List<EditTemplateDomainEvent>) {
        val token = Firebase.auth.currentUser!!.getIdToken(false).result!!.token
        val eventDtos = editTemplateDomainEvents.map {
            when (it) {
                is EditTemplateDomainEvent.CreateNewTemplate -> {
                    val data = Json.encodeToJsonElement(
                        CreateTemplateEventData.serializer(),
                        CreateTemplateEventData(it.templateId.id.toString())
                    )
                    DomainEventDto("createTemplate", it.eventId.toString(), it.timestamp, data)
                }

                is EditTemplateDomainEvent.RenameTemplate -> {
                    val data = Json.encodeToJsonElement(
                        EditTemplateTitleEventData.serializer(),
                        EditTemplateTitleEventData(it.templateId.id.toString(), it.newTitle)
                    )
                    DomainEventDto("editTemplateTitle", it.eventId.toString(), it.timestamp, data)
                }

                is EditTemplateDomainEvent.ChangeTemplateDescription -> {
                    val data = Json.encodeToJsonElement(
                        EditTemplateDescriptionEventData.serializer(),
                        EditTemplateDescriptionEventData(it.templateId.id.toString(), it.newDescription)
                    )
                    DomainEventDto("editTemplateDescription", it.eventId.toString(), it.timestamp, data)
                }

                is EditTemplateDomainEvent.AddTemplateTask -> {
                    val data = Json.encodeToJsonElement(
                        AddTemplateTaskEventData.serializer(),
                        AddTemplateTaskEventData(
                            templateId = it.templateId.id.toString(),
                            taskId = it.taskId.id.toString(),
                            parentTaskId = null
                        )
                    )
                    DomainEventDto("addTemplateTask", it.eventId.toString(), it.timestamp, data)
                }

                is EditTemplateDomainEvent.RenameTemplateTask -> {
                    val data = Json.encodeToJsonElement(
                        RenameTemplateTaskEventData.serializer(),
                        RenameTemplateTaskEventData(
                            templateId = it.templateId.id.toString(),
                            taskId = it.taskId.id.toString(),
                            newTitle = it.newTitle
                        )
                    )
                    DomainEventDto("renameTemplateTask", it.eventId.toString(), it.timestamp, data)
                }

                is EditTemplateDomainEvent.DeleteTemplateTask -> {
                    val data = Json.encodeToJsonElement(
                        DeleteTemplateTaskData.serializer(),
                        DeleteTemplateTaskData(
                            taskId = it.taskId.id.toString(),
                            templateId = it.templateId.id.toString()
                        )
                    )
                    DomainEventDto("deleteTemplateTask", it.eventId.toString(), it.timestamp, data)
                }
            }
        }
            .shuffled() // TODO delete when it's confirmed to be working
        return httpClient.post("http://10.0.2.2:8080/events") {
            header("Authorization", "Bearer $token")
            body = eventDtos
        }
    }
}

@Serializable
data class DomainEventDto(val eventType: String, val eventId: String, val timestamp: Long, val data: JsonElement)

@Serializable
data class CreateTemplateEventData(val templateId: String)

@Serializable
data class EditTemplateTitleEventData(val templateId: String, val newTitle: String)

@Serializable
data class EditTemplateDescriptionEventData(val templateId: String, val newDescription: String)

@Serializable
data class AddTemplateTaskEventData(val templateId: String, val taskId: String, val parentTaskId: String?)

@Serializable
data class RenameTemplateTaskEventData(val templateId: String, val taskId: String, val newTitle: String)

@Serializable
data class DeleteTemplateTaskData(val templateId: String, val taskId: String)
