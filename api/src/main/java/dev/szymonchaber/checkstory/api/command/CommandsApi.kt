package dev.szymonchaber.checkstory.api.command

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.api.command.dto.ChecklistApiCommand
import dev.szymonchaber.checkstory.api.command.mapper.toCommandDto
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class CommandsApi @Inject constructor(private val httpClient: HttpClient) {

    suspend fun pushCommands(commands: List<Command>) {
        Firebase.auth.currentUser ?: return
        if (commands.isEmpty()) {
            return
        }
        val templateCommands = commands.filterIsInstance<TemplateCommand>()
        val checklistCommands = commands.filterIsInstance<ChecklistCommand>()
        val templateCommandDtos = templateCommands.map {
            it.toCommandDto()
        }
        val checklistCommandDtos = checklistCommands.map {
            ChecklistApiCommand.fromCommand(it)
        }

        httpClient.post("commands") {
            setBody(templateCommandDtos)
        }
        httpClient.post("checklist-commands") {
            setBody(checklistCommandDtos)
        }
    }
}
