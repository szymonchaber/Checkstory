package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.data.database.datasource.CommandDataSource
import dev.szymonchaber.checkstory.domain.model.ChecklistDomainCommand
import dev.szymonchaber.checkstory.domain.model.DomainCommand
import dev.szymonchaber.checkstory.domain.model.TemplateDomainCommand
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checklist
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplate
import dev.szymonchaber.checkstory.domain.model.checklist.template.ChecklistTemplateId
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandRepositoryImpl @Inject constructor(
    private val commandDataSource: CommandDataSource
) {

    val unappliedCommandsFlow = commandDataSource.getAll()

    suspend fun storeCommands(domainCommands: List<DomainCommand>) {
        commandDataSource.insert(domainCommands)
    }

    suspend fun commandCount(): Int {
        return commandDataSource.count()
    }

    suspend fun commandOnlyTemplates(): List<ChecklistTemplate> {
        val domainCommands = unappliedCommandsFlow.first()
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
        return unappliedCommandsFlow.first()
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
        return unappliedCommandsFlow.first()
            .filterIsInstance<TemplateDomainCommand>()
            .filter { command -> command.templateId == templateId }
    }

    private suspend fun commandsForChecklist(checklistId: ChecklistId): List<ChecklistDomainCommand> {
        return unappliedCommandsFlow.first()
            .filterIsInstance<ChecklistDomainCommand>()
            .filter { command -> command.checklistId == checklistId }
    }

    suspend fun deleteCommands(ids: List<UUID>) {
        commandDataSource.deleteByIds(ids)
    }

    suspend fun deleteAllCommands() {
        commandDataSource.deleteAllCommands()
    }
}
