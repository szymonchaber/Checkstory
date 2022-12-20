package dev.szymonchaber.checkstory.data.api

import dev.szymonchaber.checkstory.data.api.dto.ChecklistTemplateDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import javax.inject.Inject

internal class ChecklistTemplateApi @Inject constructor(private val httpClient: HttpClient) {

    val token =
        "token"

    suspend fun getAllChecklistTemplates(): List<ChecklistTemplateDto> {
        return httpClient.get("http://10.0.2.2:8080/templates") {
            header("Authorization", "Bearer $token")
        }
    }
}
