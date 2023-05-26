package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.dao.CommandDao
import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
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
class CommandRepository @Inject constructor(
    private val dao: CommandDao
) {

    fun getUnappliedCommandsFlow(): Flow<List<DomainCommand>> {
        return dao.getAll()
            .map { commands ->
                withContext(Dispatchers.Default) {
                    commands.map {
                        CommandMapper.toDomainCommand(it)
                    }
                }
            }
    }

    suspend fun storeCommands(domainCommands: List<DomainCommand>) {
        withContext(Dispatchers.Default) {
            dao.insertAll(domainCommands.map {
                CommandMapper.toCommandEntity(it)
            })
        }
    }

    suspend fun commandCount(): Int {
        return dao.getAllSuspend().size
    }

    suspend fun commandOnlyTemplates(): List<ChecklistTemplate> {
        val domainCommands = getUnappliedCommandsFlow().first()
        return domainCommands
            .filterIsInstance<TemplateDomainCommand>()
            .groupBy { it.templateId }
            .filterValues {
                it.any { command ->
                    command is TemplateDomainCommand.CreateNewTemplate
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
            .filterIsInstance<ChecklistDomainCommand>()
            .groupBy { it.checklistId }
            .filterValues {
                it.any { command ->
                    command is ChecklistDomainCommand.CreateChecklistCommand
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

    private suspend fun commandsForTemplate(templateId: ChecklistTemplateId): List<TemplateDomainCommand> {
        return getUnappliedCommandsFlow().first()
            .filterIsInstance<TemplateDomainCommand>()
            .filter { command -> command.templateId == templateId }
    }

    private suspend fun commandsForChecklist(checklistId: ChecklistId): List<ChecklistDomainCommand> {
        return getUnappliedCommandsFlow().first()
            .filterIsInstance<ChecklistDomainCommand>()
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
