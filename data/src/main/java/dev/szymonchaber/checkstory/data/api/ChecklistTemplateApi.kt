package dev.szymonchaber.checkstory.data.api

import dev.szymonchaber.checkstory.data.api.dto.ChecklistTemplateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
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
}
