package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.model.command.ChecklistCommandEntity
import dev.szymonchaber.checkstory.data.database.model.command.CommandDataEntity
import dev.szymonchaber.checkstory.data.database.model.command.CommandEntity
import dev.szymonchaber.checkstory.data.database.model.command.TemplateCommandEntity
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal object CommandMapper {

    private const val COMMAND_TYPE_CHECKLIST = "checklistCommand"

    private const val COMMAND_TYPE_TEMPLATE = "templateCommand"

    fun toCommandEntity(command: Command): CommandEntity {
        val (entity, type) = when (command) {
            is ChecklistCommand -> ChecklistCommandEntity.fromDomainCommand(command) to COMMAND_TYPE_CHECKLIST
            is TemplateCommand -> TemplateCommandEntity.fromDomainCommand(command) to COMMAND_TYPE_TEMPLATE
        }
        val data = Json.encodeToString(CommandDataEntity.serializer(), entity)
        return CommandEntity(command.commandId, type, data)
    }

    fun toDomainCommand(commandEntity: CommandEntity): Command {
        return when (commandEntity.type) {
            COMMAND_TYPE_CHECKLIST -> {
                Json.decodeFromString<ChecklistCommandEntity>(commandEntity.jsonData).toDomainCommand()
            }

            COMMAND_TYPE_TEMPLATE -> {
                Json.decodeFromString<TemplateCommandEntity>(commandEntity.jsonData).toDomainCommand()
            }

            else -> throw IllegalStateException("Unknown command type in database: ${commandEntity.type}")
        }
    }
}
