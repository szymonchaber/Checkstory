package dev.szymonchaber.checkstory.data.api.event

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.data.api.event.mapper.toCommandDto
import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

internal class CommandsApi @Inject constructor(private val httpClient: HttpClient) {

    suspend fun pushCommands(commands: List<DomainCommand>) {
        val currentUser = Firebase.auth.currentUser ?: return

        val templateCommands = commands.filterIsInstance<TemplateDomainCommand>()
        val checklistCommands = commands.filterIsInstance<ChecklistDomainCommand>()
        val templateCommandDtos = templateCommands.map {
            it.toCommandDto()
        }
        val checklistCommandDtos = checklistCommands.map {
            it.toCommandDto()
        }

        httpClient.post("http://10.0.2.2:8080/commands") {
            setBody(templateCommandDtos)
        }
        httpClient.post("http://10.0.2.2:8080/checklist-commands") {
            setBody(checklistCommandDtos)
        }
    }
}
