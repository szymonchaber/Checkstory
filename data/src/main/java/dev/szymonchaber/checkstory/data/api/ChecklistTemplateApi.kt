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
                        CreateTemplateEventData(it.id.toInt())
                    )
                    DomainEventDto("createTemplate", it.timestamp, data)
                }
                is EditTemplateDomainEvent.RenameTemplate -> {
                    val data = Json.encodeToJsonElement(
                        EditTemplateTitleEventData.serializer(),
                        EditTemplateTitleEventData(it.id.toInt(), it.newTitle)
                    )
                    DomainEventDto("editTemplateTitle", it.timestamp, data)
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
data class DomainEventDto(val eventType: String, val timestamp: Long, val data: JsonElement)

@Serializable
data class CreateTemplateEventData(val id: Int)

@Serializable
data class EditTemplateTitleEventData(val id: Int, val newTitle: String)
