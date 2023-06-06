package dev.szymonchaber.checkstory.api.checklist

import dev.szymonchaber.checkstory.api.checklist.model.ApiChecklist
import dev.szymonchaber.checkstory.api.di.ConfiguredHttpClient
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class ChecklistsApi @Inject constructor(private val httpClient: ConfiguredHttpClient) {

    suspend fun getChecklists(): List<Checklist> {
        return httpClient.get("checklists")
            .body<List<ApiChecklist>>()
            .map(ApiChecklist::toChecklist)
    }
}
