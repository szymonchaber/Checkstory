package dev.szymonchaber.checkstory.api.command

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.api.command.dto.ChecklistApiCommand
import dev.szymonchaber.checkstory.api.command.dto.TemplateApiCommand
import dev.szymonchaber.checkstory.api.command.mapper.toCommandDto
import dev.szymonchaber.checkstory.api.di.ConfiguredHttpClient
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import javax.inject.Inject

class CommandsApi @Inject constructor(private val httpClient: ConfiguredHttpClient) {

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
        val payload = CommandsPayload(templateCommandDtos, checklistCommandDtos)
        httpClient.post("sync/commands") {
            setBody(payload)
        }
    }
}

@Serializable
internal data class CommandsPayload(
    val templateCommands: List<TemplateApiCommand>,
    val checklistCommands: List<ChecklistApiCommand>
)
