package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.CommandDao
import dev.szymonchaber.checkstory.domain.model.ChecklistCommand
import dev.szymonchaber.checkstory.domain.model.Command
import dev.szymonchaber.checkstory.domain.model.TemplateCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CommandRepository @Inject constructor(
    private val dao: CommandDao,
    private val commandMapper: CommandMapper
) {

    fun getUnappliedCommandsFlow(): Flow<List<Command>> {
        return dao.getAll()
            .map { commands ->
                withContext(Dispatchers.Default) {
                    commands.map(commandMapper::toDomainCommand)
                }
            }
    }

    suspend fun storeCommands(commands: List<Command>) {
        withContext(Dispatchers.Default) {
            dao.insertAll(commands.map(commandMapper::toCommandEntity))
        }
    }

    suspend fun commandCount(): Int {
        return dao.getAllSuspend().size
    }

    suspend fun commandOnlyTemplates(): List<ChecklistTemplate> {
        return getUnappliedCommandsFlow().first()
            .filterIsInstance<TemplateCommand>()
            .groupBy { it.templateId }
            .filterValues {
                it.any { command ->
                    command is TemplateCommand.CreateNewTemplate
                }
            }
            .map { (id, commands) ->
                commands.fold(ChecklistTemplate.empty(id)) { acc, command ->
                    command.applyTo(acc)
                }
            }
    }

    suspend fun commandOnlyChecklists(): List<Checklist> {
        return getUnappliedCommandsFlow().first()
            .filterIsInstance<ChecklistCommand>()
            .groupBy { it.checklistId }
            .filterValues {
                it.any { command ->
                    command is ChecklistCommand.CreateChecklistCommand
                }
            }
            .map { (id, commands) ->
                commands.fold(Checklist.empty(id)) { acc, command ->
                    command.applyTo(acc)
                }
            }
    }

    suspend fun hydrate(template: ChecklistTemplate): ChecklistTemplate {
        return commandsForTemplate(template.id)
            .fold(template) { acc, command ->
                command.applyTo(acc)
            }
    }

    suspend fun hydrate(checklist: Checklist): Checklist {
        return commandsForChecklist(checklist.id)
            .fold(checklist) { acc, command ->
                command.applyTo(acc)
            }
    }

    private suspend fun commandsForTemplate(templateId: ChecklistTemplateId): List<TemplateCommand> {
        return getUnappliedCommandsFlow().first()
            .filterIsInstance<TemplateCommand>()
            .filter { command -> command.templateId == templateId }
    }

    private suspend fun commandsForChecklist(checklistId: ChecklistId): List<ChecklistCommand> {
        return getUnappliedCommandsFlow().first()
            .filterIsInstance<ChecklistCommand>()
            .filter { command -> command.checklistId == checklistId }
    }

    suspend fun deleteCommands(ids: List<UUID>) {
        ids.forEach {
            dao.deleteById(it)
        }
    }

    suspend fun deleteAllCommands() {
        dao.deleteAll()
    }
}
