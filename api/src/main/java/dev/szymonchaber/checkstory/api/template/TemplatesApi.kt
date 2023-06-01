package dev.szymonchaber.checkstory.api.template

import dev.szymonchaber.checkstory.api.ConfiguredHttpClient
import dev.szymonchaber.checkstory.api.template.model.ApiTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.Template
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class TemplatesApi @Inject constructor(private val httpClient: ConfiguredHttpClient) {

    suspend fun getTemplates(): List<Template> {
        return httpClient.get("templates")
            .body<List<ApiTemplate>>()
            .map(ApiTemplate::toTemplate)
    }
}
